package org.highmed.dsf.fhir.service;

import org.hl7.fhir.r4.model.Bundle;

public interface InitialDataLoader
{
	void load(Bundle bundle);
}