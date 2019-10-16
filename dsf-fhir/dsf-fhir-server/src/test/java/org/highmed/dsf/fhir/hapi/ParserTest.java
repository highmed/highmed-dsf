package org.highmed.dsf.fhir.hapi;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Organization;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class ParserTest
{
	private static final Logger logger = LoggerFactory.getLogger(ParserTest.class);

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
	public void testParseBundleWithVersion() throws Exception
	{
		String bundleJson = "{\"id\": \"c30abdbe-c605-4c55-be31-2aa7992e754a\", \"meta\": {\"versionId\": \"1\", \"lastUpdated\": \"2019-05-10T12:16:29.532+02:00\"}, \"type\": \"searchset\", \"resourceType\": \"Bundle\"}";
		Bundle bundle = fhirContext.newJsonParser().parseResource(Bundle.class, bundleJson);
		assertNotNull(bundle);
		assertEquals("1", bundle.getMeta().getVersionId());
		// TODO HAPI bug -> null
		// assertEquals("Bundle.id.version", "1", bundle.getIdElement().getVersionIdPart());
		assertNull("Bug in HAPI fixed, if method returns 1", bundle.getIdElement().getVersionIdPart());
		// TODO remove workaround in BundleDaoJdbc#getResource if bug is fixed in HAPI
	}

	// TODO HAPI bug -> StackOverflowError
	// TODO remove workaround in WebserviceClientJersey#read(Class, String)
	// and WebserviceClientJersey#read(Class, String, String)
	@Test(expected = StackOverflowError.class)
	public void testParseBundleWithEntriesWithCircularReferences() throws Exception
	{
		Organization org = new Organization();
		org.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));
		Endpoint ept = new Endpoint();
		ept.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));
		org.addEndpoint().setReference(ept.getIdElement().getIdPart()).setType("Endpoint");
		ept.getManagingOrganization().setReference(org.getIdElement().getIdPart()).setType("Organization");

		Bundle b = new Bundle().setType(BundleType.TRANSACTION);
		b.addEntry().setFullUrl(org.getIdElement().getIdPart()).setResource(org);
		b.addEntry().setFullUrl(ept.getIdElement().getIdPart()).setResource(ept);

		String bString = fhirContext.newXmlParser().setPrettyPrint(false).encodeResourceToString(b);
		if (logger.isDebugEnabled())
			logger.debug(bString);

		Bundle read = configureParser(fhirContext.newXmlParser()).parseResource(Bundle.class, bString);
		configureParser(fhirContext.newXmlParser()).encodeResourceToString(read);
	}

	// TODO HAPI bug -> StackOverflowError
	// TODO remove workaround in WebserviceClientJersey#read(Class, String)
	// and WebserviceClientJersey#read(Class, String, String)
	@Test(expected = StackOverflowError.class)
	public void testParseBundleWithEntriesWithCircularReferencesFile() throws Exception
	{
		try (InputStream in = Files.newInputStream(Paths.get("src/test/resources/bundle.xml")))
		{
			Bundle read = configureParser(fhirContext.newXmlParser()).parseResource(Bundle.class, in);
			configureParser(fhirContext.newXmlParser()).encodeResourceToString(read);
		}
	}

	private IParser configureParser(IParser p)
	{
		p.setStripVersionsFromReferences(false);
		p.setOverrideResourceIdWithBundleEntryFullUrl(false);
		return p;
	}
}
