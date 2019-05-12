package org.highmed.bpe.service;

import java.util.Objects;
import java.util.stream.Stream;

import javax.ws.rs.WebApplicationException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.bpe.Constants;
import org.highmed.fhir.client.WebserviceClient;
import org.highmed.fhir.client.WebserviceClientProvider;
import org.highmed.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;

public class UpdateResources implements JavaDelegate, InitializingBean
{
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

		Stream<Reference> bundleReferences = (Stream<Reference>) taskHelper.getInputParameterReferenceValues(task,
				Constants.CODESYSTEM_HIGHMED_BPMN, Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_BUNDLE_REFERENCE);

		Bundle searchTransaction = new Bundle();
		searchTransaction.setType(BundleType.TRANSACTION);
		bundleReferences.map(this::toSearchEntry).forEach(searchTransaction::addEntry);

		FhirContext context = FhirContext.forR4();
		// TODO remove logging of parsed value
		logger.debug("Search Transaction: {}",
				context.newXmlParser().setPrettyPrint(true).encodeResourceToString(searchTransaction));

		WebserviceClient requesterClient = clientProvider
				.getRemoteWebserviceClient(new IdType(task.getRequester().getReference()));

		try
		{
			Bundle searchResult = requesterClient.postBundle(searchTransaction);
			// TODO remove logging of parsed value
			logger.debug("Search Result: {}",
					context.newXmlParser().setPrettyPrint(true).encodeResourceToString(searchResult));
		}
		catch (WebApplicationException e)
		{
			if (e.getResponse().hasEntity())
				logger.error("Error while handling search bundle {}", context.newXmlParser().setPrettyPrint(true)
						.encodeResourceToString(e.getResponse().readEntity(OperationOutcome.class)));

			throw e;
		}
	}

	private BundleEntryComponent toSearchEntry(Reference bundleReference)
	{
		BundleEntryComponent entry = new BundleEntryComponent();
		entry.getRequest().setMethod(HTTPVerb.GET).setUrl(bundleReference.getReference());
		return entry;
	}
}
