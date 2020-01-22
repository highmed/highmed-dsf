package org.highmed.dsf.bpe.service;

import javax.ws.rs.WebApplicationException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

public class DownloadResearchStudyResource extends AbstractServiceDelegate implements InitializingBean
{
	public DownloadResearchStudyResource(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);

		IdType researchStudyId = getResearchStudyId(task);
		FhirWebserviceClient client = getFhirWebserviceClientProvider().getLocalWebserviceClient();
		ResearchStudy researchStudy = getResearchStudy(researchStudyId, client);

		execution.setVariable(Constants.VARIABLE_RESEARCH_STUDY, researchStudy);

		boolean needsConsentCheck = getNeedsConsentCheck(task);
		execution.setVariable(Constants.VARIABLE_NEEDS_CONSENT_CHECK, needsConsentCheck);

		boolean needsRecordLinkage = getNeedsRecordLinkageCheck(task);
		execution.setVariable(Constants.VARIABLE_NEEDS_RECORD_LINKAGE, needsRecordLinkage);

		// TODO: remove when implemented
		if(needsConsentCheck || needsRecordLinkage)
		{
			throw new UnsupportedOperationException("Consent Check and Record Linkage not yet supported.");
		}
	}

	private IdType getResearchStudyId(Task task)
	{
		Reference researchStudyReference = getTaskHelper()
				.getInputParameterReferenceValues(task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE).findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						"ResearchStudy reference is not set in task with id='" + task.getId() + "', this error should "
								+ "have been caught by resource validation"));

		return new IdType(researchStudyReference.getReference());
	}

	private ResearchStudy getResearchStudy(IdType researchStudyid, FhirWebserviceClient client)
	{
		try
		{
			return client.read(ResearchStudy.class, researchStudyid.getIdPart());
		}
		catch (WebApplicationException e)
		{
			throw new ResourceNotFoundException(
					"Error while reading ResearchStudy with id " + researchStudyid.getIdPart() + " from " + client
							.getBaseUrl());
		}
	}

	private boolean getNeedsConsentCheck(Task task)
	{
		return getTaskHelper().getFirstInputParameterBooleanValue(task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
				Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK).orElseThrow(() -> new IllegalArgumentException(
				"NeedsConsentCheck boolean is not set in task with id='" + task.getId() + "', this error should "
						+ "have been caught by resource validation"));
	}

	private boolean getNeedsRecordLinkageCheck(Task task)
	{
		return getTaskHelper().getFirstInputParameterBooleanValue(task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
				Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE).orElseThrow(() -> new IllegalArgumentException(
				"NeedsRecordLinkage boolean is not set in task with id='" + task.getId() + "', this error should "
						+ "have been caught by resource validation"));
	}
}
