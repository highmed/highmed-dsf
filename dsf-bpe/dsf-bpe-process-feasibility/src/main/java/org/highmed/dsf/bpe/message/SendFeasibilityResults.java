package org.highmed.dsf.bpe.message;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.client.WebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.bpe.variables.MultiInstanceResult;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;

public class SendFeasibilityResults extends AbstractTaskMessageSend
{
	public SendFeasibilityResults(OrganizationProvider organizationProvider,
			WebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(organizationProvider, clientProvider, taskHelper);
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		MultiInstanceResult result = ((MultiInstanceResult) execution.getVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULT));
		Map<String, String> queryResults = result.getQueryResults();

		List<Task.ParameterComponent> outputs = queryResults.entrySet().stream()
				.map(entry -> new Task.ParameterComponent(new CodeableConcept(
						new Coding(Constants.NAMINGSYSTEM_HIGHMED_FEASIBILITY, Constants.NAMINGSYSTEM_HIGHMED_FEASIBILITY_VALUE_PREFIX_SINGLE_RESULT
								+ entry.getKey(), null)),
						new StringType(entry.getValue()))).collect(Collectors.toList());

		return outputs.stream();
	}
}
