package org.highmed.dsf.fhir.hapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Organization;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class BundleTest
{
	private static final Logger logger = LoggerFactory.getLogger(BundleTest.class);

	private IParser newXmlParser()
	{
		IParser p = FhirContext.forR4().newXmlParser();
		p.setStripVersionsFromReferences(false);
		p.setOverrideResourceIdWithBundleEntryFullUrl(false);
		return p;
	}

	private IParser newJsonParser()
	{
		IParser p = FhirContext.forR4().newJsonParser();
		p.setStripVersionsFromReferences(false);
		p.setOverrideResourceIdWithBundleEntryFullUrl(false);
		return p;
	}

	@Test
	public void testBundleXml() throws Exception
	{
		IParser parser = newXmlParser();

		testBundleWithParser(parser);
	}

	@Test
	public void testBundleJson() throws Exception
	{
		IParser parser = newJsonParser();

		testBundleWithParser(parser);
	}

	private void testBundleWithParser(IParser parser)
	{
		Bundle bundle1 = new Bundle();
		bundle1.setType(BundleType.TRANSACTION);

		String orgTempId = "urn:uuid:" + UUID.randomUUID().toString();
		String eptTempId = "urn:uuid:" + UUID.randomUUID().toString();

		Organization org = new Organization();
		org.addIdentifier().setSystem("http://highmed.org/sid/organization-identifier").setValue("Test_Organization");

		Endpoint ept = new Endpoint();
		ept.addIdentifier().setSystem("http://highmed.org/sid/endpoint-identifier").setValue("Test_Endpoint");

		org.getEndpointFirstRep().setType("Endpoint").setReference(eptTempId);
		ept.getManagingOrganization().setType("Organization").setReference(orgTempId);

		BundleEntryComponent orgEntry = bundle1.addEntry();
		orgEntry.setFullUrl(orgTempId);
		orgEntry.setResource(org);
		orgEntry.getRequest().setMethod(HTTPVerb.PUT)
				.setUrl("Organization?identifier=http://highmed.org/sid/organization-identifier|Test_Organization");

		BundleEntryComponent eptEntry = bundle1.addEntry();
		eptEntry.setFullUrl(eptTempId);
		eptEntry.setResource(ept);
		eptEntry.getRequest().setMethod(HTTPVerb.PUT)
				.setUrl("Endpoint?identifier=http://highmed.org/sid/endpoint-identifier|Test_Endpoint");

		String bundle1String = parser.encodeResourceToString(bundle1);
		logger.debug("Bundle1: {}", bundle1String);

		Bundle bundle2 = parser.parseResource(Bundle.class, bundle1String);

		assertTrue(bundle2.getEntry().get(0).getResource() instanceof Organization);
		assertNotNull(((Organization) bundle2.getEntry().get(0).getResource()).getEndpointFirstRep().getResource());

		// FIXME workaround hapi parser bug
		((Organization) bundle2.getEntry().get(0).getResource()).getEndpointFirstRep().setResource(null);

		assertTrue(bundle2.getEntry().get(1).getResource() instanceof Endpoint);
		assertNotNull(((Endpoint) bundle2.getEntry().get(1).getResource()).getManagingOrganization().getResource());

		// FIXME workaround hapi parser bug
		((Endpoint) bundle2.getEntry().get(1).getResource()).getManagingOrganization().setResource(null);

		String bundle2String = parser.encodeResourceToString(bundle2);
		logger.debug("Bundle2: {}", bundle2String);

		assertEquals(bundle1String, bundle2String);
	}

	@Test
	public void testBundleVersionTag() throws Exception
	{
		IdType i = new IdType(null, "id", "version");
		System.out.println(i.withResourceType("Bundle").getValueAsString());

		Bundle b = new Bundle();
		b.setIdElement(new IdType("Bundle", UUID.randomUUID().toString(), "123"));

		String bundleTxt = newXmlParser().encodeResourceToString(b);
		logger.debug(bundleTxt);

		Bundle bRead = newXmlParser().parseResource(Bundle.class, bundleTxt);
		assertEquals("123", bRead.getMeta().getVersionId());
		assertNull(bRead.getIdElement().getVersionIdPart());

		// FIXME workaround hapi parser bug
		bRead.setIdElement(bRead.getIdElement().withVersion(bRead.getMeta().getVersionId()));
		assertEquals("123", bRead.getIdElement().getVersionIdPart());
	}
}
