package org.highmed.dsf.fhir.task;

import java.util.Optional;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.UrlType;

public interface TaskHelper
{
	public Optional<String> getFirstInputParameterStringValue(Task task, String system, String code);

	public Stream<String> getInputParameterStringValues(Task task, String system, String code);

	public Optional<Reference> getFirstInputParameterReferenceValue(Task task, String system, String code);

	public Stream<Reference> getInputParameterReferenceValues(Task task, String system, String code);

	public Optional<UrlType> getFirstInputParameterUrlValue(Task task, String system, String code);

	public Stream<UrlType> getInputParameterUrlValues(Task task, String system, String code);
}
