package org.highmed.dsf.fhir.task;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_LEADING_TASK;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TASK;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.fhir.variables.FhirResourceValues;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.hl7.fhir.r4.model.Task.TaskOutputComponent;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.UnsignedIntType;
import org.hl7.fhir.r4.model.UrlType;

public class TaskHelperImpl implements TaskHelper
{
	@Override
	public Optional<String> getFirstInputParameterStringValue(Task task, String system, String code)
	{
		return getInputParameterStringValues(task, system, code).findFirst();
	}

	@Override
	public Stream<String> getInputParameterStringValues(Task task, String system, String code)
	{
		return getInputParameterValues(task, system, code, StringType.class).map(t -> t.asStringValue());
	}

	@Override
	public Optional<Boolean> getFirstInputParameterBooleanValue(Task task, String system, String code)
	{
		return getInputParameterBooleanValues(task, system, code).findFirst();
	}

	@Override
	public Stream<Boolean> getInputParameterBooleanValues(Task task, String system, String code)
	{
		return getInputParameterValues(task, system, code, BooleanType.class).map(t -> t.getValue());
	}

	@Override
	public Optional<Reference> getFirstInputParameterReferenceValue(Task task, String system, String code)
	{
		return getInputParameterReferenceValues(task, system, code).findFirst();
	}

	@Override
	public Stream<Reference> getInputParameterReferenceValues(Task task, String system, String code)
	{
		return getInputParameterValues(task, system, code, Reference.class);
	}

	@Override
	public Optional<UrlType> getFirstInputParameterUrlValue(Task task, String system, String code)
	{
		return getInputParameterUrlValues(task, system, code).findFirst();
	}

	@Override
	public Stream<UrlType> getInputParameterUrlValues(Task task, String system, String code)
	{
		return getInputParameterValues(task, system, code, UrlType.class);
	}

	@Override
	public Optional<byte[]> getFirstInputParameterByteValue(Task task, String system, String code)
	{
		return getInputParameterValues(task, system, code, Base64BinaryType.class).map(Base64BinaryType::getValue)
				.findFirst();
	}

	@Override
	public Stream<ParameterComponent> getInputParameterWithExtension(Task task, String system, String code, String url)
	{
		return task.getInput().stream()
				.filter(input -> input.getType().getCoding().stream()
						.anyMatch(coding -> coding.getSystem().equals(system) && coding.getCode().equals(code)))
				.filter(input -> input.getExtension().stream().anyMatch(extension -> extension.getUrl().equals(url)));
	}

	private <T extends Type> Stream<T> getInputParameterValues(Task task, String system, String code, Class<T> type)
	{
		return task.getInput().stream().filter(c -> type.isInstance(c.getValue()))
				.filter(c -> c.getType().getCoding().stream()
						.anyMatch(co -> system.equals(co.getSystem()) && code.equals(co.getCode())))
				.map(c -> type.cast(c.getValue()));
	}

	@Override
	public ParameterComponent createInput(String system, String code, String value)
	{
		return new ParameterComponent(new CodeableConcept(new Coding(system, code, null)), new StringType(value));
	}

	@Override
	public ParameterComponent createInput(String system, String code, boolean value)
	{
		return new ParameterComponent(new CodeableConcept(new Coding(system, code, null)), new BooleanType(value));
	}

	@Override
	public ParameterComponent createInput(String system, String code, Reference reference)
	{
		return new ParameterComponent(new CodeableConcept(new Coding(system, code, null)), reference);
	}

	@Override
	public ParameterComponent createInput(String system, String code, byte[] bytes)
	{
		return new ParameterComponent(new CodeableConcept(new Coding(system, code, null)), new Base64BinaryType(bytes));
	}

	@Override
	public ParameterComponent createInputUnsignedInt(String system, String code, int value)
	{
		return new ParameterComponent(new CodeableConcept(new Coding(system, code, null)), new UnsignedIntType(value));
	}

	@Override
	public ParameterComponent createInput(String system, String code, int value)
	{
		return new ParameterComponent(new CodeableConcept(new Coding(system, code, null)), new IntegerType(value));
	}

	@Override
	public TaskOutputComponent createOutput(String system, String code, String value)
	{
		return new TaskOutputComponent(new CodeableConcept(new Coding(system, code, null)), new StringType(value));
	}

	@Override
	public TaskOutputComponent createOutputUnsignedInt(String system, String code, int value)
	{
		return new TaskOutputComponent(new CodeableConcept(new Coding(system, code, null)), new UnsignedIntType(value));
	}

	@Override
	public TaskOutputComponent createOutput(String system, String code, Reference reference)
	{
		return new TaskOutputComponent(new CodeableConcept(new Coding(system, code, null)), reference);
	}

	@Override
	public Task getTask(DelegateExecution execution)
	{
		if (execution == null)
			throw new IllegalStateException("execution not started");

		return execution.getParentId() == null || execution.getParentId().equals(execution.getProcessInstanceId())
				? getLeadingTaskFromExecutionVariables(execution)
				: getCurrentTaskFromExecutionVariables(execution);
	}

	@Override
	public Task getCurrentTaskFromExecutionVariables(DelegateExecution execution)
	{
		if (execution == null)
			throw new IllegalStateException("execution not started");

		return (Task) execution.getVariable(BPMN_EXECUTION_VARIABLE_TASK);
	}

	@Override
	public Task getLeadingTaskFromExecutionVariables(DelegateExecution execution)
	{
		if (execution == null)
			throw new IllegalStateException("execution not started");

		Task leadingTask = (Task) execution.getVariable(BPMN_EXECUTION_VARIABLE_LEADING_TASK);
		return leadingTask != null ? leadingTask : getCurrentTaskFromExecutionVariables(execution);
	}

	@Override
	public void updateCurrentTaskInExecutionVariables(DelegateExecution execution, Task task)
	{
		if (execution == null)
			throw new IllegalStateException("execution not started");

		Objects.requireNonNull(task, "task");
		execution.setVariable(BPMN_EXECUTION_VARIABLE_TASK, FhirResourceValues.create(task));
	}

	@Override
	public void updateLeadingTaskInExecutionVariables(DelegateExecution execution, Task task)
	{
		if (execution == null)
			throw new IllegalStateException("execution not started");

		Objects.requireNonNull(task, "task");
		Task leadingTask = (Task) execution.getVariable(BPMN_EXECUTION_VARIABLE_LEADING_TASK);

		if (leadingTask != null)
			execution.setVariable(BPMN_EXECUTION_VARIABLE_LEADING_TASK, FhirResourceValues.create(task));
		else
			updateCurrentTaskInExecutionVariables(execution, task);
	}
}
