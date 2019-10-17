package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.Constants.GROUP_PREFIX;
import static org.highmed.dsf.bpe.Constants.ORGANIZATION_PREFIX;
import static org.highmed.dsf.bpe.Constants.RESEARCH_STUDY_PREFIX;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Group;
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

		Reference requester = task.getRequester();
		Reference recipient = task.getRestriction().getRecipient().get(0);

		String endpointAddress = getEndpointAddress(requester, recipient);
		FhirWebserviceClient client = getWebserviceClient(requester, recipient, endpointAddress);

		ResearchStudy researchStudy = getResearchStudy(task, client);
		execution.setVariable(Constants.VARIABLE_RESEARCH_STUDY, researchStudy);

		@SuppressWarnings("unchecked")
		List<OutputWrapper> outputs = (List<OutputWrapper>) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);

		List<Group> cohortDefinitions = getCohortDefinitions(researchStudy, outputs, client);
		execution.setVariable(Constants.VARIABLE_COHORTS, cohortDefinitions);

		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, outputs);
	}

	private String getEndpointAddress(Reference requester, Reference recipient)
	{
		String requesterReference = requester.getReference();
		if (requesterReference.startsWith(ORGANIZATION_PREFIX) && !requester.equalsDeep(recipient))
		{
			Map<String, List<String>> searchParams = new HashMap<>();
			searchParams.put("organization",
					Collections.singletonList(requesterReference.substring(ORGANIZATION_PREFIX.length())));

			FhirWebserviceClient client = getFhirWebserviceClientProvider().getLocalWebserviceClient();
			Bundle bundle = client.search(Endpoint.class, searchParams);

			if (bundle.getEntry().size() != 1)
			{
				throw new ResourceNotFoundException(
						"Did not find endpoint of task requester: expected 1, found " + bundle.getEntry().size());
			}

			Endpoint endpoint = (Endpoint) bundle.getEntry().get(0).getResource();
			return endpoint.getAddress();
		}

		return getFhirWebserviceClientProvider().getLocalBaseUrl();
	}

	private FhirWebserviceClient getWebserviceClient(Reference requester, Reference recipient, String endpointAddress)
	{
		FhirWebserviceClient client;
		if (requester.equalsDeep(recipient) || !requester.getReference().startsWith(ORGANIZATION_PREFIX))
		{
			client = getFhirWebserviceClientProvider().getLocalWebserviceClient();
		}
		else
		{
			client = getFhirWebserviceClientProvider().getRemoteWebserviceClient(endpointAddress);
		}

		return client;
	}

	private ResearchStudy getResearchStudy(Task task, FhirWebserviceClient client)
	{
		Reference researchStudyReference = getTaskHelper()
				.getInputParameterReferenceValues(task, Constants.CODESYSTEM_HIGHMED_UPDATE_RESOURCE,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE)
				.collect(Collectors.toList()).get(0);

		ResearchStudy researchStudy;
		try
		{
			researchStudy = client.read(ResearchStudy.class,
					researchStudyReference.getReference().substring(RESEARCH_STUDY_PREFIX.length()));
		}
		catch (WebApplicationException e)
		{
			throw new ResourceNotFoundException(
					"Error while reading ResearchStudy with id " + researchStudyReference.getReference() + " from "
							+ client.getBaseUrl());
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
				Group group = client.read(Group.class, reference.getReference().substring(GROUP_PREFIX.length()));
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
