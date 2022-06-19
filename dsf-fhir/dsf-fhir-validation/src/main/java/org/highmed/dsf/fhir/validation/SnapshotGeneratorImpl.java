package org.highmed.dsf.fhir.validation;

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

public class SnapshotGeneratorImpl implements SnapshotGenerator
{
	private static final Logger logger = LoggerFactory.getLogger(SnapshotGeneratorImpl.class);

	private final IWorkerContext worker;

	public SnapshotGeneratorImpl(FhirContext fhirContext, IValidationSupport validationSupport)
	{
		worker = createWorker(fhirContext, validationSupport);
	}

	protected IWorkerContext createWorker(FhirContext context, IValidationSupport validationSupport)
	{
		HapiWorkerContext workerContext = new HapiWorkerContext(context, validationSupport);
		workerContext.setLocale(context.getLocalizer().getLocale());
		return workerContext;
	}

	@Override
	public SnapshotWithValidationMessages generateSnapshot(StructureDefinition differential)
	{
		return generateSnapshot(differential, "");
	}

	@Override
	public SnapshotWithValidationMessages generateSnapshot(StructureDefinition differential,
			String baseAbsoluteUrlPrefix)
	{
		Objects.requireNonNull(differential, "differential");

		logger.debug("Generating snapshot for StructureDefinition with id {}, url {}, version {}, base {}",
				differential.getIdElement().getIdPart(), differential.getUrl(), differential.getVersion(),
				differential.getBaseDefinition());

		StructureDefinition base = worker.fetchResource(StructureDefinition.class, differential.getBaseDefinition());

		if (base == null)
			logger.warn("Base definition with url {} not found", differential.getBaseDefinition());

		/* ProfileUtilities is not thread safe */
		List<ValidationMessage> messages = new ArrayList<>();
		ProfileUtilities profileUtils = new ProfileUtilities(worker, messages, null);

		profileUtils.generateSnapshot(base, differential, baseAbsoluteUrlPrefix, baseAbsoluteUrlPrefix, null);

		if (messages.isEmpty())
			logger.debug("Snapshot generated for StructureDefinition with id {}, url {}, version {}",
					differential.getIdElement().getIdPart(), differential.getUrl(), differential.getVersion());
		else
		{
			logger.warn("Snapshot generated with issues for StructureDefinition with id {}, url {}, version {}",
					differential.getIdElement().getIdPart(), differential.getUrl(), differential.getVersion());
			messages.forEach(m -> logger.warn("Issue while generating snapshot: {} - {} - {}", m.getDisplay(),
					m.getLine(), m.getMessage()));
		}

		return new SnapshotWithValidationMessages(differential, messages);
	}
}
