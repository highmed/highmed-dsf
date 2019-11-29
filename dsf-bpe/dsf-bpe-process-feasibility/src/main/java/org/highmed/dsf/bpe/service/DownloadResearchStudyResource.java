package org.highmed.dsf.bpe.service;

import java.security.InvalidParameterException;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

public class DownloadResearchStudyResource extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadResearchStudyResource.class);

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
	}

	private IdType getResearchStudyId(Task task)
	{
		Reference researchStudyReference = getTaskHelper()
				.getInputParameterReferenceValues(task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE).findFirst()
				.orElseThrow(() -> new InvalidParameterException(
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
}
