package org.highmed.fhir.task;

import java.util.Optional;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;

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
		return task.getInput().stream().filter(c -> c.getValue() instanceof StringType)
				.filter(c -> c.getType().getCoding().stream()
						.anyMatch(co -> system.equals(co.getSystem()) && code.equals(co.getCode())))
				.map(c -> ((StringType) c.getValue()).asStringValue());
	}
}
