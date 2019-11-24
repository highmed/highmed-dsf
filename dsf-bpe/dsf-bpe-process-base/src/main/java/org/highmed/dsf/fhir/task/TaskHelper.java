package org.highmed.dsf.fhir.task;

import java.util.Optional;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.variables.Outputs;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.UrlType;

public interface TaskHelper
{
	Optional<String> getFirstInputParameterStringValue(Task task, String system, String code);

	Stream<String> getInputParameterStringValues(Task task, String system, String code);

	Optional<Reference> getFirstInputParameterReferenceValue(Task task, String system, String code);

	Stream<Reference> getInputParameterReferenceValues(Task task, String system, String code);

	Optional<UrlType> getFirstInputParameterUrlValue(Task task, String system, String code);

	Stream<UrlType> getInputParameterUrlValues(Task task, String system, String code);

	Task.ParameterComponent createInput(String system, String code, String value);

	Task.ParameterComponent createInput(String system, String code, Reference reference);

	Task.TaskOutputComponent createOutput(String system, String code, String value);

	Task addOutputs(Task task, Outputs outputs);
}
