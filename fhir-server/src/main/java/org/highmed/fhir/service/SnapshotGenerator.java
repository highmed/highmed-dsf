package org.highmed.fhir.service;

import java.util.List;

import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.utilities.validation.ValidationMessage;

public interface SnapshotGenerator
{
	class SnapshotWithValidationMessages
	{
		private final StructureDefinition snapshot;
		private final List<ValidationMessage> messages;

		SnapshotWithValidationMessages(StructureDefinition snapshot, List<ValidationMessage> messages)
		{
			this.snapshot = snapshot;
			this.messages = messages;
		}

		public StructureDefinition getSnapshot()
		{
			return snapshot;
		}

		public List<ValidationMessage> getMessages()
		{
			return messages;
		}
	}
	
	SnapshotWithValidationMessages generateSnapshot(StructureDefinition differential);

	SnapshotWithValidationMessages generateSnapshot(String baseAbsoluteUrlPrefix, StructureDefinition differential);
}