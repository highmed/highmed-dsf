package org.highmed.fhir.service;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.conformance.ProfileUtilities;
import org.hl7.fhir.r4.context.IWorkerContext;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.utilities.validation.ValidationMessage;

import ca.uhn.fhir.context.FhirContext;

public class SnapshotGenerator
{
	public static class SnapshotWithValidationMessages
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

	private final IWorkerContext worker;

	public SnapshotGenerator(FhirContext fhirContext, StructureDefinition... structureDefinitions)
	{
		worker = createWorker(fhirContext, createValidationSupport(fhirContext, structureDefinitions));
	}

	protected HapiWorkerContext createWorker(FhirContext context, IValidationSupport validationSupport)
	{
		return new HapiWorkerContext(context, validationSupport);
	}

	protected IValidationSupport createValidationSupport(FhirContext context,
			StructureDefinition... structureDefinitions)
	{
		return new DefaultProfileValidationSupportWithCustomStructureDefinitions(context, structureDefinitions);
	}

	public SnapshotWithValidationMessages generateSnapshot(StructureDefinition differential)
	{
		return generateSnapshot("", differential);
	}

	public SnapshotWithValidationMessages generateSnapshot(String baseAbsoluteUrlPrefix,
			StructureDefinition differential)
	{
		StructureDefinition base = worker.fetchTypeDefinition(differential.getType());

		/* ProfileUtilities is not thread safe */
		List<ValidationMessage> messages = new ArrayList<>();
		ProfileUtilities profileUtils = new ProfileUtilities(worker, messages, null);

		profileUtils.generateSnapshot(base, differential, baseAbsoluteUrlPrefix, null);

		return new SnapshotWithValidationMessages(differential, messages);
	}
}
