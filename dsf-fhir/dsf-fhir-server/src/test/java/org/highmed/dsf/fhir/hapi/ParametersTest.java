package org.highmed.dsf.fhir.hapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StructureDefinition;
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
	public void testParametersWithoutResource() throws Exception
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

	@Test
	public void testParametersWithResource() throws Exception
	{
		final CodeType mode = new CodeType(ResourceValidationMode.CREATE.toCode());
		final UriType uri = new UriType("StructureDefinition/" + UUID.randomUUID().toString());
		final Patient patient = new Patient();
		patient.setId(UUID.randomUUID().toString());

		Parameters parameters = new Parameters();
		parameters.addParameter("mode", mode);
		parameters.addParameter("uri", uri);
		parameters.addParameter().setName("resource").setResource(patient);

		FhirContext context = FhirContext.forR4();
		logger.info("Parameters: {}", context.newXmlParser().encodeResourceToString(parameters));

		assertEquals(mode, parameters.getParameter("mode"));
		assertEquals(uri, parameters.getParameter("uri"));

		Optional<ParametersParameterComponent> resource = parameters.getParameter().stream()
				.filter(p -> "resource".equals(p.getName())).findFirst();
		assertTrue(resource.isPresent());
		assertEquals(patient, resource.get().getResource());
	}

	@Test
	public void testParametersSnapshotOperationInWithResource() throws Exception
	{
		final StructureDefinition sd = new StructureDefinition();
		sd.setUrlElement(new UriType("http://test.com/fhir/StructureDefinition/" + UUID.randomUUID().toString()));

		Parameters parameters = new Parameters();
		parameters.addParameter().setName("resource").setResource(sd);

		FhirContext context = FhirContext.forR4();
		logger.info("Parameters: {}", context.newXmlParser().encodeResourceToString(parameters));

		Optional<ParametersParameterComponent> resource = parameters.getParameter().stream()
				.filter(p -> "resource".equals(p.getName())).findFirst();
		assertTrue(resource.isPresent());
		assertEquals(sd, resource.get().getResource());
	}

	@Test
	public void testParametersSnapshotOperationInWithUrl() throws Exception
	{
		final UriType uri = new UriType(
				"http://test.com/fhir/StructureDefinition/" + UUID.randomUUID().toString() + "|0.0.1");

		Parameters parameters = new Parameters();
		parameters.addParameter().setName("url").setValue(uri);

		FhirContext context = FhirContext.forR4();
		logger.info("Parameters: {}", context.newXmlParser().encodeResourceToString(parameters));

		assertEquals(uri, parameters.getParameter("url"));
	}
}
