package org.highmed.dsf.fhir.hapi;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class ResearchStudyTest
{
	private static final Logger logger = LoggerFactory.getLogger(ResearchStudyTest.class);

	@Test
	public void testReferenceExtension() throws Exception
	{
		ResearchStudy r = new ResearchStudy();
		r.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/extension-participating-medic")
				.setValue(new Reference().setReference("Organization/" + UUID.randomUUID().toString()));

		logger.debug(FhirContext.forR4().newXmlParser().setPrettyPrint(true).encodeResourceToString(r));

		assertTrue(r.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/extension-participating-medic")
				.getValue() instanceof Reference);
	}
}
