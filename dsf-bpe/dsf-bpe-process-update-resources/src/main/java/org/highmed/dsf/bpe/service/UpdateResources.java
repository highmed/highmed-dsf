package org.highmed.dsf.bpe.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.WebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.UrlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;

public class UpdateResources extends AbstractServiceDelegate implements InitializingBean
{
	private static final String BUNDLE_ID_PREFIX = "Bundle/";

	private static final Logger logger = LoggerFactory.getLogger(UpdateResources.class);

	private final WebserviceClientProvider clientProvider;
	private final TaskHelper taskHelper;
	private final FhirContext context;

	public UpdateResources(WebserviceClientProvider clientProvider, TaskHelper taskHelper, FhirContext context)
	{
		super(clientProvider.getLocalWebserviceClient(), taskHelper);
		this.clientProvider = clientProvider;
		this.taskHelper = taskHelper;
		this.context = context;
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

		Reference bundleReference = getBundleReference(task);
		UrlType endpointAddress = getEndpointAddress(task);

		WebserviceClient requesterClient = clientProvider.getRemoteWebserviceClient(endpointAddress.asStringValue());

		Bundle bundle;
		try
		{
			bundle = requesterClient.read(Bundle.class,
					bundleReference.getReference().substring(BUNDLE_ID_PREFIX.length()));
		}
		catch (WebApplicationException e)
		{
			logger.error("Error while reading Bundle with id {} from organization {}", bundleReference.getReference(),
					task.getRequester().getReference());
			throw new RuntimeException("Error while reading Bundle with id " + bundleReference.getReference()
					+ " from organization " + task.getRequester().getReference(), e);
		}

		if (!EnumSet.of(BundleType.TRANSACTION, BundleType.BATCH).contains(bundle.getType()))
		{
			logger.error("Bundle type TRANSACTION or BATCH expected, but got {}", bundle.getType());
			throw new RuntimeException("Bundle type TRANSACTION or BATCH expected, but got " + bundle.getType());
		}

		try
		{
			logger.debug("Posting bundle to local endpoint: {}", context.newXmlParser().encodeResourceToString(bundle));
			clientProvider.getLocalWebserviceClient().postBundle(bundle);
		}
		catch (Exception e)
		{
			logger.error("Error while executing read Bundle with id {} from organization {} locally",
					bundleReference.getReference(), task.getRequester().getReference());
			throw new RuntimeException("Error while executing read Bundle with id " + bundleReference.getReference()
					+ " from organization " + task.getRequester().getReference() + " locally", e);
		}
	}

	private Reference getBundleReference(Task task)
	{
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

		return bundleReferences.get(0);
	}

	private UrlType getEndpointAddress(Task task)
	{
		Optional<UrlType> endpointAddress = taskHelper.getFirstInputParameterUrlValue(task,
				Constants.CODESYSTEM_HIGHMED_BPMN, Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_ENDPOINT_ADDRESS);

		if (endpointAddress.isEmpty())
		{
			logger.error("Task is missing input parameter {}",
					Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_ENDPOINT_ADDRESS);
			throw new RuntimeException(
					"Task is missing input parameter " + Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_BUNDLE_REFERENCE);
		}

		return endpointAddress.get();
	}
}
