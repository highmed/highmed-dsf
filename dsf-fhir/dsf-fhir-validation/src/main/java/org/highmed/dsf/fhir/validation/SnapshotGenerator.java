package org.highmed.dsf.fhir.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.utilities.validation.ValidationMessage;

public interface SnapshotGenerator
{
	public class SnapshotWithValidationMessages
	{
		private final StructureDefinition snapshot;
		private final List<ValidationMessage> messages = new ArrayList<>();

		public SnapshotWithValidationMessages(StructureDefinition snapshot, List<ValidationMessage> messages)
		{
			this.snapshot = snapshot;
			if (messages != null)
				this.messages.addAll(messages);
		}

		public StructureDefinition getSnapshot()
		{
			return snapshot;
		}

		public List<ValidationMessage> getMessages()
		{
			return Collections.unmodifiableList(messages);
		}
	}

	SnapshotWithValidationMessages generateSnapshot(StructureDefinition differential);

	SnapshotWithValidationMessages generateSnapshot(StructureDefinition differential, String baseAbsoluteUrlPrefix);
}