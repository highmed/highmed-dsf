package org.highmed.dsf.fhir.hapi;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;

public class BinaryTest
{
	@Test
	public void testJsonParser() throws Exception
	{
		Binary b1 = new Binary();
		b1.setId(new IdType("Binary", UUID.randomUUID().toString(), "1"));
		b1.getMeta().setLastUpdated(new Date());

		FhirContext context = FhirContext.forR4();

		String b1String = context.newJsonParser().encodeResourceToString(b1);
		System.out.println(b1String);

		Binary b2 = context.newJsonParser().parseResource(Binary.class, b1String);

		System.out.println(b1.getId());
		System.out.println(b2.getId());

		String b2String = context.newJsonParser().encodeResourceToString(b2);
		System.out.println(b2String);

		System.out.println(b1.equalsDeep(b2));
	}

	@Test
	public void testXmlParser() throws Exception
	{
		Binary b1 = new Binary();
		b1.setId(new IdType("Binary", UUID.randomUUID().toString(), "1"));
		b1.getMeta().setLastUpdated(new Date());

		FhirContext context = FhirContext.forR4();

		String b1String = context.newXmlParser().encodeResourceToString(b1);
		System.out.println(b1String);

		Binary b2 = context.newXmlParser().parseResource(Binary.class, b1String);

		System.out.println(b1.getId());
		System.out.println(b2.getId());

		String b2String = context.newXmlParser().encodeResourceToString(b2);
		System.out.println(b2String);

		System.out.println(b1.equalsDeep(b2));
	}

	@Test
	public void testJsonParserP() throws Exception
	{
		Bundle b1 = new Bundle();
		b1.setId(new IdType("Bundle", UUID.randomUUID().toString(), "1"));
		b1.getMeta().setLastUpdated(new Date());

		FhirContext context = FhirContext.forR4();

		String b1String = context.newJsonParser().encodeResourceToString(b1);
		System.out.println(b1String);

		Bundle b2 = context.newJsonParser().parseResource(Bundle.class, b1String);

		System.out.println(b1.getId());
		System.out.println(b2.getId());

		String b2String = context.newJsonParser().encodeResourceToString(b2);
		System.out.println(b2String);

		System.out.println(b1.equalsDeep(b2));
	}
	
	@Test
	public void testJsonParserE() throws Exception
	{
		Endpoint e1 = new Endpoint();
		e1.setId(new IdType("Endpoint", UUID.randomUUID().toString(), "1"));
		e1.getMeta().setLastUpdated(new Date());

		FhirContext context = FhirContext.forR4();

		String e1String = context.newJsonParser().encodeResourceToString(e1);
		System.out.println(e1String);

		Endpoint e2 = context.newJsonParser().parseResource(Endpoint.class, e1String);

		System.out.println(e1.getId());
		System.out.println(e2.getId());

		String e2String = context.newJsonParser().encodeResourceToString(e2);
		System.out.println(e2String);

		System.out.println(e1.equalsDeep(e2));
		
		
			IBaseResource elem = new Patient();
			String resourceName = "";
			String versionId = elem.getMeta().getVersionId();
			if (StringUtils.isBlank(elem.getIdElement().getIdPart())) {
				// Resource has no ID
			} else if (StringUtils.isNotBlank(versionId)) {
				elem.getIdElement().setValue(resourceName + "/" + elem.getIdElement().getIdPart() + "/_history/" + versionId);
			} else {
				elem.getIdElement().setValue(resourceName + "/" + elem.getIdElement().getIdPart());
			}
	}
}
