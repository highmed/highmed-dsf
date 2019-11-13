package org.highmed.dsf.fhir.service;

import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.terminologies.ValueSetExpander.ValueSetExpansionOutcome;

public interface ValueSetExpander
{
	ValueSetExpansionOutcome expand(ValueSet valueSet);
}
