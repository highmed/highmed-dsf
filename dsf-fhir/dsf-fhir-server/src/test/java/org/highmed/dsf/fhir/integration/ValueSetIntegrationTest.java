package org.highmed.dsf.fhir.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.text.SimpleDateFormat;
import java.util.UUID;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueSetIntegrationTest extends AbstractIntegrationTest
{
	private static final Logger logger = LoggerFactory.getLogger(ValueSetIntegrationTest.class);

	private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");

	@Test
	public void testDeleteAndCreateInOneTransactionBundle() throws Exception
	{
		ValueSet v = new ValueSet();
		v.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ALL");
		v.setUrl("http://foo.bar/fhir/baz");
		v.setVersion("1.0.0");
		v.setDate(DF.parse("1010-01-01"));
		v.setStatus(PublicationStatus.DRAFT);

		ValueSet created = getWebserviceClient().create(v);
		assertNotNull(created);

		Bundle b = new Bundle();
		b.setType(BundleType.TRANSACTION);
		b.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).getRequest().setMethod(HTTPVerb.DELETE)
				.setUrl("ValueSet?url=" + v.getUrl() + "&version=" + v.getVersion() + "&date=eq"
						+ DF.format(v.getDate()));

		v.setDate(DF.parse("2020-02-02"));
		b.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(v).getRequest()
				.setMethod(HTTPVerb.POST).setUrl("ValueSet")
				.setIfNoneExist("url=" + v.getUrl() + "&version=" + v.getVersion());

		logger.debug("Post bundle: {}", fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(b));

		Bundle returnBundle = getWebserviceClient().postBundle(b);

		logger.debug("Return bundle: {}",
				fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(returnBundle));

		assertNotNull(returnBundle);
		assertEquals(2, returnBundle.getEntry().size());
		assertNotSame(created.getIdElement().getIdPart(),
				returnBundle.getEntry().get(1).getResource().getIdElement().getIdPart());
	}
}
