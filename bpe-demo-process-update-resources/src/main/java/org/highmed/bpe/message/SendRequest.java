package org.highmed.bpe.message;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.bpe.Constants;
import org.highmed.fhir.client.WebserviceClientProvider;
import org.highmed.fhir.organization.OrganizationProvider;
import org.highmed.fhir.task.AbstractTaskMessageSend;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task.ParameterComponent;

public class SendRequest extends AbstractTaskMessageSend
{
	public SendRequest(OrganizationProvider organizationProvider, WebserviceClientProvider clientProvider)
	{
		super(organizationProvider, clientProvider);
	}

	@Override
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		String bundleId = (String) execution.getVariable(Constants.VARIABLE_BUNDLE_ID);
		return Stream.of(bundleId).map(this::toInputParameter);
	}

	private ParameterComponent toInputParameter(String bundleId)
	{
		return new ParameterComponent(
				new CodeableConcept(new Coding(Constants.CODESYSTEM_HIGHMED_BPMN,
						Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_BUNDLE_REFERENCE, null)),
				new Reference().setReference(bundleId));
	}
}
