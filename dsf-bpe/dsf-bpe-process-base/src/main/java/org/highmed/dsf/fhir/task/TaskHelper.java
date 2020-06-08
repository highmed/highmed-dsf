package org.highmed.dsf.fhir.task;

import java.util.Optional;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.variables.Outputs;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.hl7.fhir.r4.model.Task.TaskOutputComponent;
import org.hl7.fhir.r4.model.UrlType;

public interface TaskHelper
{
	Optional<String> getFirstInputParameterStringValue(Task task, String system, String code);

	Stream<String> getInputParameterStringValues(Task task, String system, String code);

	Optional<Boolean> getFirstInputParameterBooleanValue(Task task, String system, String code);

	Stream<Boolean> getInputParameterBooleanValues(Task task, String system, String code);

	Optional<Reference> getFirstInputParameterReferenceValue(Task task, String system, String code);

	Stream<Reference> getInputParameterReferenceValues(Task task, String system, String code);

	Optional<UrlType> getFirstInputParameterUrlValue(Task task, String system, String code);

	Stream<UrlType> getInputParameterUrlValues(Task task, String system, String code);

	Optional<byte[]> getFirstInputParameterByteValue(Task task, String system, String code);

	Stream<ParameterComponent> getInputParameterWithExtension(Task task, String system, String code, String url);

	ParameterComponent createInput(String system, String code, String value);

	ParameterComponent createInput(String system, String code, boolean value);

	ParameterComponent createInput(String system, String code, Reference reference);

	ParameterComponent createInput(String system, String code, byte[] bytes);

	ParameterComponent createInputUnsignedInt(String system, String code, int value);

	ParameterComponent createInput(String system, String code, int value);

	TaskOutputComponent createOutput(String system, String code, String value);

	TaskOutputComponent createOutputUnsignedInt(String system, String code, int value);

	Task addOutputs(Task task, Outputs outputs);
}
