package org.highmed.dsf.fhir.task;

import java.util.Optional;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.variables.Outputs;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Type;
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

	private <T extends Type> Stream<T> getInputParameterValues(Task task, String system, String code, Class<T> type)
	{
		return task.getInput().stream().filter(c -> type.isInstance(c.getValue()))
				.filter(c -> c.getType().getCoding().stream()
						.anyMatch(co -> system.equals(co.getSystem()) && code.equals(co.getCode())))
				.map(c -> type.cast(c.getValue()));
	}

	@Override
	public Task.ParameterComponent createInput(String system, String code, String value)
	{
		return  new Task.ParameterComponent(new CodeableConcept(
				new Coding(system, code, null)), new StringType(value));
	}

	@Override
	public Task.ParameterComponent createInput(String system, String code, Reference reference)
	{
		return  new Task.ParameterComponent(new CodeableConcept(
				new Coding(system, code, null)), reference);
	}

	@Override
	public Task.TaskOutputComponent createOutput(String system, String code, String value)
	{
		return  new Task.TaskOutputComponent(new CodeableConcept(
				new Coding(system, code, null)), new StringType(value));
	}

	@Override
	public Task addOutputs(Task task, Outputs outputs)
	{
		outputs.getOutputs().forEach(output ->
		{
			Task.TaskOutputComponent component = createOutput(output.getSystem(), output.getCode(), output.getValue());
			task.addOutput(component);
		});

		return task;
	}
}
