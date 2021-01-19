package org.highmed.dsf.bpe.message;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_BUNDLE_ID;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task.ParameterComponent;

import ca.uhn.fhir.context.FhirContext;

public class SendRequest extends AbstractTaskMessageSend
{
	public SendRequest(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Override
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		String bundleId = (String) execution.getVariable(BPMN_EXECUTION_VARIABLE_BUNDLE_ID);
		return Stream
				.of(toInputParameterBundleReference(getFhirWebserviceClientProvider().getLocalBaseUrl(), bundleId));
	}

	private ParameterComponent toInputParameterBundleReference(String localBaseUrl, String bundleId)
	{
		if (bundleId == null || bundleId.isEmpty())
			throw new IllegalArgumentException("bundleId null or empty");

		return new ParameterComponent(new CodeableConcept(new Coding(CODESYSTEM_HIGHMED_UPDATE_RESOURCE,
				CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE, null)),
				new Reference().setReference(localBaseUrl + (localBaseUrl.endsWith("/") ? "" : "/") + bundleId));
	}
}
