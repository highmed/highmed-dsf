package org.highmed.dsf.bpe.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.OutputWrapper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

public class DownloadFeasibilityResources extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadFeasibilityResources.class);

	private final OrganizationProvider organizationProvider;

	public DownloadFeasibilityResources(OrganizationProvider organizationProvider,
			FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		logger.debug("{}: Process-instance-id {}, business-key {}, variables {}, local-variables {}",
				getClass().getName(), execution.getProcessInstanceId(), execution.getBusinessKey(),
				execution.getVariables(), execution.getVariablesLocal());

		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);

		IdType researchStudyType = getResearchStudyIdType(task);
		FhirWebserviceClient client = getWebserviceClient(researchStudyType);
		ResearchStudy researchStudy = getResearchStudy(researchStudyType, client);
		execution.setVariable(Constants.VARIABLE_RESEARCH_STUDY, researchStudy);

		@SuppressWarnings("unchecked")
		List<OutputWrapper> outputs = (List<OutputWrapper>) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);

		List<Group> cohortDefinitions = getCohortDefinitions(researchStudy, outputs, client);
		execution.setVariable(Constants.VARIABLE_COHORTS, cohortDefinitions);

		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, outputs);
	}

	private IdType getResearchStudyIdType(Task task)
	{
		Reference researchStudyReference = getTaskHelper()
				.getInputParameterReferenceValues(task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE)
				.collect(Collectors.toList()).get(0);

		return new IdType(researchStudyReference.getReference());
	}

	private FhirWebserviceClient getWebserviceClient(IdType researchStudyType)
	{
		String baseUrl = Optional.ofNullable(researchStudyType.getBaseUrl()).orElse("");

		FhirWebserviceClient client;
		if (baseUrl.equals(getFhirWebserviceClientProvider().getLocalBaseUrl()) || baseUrl.isEmpty())
		{
			client = getFhirWebserviceClientProvider().getLocalWebserviceClient();
		}
		else
		{
			client = getFhirWebserviceClientProvider().getRemoteWebserviceClient(baseUrl);
		}

		return client;
	}

	private ResearchStudy getResearchStudy(IdType researchStudyType, FhirWebserviceClient client)
	{
		ResearchStudy researchStudy;
		try
		{
			researchStudy = client.read(ResearchStudy.class, researchStudyType.getIdPart());
		}
		catch (WebApplicationException e)
		{
			throw new ResourceNotFoundException(
					"Error while reading ResearchStudy with id " + researchStudyType.getIdPart() + " from " + client
							.getBaseUrl());
		}

		return researchStudy;
	}

	private List<Group> getCohortDefinitions(ResearchStudy researchStudy, List<OutputWrapper> outputs,
			FhirWebserviceClient client)
	{
		List<Group> cohortDefinitions = new ArrayList<>();
		List<Reference> cohortDefinitionReferences = researchStudy.getEnrollment();

		OutputWrapper errors = new OutputWrapper(Constants.CODESYSTEM_HIGHMED_BPMN);

		cohortDefinitionReferences.forEach(reference -> {
			try
			{
				IdType type = new IdType(reference.getReference());
				Group group = client.read(Group.class, type.getIdPart());
				cohortDefinitions.add(group);
			}
			catch (WebApplicationException e)
			{
				String errorMessage =
						"Error while reading cohort definition with id " + reference.getReference() + " from " + client
								.getBaseUrl();

				logger.info(errorMessage);
				errors.addKeyValue(Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE, errorMessage);
			}
		});

		outputs.add(errors);
		return cohortDefinitions;
	}
}
