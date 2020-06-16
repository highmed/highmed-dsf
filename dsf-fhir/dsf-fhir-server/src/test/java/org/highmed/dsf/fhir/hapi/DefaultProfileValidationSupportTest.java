package org.highmed.dsf.fhir.hapi;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;

public class DefaultProfileValidationSupportTest
{
	@Test(expected = NullPointerException.class)
	public void testFetchAllBugInHapi() throws Exception
	{
		// XXX bug in HAPI framework
		new DefaultProfileValidationSupport(FhirContext.forR4()).fetchAllConformanceResources();
	}

	@Test
	public void testFetchAllBugInHapiWorkaround() throws Exception
	{
		// XXX bug in HAPI framework workaround
		IValidationSupport support = new DefaultProfileValidationSupport(FhirContext.forR4());
		assertNull(support.fetchCodeSystem(""));
		assertNotNull(support.fetchAllStructureDefinitions());
		assertNotNull(support.fetchAllConformanceResources());
	}
}
