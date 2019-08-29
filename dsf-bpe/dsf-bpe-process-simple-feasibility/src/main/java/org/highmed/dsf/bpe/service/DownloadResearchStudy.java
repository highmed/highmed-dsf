package org.highmed.dsf.bpe.service;

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
import org.highmed.dsf.fhir.client.WebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class DownloadResearchStudy extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadResearchStudy.class);

	private static final String RESEARCH_STUDY_PREFIX = "ResearchStudy/";

	private final WebserviceClientProvider clientProvider;
	private final TaskHelper taskHelper;

	public DownloadResearchStudy(WebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider.getLocalWebserviceClient(), taskHelper);

		this.clientProvider = clientProvider;
		this.taskHelper = taskHelper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(clientProvider, "clientProvider");
		Objects.requireNonNull(taskHelper, "taskHelper");
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		logger.debug("{}: Process-instance-id {}, business-key {}, variables {}, local-variables {}",
				getClass().getName(), execution.getProcessInstanceId(), execution.getBusinessKey(),
				execution.getVariables(), execution.getVariablesLocal());

		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);

		String endpointAddress = getEndpointAddress(task);
		WebserviceClient client = getWebserviceClient(task, endpointAddress);

		Reference researchStudyReference = getResearchStudyReference(task);
		ResearchStudy researchStudy = getResearchStudy(task, researchStudyReference, client);

		execution.setVariable(Constants.VARIABLE_RESEARCH_STUDY, researchStudy);
	}

	private String getEndpointAddress(Task task)
	{
		if(!task.getRequester().hasIdentifier()) {
			logger.error("Task requester reference identifier parameter is null for task with id='{}'", task.getId());
			throw new NullPointerException("Task requester reference identifier parameter is null");
		}

		Identifier requesterIdentifier = task.getRequester().getIdentifier();

		Map<String, List<String>> searchParams = new HashMap<>();
		searchParams.put("managingOrganization",
				Collections.singletonList(requesterIdentifier.getSystem() + "|" + requesterIdentifier.getValue()));

		WebserviceClient client = clientProvider.getLocalWebserviceClient();
		Bundle bundle = client.search(Endpoint.class, searchParams);

		if(bundle.getEntry().size() != 1) {
			logger.error("Did not find endpoint of task requester: expected 1, found {}, for task with id={}''", bundle.getEntry().size(), task.getId());
			throw new RuntimeException("Did not find endpoint of task requester: expected 1, found " + bundle.getEntry().size());
		}

		Endpoint endpoint = (Endpoint) bundle.getEntry().get(0).getResource();
		return endpoint.getAddress();
	}

	private WebserviceClient getWebserviceClient(Task task, String endpointAddress)
	{
		WebserviceClient client;
		if (task.getRequester().equalsDeep(task.getRestriction().getRecipient().get(0)))
		{
			logger.trace("Downloading ResearchStudy referenced in task with id='{}' from local endpoint", task.getId());
			client = clientProvider.getLocalWebserviceClient();
		}
		else
		{
			logger.trace("Downloading ResearchStudy referenced in task with id='{}' from remote endpoint {}", task.getId(),
					endpointAddress);
			client = clientProvider.getRemoteWebserviceClient(endpointAddress);
		}

		return client;
	}

	private Reference getResearchStudyReference(Task task)
	{
		List<Reference> researchStudyReferences = taskHelper
				.getInputParameterReferenceValues(task, Constants.CODESYSTEM_HIGHMED_BPMN,
						Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_RESEARCH_STUDY_REFERENCE).collect(Collectors.toList());

		if (researchStudyReferences.size() != 1)
		{
			logger.error("Task input parameter {} contains unexpected number of ResearchStudy IDs, expected 1, got {}",
					Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_RESEARCH_STUDY_REFERENCE, researchStudyReferences.size());
			throw new RuntimeException(
					"Task input parameter " + Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_RESEARCH_STUDY_REFERENCE
							+ " contains unexpected number of ResearchStudy IDs, expected 1, got "
							+ researchStudyReferences.size());
		}

		if (!researchStudyReferences.get(0).hasReference())
		{
			logger.error("Task input parameter {} has no ResearchStudy reference",
					Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_RESEARCH_STUDY_REFERENCE);
			throw new RuntimeException(
					"Task input parameter " + Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_RESEARCH_STUDY_REFERENCE
							+ " has no research study reference");
		}

		return researchStudyReferences.get(0);
	}

	private ResearchStudy getResearchStudy(Task task, Reference researchStudyReference, WebserviceClient client)
	{
		ResearchStudy researchStudy;
		try
		{
			researchStudy = client.read(ResearchStudy.class,
					researchStudyReference.getReference().substring(RESEARCH_STUDY_PREFIX.length()));
		}
		catch (WebApplicationException e)
		{
			logger.error("Error while reading ResearchStudy with id {} from organization {}",
					researchStudyReference.getReference(), task.getRequester().getReference());
			throw new RuntimeException(
					"Error while reading ResearchStudy with id " + researchStudyReference.getReference()
							+ " from organization " + task.getRequester().getReference(), e);
		}

		return researchStudy;
	}
}
