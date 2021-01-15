package org.highmed.dsf.fhir.hapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.apache.commons.codec.binary.Hex;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
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
		Extension thumbprint1 = organization.addExtension();
		thumbprint1.setUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");
		thumbprint1.setValue(new StringType(Hex.encodeHexString("foo bar baz".getBytes())));
		Extension thumbprint2 = organization.addExtension();
		thumbprint2.setUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");
		thumbprint2.setValue(new StringType(Hex.encodeHexString("Blub Blub Blub".getBytes())));
		organization.addEndpoint(new Reference(new IdType("Endpoint", UUID.randomUUID().toString())));

		FhirContext context = FhirContext.forR4();

		String string = context.newXmlParser().setPrettyPrint(true).encodeResourceToString(organization);
		logger.info("Organization:\n{}", string);

		Organization parseOrganization = context.newXmlParser().parseResource(Organization.class, string);

		assertNotNull(parseOrganization);
		assertNotNull(parseOrganization.getIdentifierFirstRep());
		assertEquals(organization.getIdentifierFirstRep().getValue(), organization.getIdentifierFirstRep().getValue());
		assertEquals(organization.getIdentifierFirstRep().getSystem(),
				organization.getIdentifierFirstRep().getSystem());
	}

	@Test
	public void testEndpointReference() throws Exception
	{
		String eId = UUID.randomUUID().toString();
		Endpoint e = new Endpoint();
		e.setIdElement(new IdType("Endpoint", eId, "version"));

		Organization o = new Organization();
		o.addEndpoint().setReferenceElement(new IdType(e.getIdElement().getResourceType(), e.getIdElement().getIdPart(),
				e.getIdElement().getVersionIdPart()));

		FhirContext context = FhirContext.forR4();
		String string = context.newXmlParser().setPrettyPrint(true).encodeResourceToString(o);

		logger.info(string);
	}
}
