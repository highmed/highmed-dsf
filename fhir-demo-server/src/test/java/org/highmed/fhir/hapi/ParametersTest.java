package org.highmed.fhir.hapi;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.codesystems.ResourceValidationMode;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class ParametersTest
{
	private static final Logger logger = LoggerFactory.getLogger(ParametersTest.class);

	@Test
	public void testParameters() throws Exception
	{
		final CodeType mode = new CodeType(ResourceValidationMode.CREATE.toCode());
		final UriType uri = new UriType("StructureDefinition/" + UUID.randomUUID().toString());

		Parameters parameters = new Parameters();
		parameters.addParameter("mode", mode);
		parameters.addParameter("uri", uri);

		FhirContext context = FhirContext.forR4();
		logger.info("Parameters: {}", context.newXmlParser().encodeResourceToString(parameters));

		assertEquals(mode, parameters.getParameter("mode"));
		assertEquals(uri, parameters.getParameter("uri"));
	}
}
