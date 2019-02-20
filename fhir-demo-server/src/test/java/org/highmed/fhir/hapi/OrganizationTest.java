package org.highmed.fhir.hapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.codec.binary.Hex;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class OrganizationTest
{
	private static final Logger logger = LoggerFactory.getLogger(OrganizationTest.class);

	@Test
	public void testOrganizationJson() throws Exception
	{
		Organization organization = new Organization();
		Identifier identifier = organization.addIdentifier();
		identifier.setSystem("http://highmed.org/fhir/NamingSystem/certificate-thumbprint-hex");
		identifier.setValue(Hex.encodeHexString("foo bar baz".getBytes()));

		FhirContext context = FhirContext.forR4();
		
		String string = context.newJsonParser().encodeResourceToString(organization);
		logger.info(string);

		Organization parseOrganization = context.newJsonParser().parseResource(Organization.class, string);

		assertNotNull(parseOrganization);
		assertNotNull(parseOrganization.getIdentifierFirstRep());
		assertEquals(organization.getIdentifierFirstRep().getValue(), organization.getIdentifierFirstRep().getValue());
		assertEquals(organization.getIdentifierFirstRep().getSystem(),
				organization.getIdentifierFirstRep().getSystem());
	}
}
