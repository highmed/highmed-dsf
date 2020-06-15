package org.highmed.dsf.tools.generator;

import java.util.Objects;

import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionComponent;
import org.hl7.fhir.r4.terminologies.ValueSetExpander.ValueSetExpansionOutcome;
import org.hl7.fhir.r4.terminologies.ValueSetExpanderSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;

public class ValueSetExpander
{
	private static final Logger logger = LoggerFactory.getLogger(ValueSetExpander.class);

	private final HapiWorkerContext workerContext;

	public ValueSetExpander(FhirContext fhirContext, IValidationSupport validationSupport)
	{
		workerContext = createWorkerContext(fhirContext, validationSupport);
	}

	protected HapiWorkerContext createWorkerContext(FhirContext fhirContext, IValidationSupport validationSupport)
	{
		HapiWorkerContext workerContext = new HapiWorkerContext(fhirContext, validationSupport);
		workerContext.setLocale(fhirContext.getLocalizer().getLocale());
		return workerContext;
	}

	public ValueSet expand(ValueSet valueSet)
	{
		Objects.requireNonNull(valueSet, "valueSet");

		logger.info("Generating expansion for ValueSet url {}, version {}", valueSet.getUrl(), valueSet.getVersion());

		// ValueSetExpanderSimple can't be reused
		ValueSetExpanderSimple valueSetExpander = new ValueSetExpanderSimple(workerContext);

		ValueSetExpansionOutcome outcome = valueSetExpander.expand(valueSet, null);
		if (outcome.getValueset() == null)
			throw new RuntimeException(
					"Error while generating Expansion for ValueSet with url " + valueSet.getUrl() + ", version "
							+ valueSet.getVersion() + ": " + outcome.getError() + " (" + outcome.getErrorClass() + ")");

		ValueSetExpansionComponent expansion = outcome.getValueset().getExpansion();
		valueSet.setExpansion(expansion);
		return valueSet;
	}
}
