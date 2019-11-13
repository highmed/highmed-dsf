package org.highmed.dsf.fhir.service;

import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.terminologies.ValueSetExpander.ValueSetExpansionOutcome;

import ca.uhn.fhir.context.FhirContext;

public class ValueSetExpanderImpl implements ValueSetExpander
{
	private final HapiWorkerContext worker;

	public ValueSetExpanderImpl(FhirContext fhirContext, IValidationSupport validationSupport)
	{
		worker = createWorker(fhirContext, validationSupport);
	}

	protected HapiWorkerContext createWorker(FhirContext context, IValidationSupport validationSupport)
	{
		return new HapiWorkerContext(context, validationSupport);
	}

	public ValueSetExpansionOutcome expand(ValueSet valueSet)
	{
		return worker.expand(valueSet, null);
	}
}
