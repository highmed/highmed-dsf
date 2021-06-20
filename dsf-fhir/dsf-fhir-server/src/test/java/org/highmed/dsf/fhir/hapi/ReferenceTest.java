package org.highmed.dsf.fhir.hapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.hl7.fhir.r4.model.Endpoint;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class ReferenceTest
{
	private static final Logger logger = LoggerFactory.getLogger(ReferenceTest.class);

	@Test
	public void testLiteralAndLogicalReference() throws Exception
	{
		Endpoint endpoint = new Endpoint();
		endpoint.getManagingOrganization().setReference("Organization/" + UUID.randomUUID().toString()).getIdentifier()
				.setSystem("http://highmed.org/sid/organization-identifier").setValue("hs-heilbronn.de");

		FhirContext context = FhirContext.forR4();
		String endpointString = context.newXmlParser().setPrettyPrint(true).encodeResourceToString(endpoint);

		logger.debug("Endpoint: {}", endpointString);

		Endpoint readEndpoint = context.newXmlParser().parseResource(Endpoint.class, endpointString);

		assertNotNull(readEndpoint);
		assertEquals(endpoint.getManagingOrganization().getReference(),
				readEndpoint.getManagingOrganization().getReference());
		assertEquals(endpoint.getManagingOrganization().getIdentifier().getSystem(),
				readEndpoint.getManagingOrganization().getIdentifier().getSystem());
		assertEquals(endpoint.getManagingOrganization().getIdentifier().getValue(),
				readEndpoint.getManagingOrganization().getIdentifier().getValue());
	}
}
