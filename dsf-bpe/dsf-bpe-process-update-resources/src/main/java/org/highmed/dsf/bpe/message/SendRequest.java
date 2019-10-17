package org.highmed.dsf.bpe.message;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.hl7.fhir.r4.model.UrlType;

public class SendRequest extends AbstractTaskMessageSend
{
	public SendRequest(OrganizationProvider organizationProvider, FhirWebserviceClientProvider clientProvider,
			TaskHelper taskHelper)
	{
		super(organizationProvider, clientProvider, taskHelper);
	}

	@Override
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		String bundleId = (String) execution.getVariable(Constants.VARIABLE_BUNDLE_ID);
		return Stream.of(toInputParameterBundleReference(bundleId),
				toInputParameterEndpointAddress(getFhirWebserviceClientProvider().getLocalBaseUrl()));
	}

	private ParameterComponent toInputParameterBundleReference(String bundleId)
	{
		return new ParameterComponent(new CodeableConcept(new Coding(Constants.CODESYSTEM_HIGHMED_UPDATE_RESOURCE,
				Constants.CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE, null)),
				new Reference().setReference(bundleId));
	}

	private ParameterComponent toInputParameterEndpointAddress(String localBaseUrl)
	{
		return new ParameterComponent(new CodeableConcept(new Coding(Constants.CODESYSTEM_HIGHMED_UPDATE_RESOURCE,
				Constants.CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_ENDPOINT_ADDRESS, null)),
				new UrlType().setValue(localBaseUrl));
	}
}
