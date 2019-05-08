package org.highmed.fhir.task;

import java.util.Optional;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.Task;

public interface TaskHelper
{
	public Optional<String> getFirstInputParameterStringValue(Task task, String system, String code);

	public Stream<String> getInputParameterStringValues(Task task, String system, String code);
}
