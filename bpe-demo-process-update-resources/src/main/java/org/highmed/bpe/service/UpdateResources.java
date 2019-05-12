package org.highmed.bpe.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.bpe.Constants;
import org.highmed.fhir.client.WebserviceClient;
import org.highmed.fhir.client.WebserviceClientProvider;
import org.highmed.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class UpdateResources implements JavaDelegate, InitializingBean
{
	private static final String BUNDLE_ID_PREFIX = "Bundle/";

	private static final Logger logger = LoggerFactory.getLogger(UpdateResources.class);

	private final WebserviceClientProvider clientProvider;
	private final TaskHelper taskHelper;

	public UpdateResources(WebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
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
	public void execute(DelegateExecution execution) throws Exception
	{
		logger.debug("{}: Process-instance-id {}, business-key {}, variables {}, local-variables {}",
				getClass().getName(), execution.getProcessInstanceId(), execution.getBusinessKey(),
				execution.getVariables(), execution.getVariablesLocal());

		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);

		List<Reference> bundleReferences = taskHelper.getInputParameterReferenceValues(task,
				Constants.CODESYSTEM_HIGHMED_BPMN, Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_BUNDLE_REFERENCE)
				.collect(Collectors.toList());

		if (bundleReferences.size() != 1)
		{
			logger.error("Task input parameter {} contains unexpected number of Bundle IDs, expected 1, got {}",
					Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_BUNDLE_REFERENCE, bundleReferences.size());
			throw new RuntimeException(
					"Task input parameter " + Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_BUNDLE_REFERENCE
							+ " contains unexpected number of Bundle IDs, expected 1, got " + bundleReferences.size());
		}
		else if (!bundleReferences.get(0).hasReference()
				|| !bundleReferences.get(0).getReference().startsWith(BUNDLE_ID_PREFIX))
		{
			logger.error("Task input parameter {} has no Bundle reference",
					Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_BUNDLE_REFERENCE);
			throw new RuntimeException("Task input parameter "
					+ Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_BUNDLE_REFERENCE + " has no Bundle reference");
		}

		WebserviceClient requesterClient = clientProvider
				.getRemoteWebserviceClient(new IdType(task.getRequester().getReference()));

		Bundle bundle;
		try
		{
			bundle = requesterClient.read(Bundle.class,
					bundleReferences.get(0).getReference().substring(BUNDLE_ID_PREFIX.length()));
		}
		catch (WebApplicationException e)
		{
			logger.error("Error while reading Bundle with id {} from organization {}",
					bundleReferences.get(0).getReference(), task.getRequester().getReference());
			throw new RuntimeException("Error while reading Bundle with id " + bundleReferences.get(0).getReference()
					+ " from organization " + task.getRequester().getReference());
		}

		if (!EnumSet.of(BundleType.TRANSACTION, BundleType.BATCH).contains(bundle.getType()))
		{
			logger.error("Bundle type TRANSACTION or BATCH expected, but got {}", bundle.getType());
			throw new RuntimeException("Bundle type TRANSACTION or BATCH expected, but got " + bundle.getType());
		}

		try
		{
			clientProvider.getLocalWebserviceClient().postBundle(bundle);
		}
		catch (Exception e)
		{
			logger.error("Error while executing read Bundle with id {} from organization {} locally",
					bundleReferences.get(0).getReference(), task.getRequester().getReference());
			throw new RuntimeException(
					"Error while executing read Bundle with id " + bundleReferences.get(0).getReference()
							+ " from organization " + task.getRequester().getReference() + " locally");
		}
	}
}
