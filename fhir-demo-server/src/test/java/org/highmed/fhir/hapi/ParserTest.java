package org.highmed.fhir.hapi;

import static org.junit.Assert.*;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.junit.Ignore;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;

public class ParserTest
{
	private final FhirContext fhirContext = FhirContext.forR4();

	@Test
	public void testParseCodeSystemWithVersion() throws Exception
	{
		String codeSystemJson = "{\"id\": \"72fba613-6a26-4a13-9e97-c1678604e514\", \"meta\": {\"versionId\": \"1\", \"lastUpdated\": \"2019-05-10T12:18:43.912+02:00\"}, \"name\": \"Demo CodeSystem Name\", \"resourceType\": \"CodeSystem\"}";
		CodeSystem codeSystem = fhirContext.newJsonParser().parseResource(CodeSystem.class, codeSystemJson);
		assertNotNull(codeSystem);
		assertEquals("1", codeSystem.getMeta().getVersionId());
		assertEquals("CodeSystem.id.version", "1", codeSystem.getIdElement().getVersionIdPart());
	}

	@Test
	@Ignore ("HAPI bug: bundle.getIdElement().getVersionIdPart() returns null")
	public void testParseBundleWithVersion() throws Exception
	{
		String bundleJson = "{\"id\": \"c30abdbe-c605-4c55-be31-2aa7992e754a\", \"meta\": {\"versionId\": \"1\", \"lastUpdated\": \"2019-05-10T12:16:29.532+02:00\"}, \"type\": \"searchset\", \"resourceType\": \"Bundle\"}";
		Bundle bundle = fhirContext.newJsonParser().parseResource(Bundle.class, bundleJson);
		assertNotNull(bundle);
		assertEquals("1", bundle.getMeta().getVersionId());
		assertEquals("Bundle.id.version", "1", bundle.getIdElement().getVersionIdPart()); // HAPI bug -> null
	}
}
