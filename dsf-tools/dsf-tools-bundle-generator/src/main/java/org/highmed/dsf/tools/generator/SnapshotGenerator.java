package org.highmed.dsf.tools.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hl7.fhir.r4.conformance.ProfileUtilities;
import org.hl7.fhir.r4.context.IWorkerContext;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;

public class SnapshotGenerator
{
	private static final Logger logger = LoggerFactory.getLogger(SnapshotGenerator.class);

	private final IWorkerContext worker;

	public SnapshotGenerator(FhirContext fhirContext, IValidationSupport validationSupport)
	{
		worker = createWorker(fhirContext, validationSupport);
	}

	protected HapiWorkerContext createWorker(FhirContext context, IValidationSupport validationSupport)
	{
		HapiWorkerContext workerContext = new HapiWorkerContext(context, validationSupport);
		workerContext.setLocale(context.getLocalizer().getLocale());
		return workerContext;
	}

	public StructureDefinition generateSnapshot(StructureDefinition differential)
	{
		Objects.requireNonNull(differential, "differential");

		StructureDefinition base = worker.fetchResource(StructureDefinition.class, differential.getBaseDefinition());

		if (base == null)
			logger.warn("Base definition with url {} not found", differential.getBaseDefinition());
		else if (!base.hasSnapshot())
			generateSnapshot(base);

		logger.info("Generating snapshot for StructureDefinition with url {}, version {}, base {}",
				differential.getUrl(), differential.getVersion(), differential.getBaseDefinition());

		/* ProfileUtilities is not thread safe */
		List<ValidationMessage> messages = new ArrayList<>();
		ProfileUtilities profileUtils = new ProfileUtilities(worker, messages, null);
		profileUtils.generateSnapshot(base, differential, "", "", null);

		if (messages.isEmpty())
			logger.debug("Snapshot generated for StructureDefinition url {}, version {}", differential.getUrl(),
					differential.getVersion());
		else
		{
			logger.warn("Snapshot not generated for StructureDefinition with url {}, version {}", differential.getUrl(),
					differential.getVersion());
			messages.forEach(m -> logger.warn("Issue while generating snapshot: {} - {} - {}", m.getDisplay(),
					m.getLine(), m.getMessage()));

			throw new RuntimeException("Error while generating Snapshot for StructureDefinition with url "
					+ differential.getUrl() + ", version " + differential.getVersion() + ": " + messages.toString());
		}

		return differential;
	}
}
