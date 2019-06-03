package org.highmed.dsf.fhir.service;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class SnapshotGeneratorImpl implements SnapshotGenerator
{
	private static final Logger logger = LoggerFactory.getLogger(SnapshotGeneratorImpl.class);

	private final IWorkerContext worker;

	public SnapshotGeneratorImpl(FhirContext fhirContext, IValidationSupport validationSupport)
	{
		worker = createWorker(fhirContext, validationSupport);
	}

	protected HapiWorkerContext createWorker(FhirContext context, IValidationSupport validationSupport)
	{
		return new HapiWorkerContext(context, validationSupport);
	}

	@Override
	public SnapshotWithValidationMessages generateSnapshot(StructureDefinition differential)
	{
		return generateSnapshot("", differential);
	}

	@Override
	public SnapshotWithValidationMessages generateSnapshot(String baseAbsoluteUrlPrefix,
			StructureDefinition differential)
	{
		logger.debug("Generating snapshot for StructureDefinition with id {}, url {}, version {}",
				differential.getIdElement().getIdPart(), differential.getUrl(), differential.getVersion());

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

		if (messages.isEmpty())
			logger.debug("Snapshot generated for StructureDefinition with id {}, url {}, version {}",
					differential.getIdElement().getIdPart(), differential.getUrl(), differential.getVersion());
		else
		{
			logger.warn("Snapshot not generated for StructureDefinition with id {}, url {}, version {}",
					differential.getIdElement().getIdPart(), differential.getUrl(), differential.getVersion());
			messages.forEach(m -> logger.warn("Issue while generating snapshot: {} - {} - {}", m.getDisplay(),
					m.getLine(), m.getMessage()));
		}

		return new SnapshotWithValidationMessages(differential, messages);
	}
}
