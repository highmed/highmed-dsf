package org.highmed.dsf.bpe.message;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.client.WebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.hl7.fhir.r4.model.*;

import java.util.stream.Stream;

public class SendSimpleCohortSizeResult extends AbstractTaskMessageSend
{
	public SendSimpleCohortSizeResult(OrganizationProvider organizationProvider, WebserviceClientProvider clientProvider)
	{
		super(organizationProvider, clientProvider);
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		// TODO: change to multiinstance for multiple cohorts of one research study
		Integer result = (Integer) execution.getVariable(Constants.VARIABLE_QUERY_RESULT);

		return Stream.of(new Task.ParameterComponent(new CodeableConcept(new Coding(Constants.CODESYSTEM_HIGHMED_BPMN,
				Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_SIMPLE_COHORT_SIZE_QUERY_RESULT, null)),
				new IntegerType(result)));
	}
}
