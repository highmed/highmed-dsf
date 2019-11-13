package org.highmed.dsf.fhir.hapi;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class EndpointTest
{
	private static final Logger logger = LoggerFactory.getLogger(EndpointTest.class);

	@Test
	public void testEndpointXml() throws Exception
	{
		FhirContext context = FhirContext.forR4();

		Endpoint endpoint = new Endpoint();
		endpoint.setManagingOrganization(new Reference(new IdType("Organization", UUID.randomUUID().toString())));

		String str = context.newJsonParser().setPrettyPrint(true).encodeResourceToString(endpoint);

		logger.info("Endpoint:\n{}", str);

		Endpoint read = context.newJsonParser().parseResource(Endpoint.class, str);

		assertEquals(endpoint.getManagingOrganization().getReference(), read.getManagingOrganization().getReference());
	}
}
