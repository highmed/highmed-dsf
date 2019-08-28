package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.WebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.UrlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class DownloadResearchStudy extends AbstractServiceDelegate implements InitializingBean
{
	private static final String RESEARCH_STUDY_PREFIX = "ResearchStudy/";

	private static final Logger logger = LoggerFactory.getLogger(DownloadResearchStudy.class);

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
		Reference researchStudyReference = getResearchStudyReference(task);
		UrlType endpointAddress = getEndpointAddress(task);

		WebserviceClient client;
		if (task.getRequester().equalsDeep(task.getRestriction().getRecipient().get(0)))
		{
			logger.trace("Downloading ResearchStudy referenced in task  {} from local endpoint", task.getId());
			client = clientProvider.getLocalWebserviceClient();
		}
		else
		{
			logger.trace("Downloading ResearchStudy referenced in task {} from remote endpoint {}", task.getId(),
					endpointAddress.asStringValue());
			client = clientProvider.getRemoteWebserviceClient(endpointAddress.asStringValue());
		}

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

		execution.setVariable(Constants.VARIABLE_RESEARCH_STUDY, researchStudy);
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
		else if (!researchStudyReferences.get(0).hasReference())
		{
			logger.error("Task input parameter {} has no ResearchStudy reference",
					Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_RESEARCH_STUDY_REFERENCE);
			throw new RuntimeException(
					"Task input parameter " + Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_RESEARCH_STUDY_REFERENCE
							+ " has no research study reference");
		}

		return researchStudyReferences.get(0);
	}

	private UrlType getEndpointAddress(Task task)
	{
		Optional<UrlType> endpointAddress = taskHelper
				.getFirstInputParameterUrlValue(task, Constants.CODESYSTEM_HIGHMED_BPMN,
						Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_ENDPOINT_ADDRESS);

		if (endpointAddress.isEmpty())
		{
			logger.error("Task is missing input parameter {}",
					Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_ENDPOINT_ADDRESS);
			throw new RuntimeException("Task is missing input parameter "
					+ Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_RESEARCH_STUDY_REFERENCE);
		}

		return endpointAddress.get();
	}
}
