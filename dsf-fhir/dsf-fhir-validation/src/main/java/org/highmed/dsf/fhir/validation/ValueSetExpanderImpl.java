package org.highmed.dsf.fhir.validation;

import java.util.Objects;

import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.terminologies.ValueSetExpander.ValueSetExpansionOutcome;
import org.hl7.fhir.r4.terminologies.ValueSetExpanderSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;

public class ValueSetExpanderImpl implements ValueSetExpander
{
	private static final Logger logger = LoggerFactory.getLogger(ValueSetExpanderImpl.class);

	private final HapiWorkerContext workerContext;

	public ValueSetExpanderImpl(FhirContext fhirContext, IValidationSupport validationSupport)
	{
		workerContext = createWorkerContext(fhirContext, validationSupport);
	}

	protected HapiWorkerContext createWorkerContext(FhirContext fhirContext, IValidationSupport validationSupport)
	{
		HapiWorkerContext workerContext = new HapiWorkerContext(fhirContext, validationSupport);
		workerContext.setLocale(fhirContext.getLocalizer().getLocale());
		return workerContext;
	}

	public ValueSetExpansionOutcome expand(ValueSet valueSet)
	{
		Objects.requireNonNull(valueSet, "valueSet");

		// ValueSetExpanderSimple can't be reused
		ValueSetExpanderSimple valueSetExpander = new ValueSetExpanderSimple(workerContext);

		logger.debug("Generating expansion for ValueSet with id {}, url {}, version {}",
				valueSet.getIdElement().getIdPart(), valueSet.getUrl(), valueSet.getVersion());

		return valueSetExpander.expand(valueSet, null);
	}
}
