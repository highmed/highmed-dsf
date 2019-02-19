package org.highmed.fhir.service;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.exceptions.DefinitionException;
import org.hl7.fhir.r4.conformance.ProfileUtilities;
import org.hl7.fhir.r4.context.IWorkerContext;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureDefinition.StructureDefinitionMappingComponent;
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

	public SnapshotGenerator(FhirContext fhirContext, IValidationSupport validationSupport)
	{
		worker = createWorker(fhirContext, validationSupport);
	}

	protected HapiWorkerContext createWorker(FhirContext context, IValidationSupport validationSupport)
	{
		return new HapiWorkerContext(context, validationSupport);
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
		ProfileUtilities profileUtils = new ProfileUtilities(worker, messages, null)
		{
			@Override
			public void updateMaps(StructureDefinition base, StructureDefinition derived) throws DefinitionException
			{
				if (base == null)
					throw new DefinitionException("no base profile provided");
				if (derived == null)
					throw new DefinitionException("no derived structure provided");

				for (StructureDefinitionMappingComponent baseMap : base.getMapping())
				{
					boolean found = false;
					for (StructureDefinitionMappingComponent derivedMap : derived.getMapping())
					{
						/*
						 * XXX NullPointerException if mapping.uri is null, see original if statement:
						 * 
						 * if (derivedMap.getUri().equals(baseMap.getUri()))
						 * 
						 * NPE fix by checking getUri != null
						 * 
						 * also fixes missing name based matching, via specification rule: StructureDefinition.mapping
						 * "Must have at least a name or a uri (or both)"
						 */
						if ((derivedMap.getUri() != null && derivedMap.getUri().equals(baseMap.getUri()))
								|| (derivedMap.getName() != null && derivedMap.getName().equals(baseMap.getName())))
						{
							found = true;
							break;
						}
					}
					if (!found)
						derived.getMapping().add(baseMap);
				}
			}
		};

		profileUtils.generateSnapshot(base, differential, baseAbsoluteUrlPrefix, null);

		return new SnapshotWithValidationMessages(differential, messages);
	}
}
