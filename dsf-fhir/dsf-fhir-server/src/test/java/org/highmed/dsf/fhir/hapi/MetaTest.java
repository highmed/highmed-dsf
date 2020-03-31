package org.highmed.dsf.fhir.hapi;

import static org.junit.Assert.*;

import org.hl7.fhir.r4.model.CodeSystem;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class MetaTest
{
	private static final Logger logger = LoggerFactory.getLogger(MetaTest.class);

	@Test
	public void testMetaTag() throws Exception
	{
		CodeSystem c = new CodeSystem();
		c.getMeta().addTag().setSystem("http://system.com/foo").setCode("TAG_CODE");

		FhirContext context = FhirContext.forR4();
		String string = context.newJsonParser().encodeResourceToString(c);

		logger.info(string);

		CodeSystem c2 = context.newJsonParser().parseResource(CodeSystem.class, string);
		assertTrue(c2.hasMeta());
		assertTrue(c2.getMeta().hasTag());
		assertEquals(1, c2.getMeta().getTag().size());
		assertEquals(c.getMeta().getTagFirstRep().getSystem(), c2.getMeta().getTagFirstRep().getSystem());
	}
}
