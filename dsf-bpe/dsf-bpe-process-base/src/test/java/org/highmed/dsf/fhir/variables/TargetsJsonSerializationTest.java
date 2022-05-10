package org.highmed.dsf.fhir.variables;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.UUID;

import org.highmed.dsf.fhir.json.ObjectMapperFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

public class TargetsJsonSerializationTest
{
	private static final Logger logger = LoggerFactory.getLogger(TargetsJsonSerializationTest.class);

	@Test
	public void testEmptyTargetsSerialization() throws Exception
	{
		ObjectMapper mapper = ObjectMapperFactory.createObjectMapper(FhirContext.forR4());

		Targets targets = new Targets(Collections.emptyList());

		String targetsAsString = mapper.writeValueAsString(targets);
		assertNotNull(targetsAsString);

		logger.debug("Empty targests json: '{}'", targetsAsString);

		Targets readTargets = mapper.readValue(targetsAsString, Targets.class);
		assertNotNull(readTargets);
		assertTrue(readTargets.isEmpty());
	}

	@Test
	public void testTargetsWithBiDirectionalTargetSerialization() throws Exception
	{
		ObjectMapper mapper = ObjectMapperFactory.createObjectMapper(FhirContext.forR4());

		Target target = Target.createBiDirectionalTarget("target.org", "endpoint.target.org",
				"https://endpoint.target.org/fhir", UUID.randomUUID().toString());
		Targets targets = new Targets(Collections.singleton(target));

		String targetsAsString = mapper.writeValueAsString(targets);
		assertNotNull(targetsAsString);

		logger.debug("Targets with bi-directional target json: '{}'", targetsAsString);

		Targets readTargets = mapper.readValue(targetsAsString, Targets.class);
		assertNotNull(readTargets);
		assertFalse(readTargets.isEmpty());
		assertNotNull(readTargets.getEntries());
		assertEquals(1, readTargets.getEntries().size());

		targetEquals(target, readTargets.getEntries().get(0));
	}

	@Test
	public void testTargetsWithUniDirectionalTargetSerialization() throws Exception
	{
		ObjectMapper mapper = ObjectMapperFactory.createObjectMapper(FhirContext.forR4());

		Target target = Target.createUniDirectionalTarget("target.org", "endpoint.target.org",
				"https://endpoint.target.org/fhir");
		Targets targets = new Targets(Collections.singleton(target));

		String targetsAsString = mapper.writeValueAsString(targets);
		assertNotNull(targetsAsString);

		logger.debug("Targets with Uni directional target json: '{}'", targetsAsString);

		Targets readTargets = mapper.readValue(targetsAsString, Targets.class);
		assertNotNull(readTargets);
		assertFalse(readTargets.isEmpty());
		assertNotNull(readTargets.getEntries());
		assertEquals(1, readTargets.getEntries().size());

		targetEquals(target, readTargets.getEntries().get(0));
	}

	private void targetEquals(Target expected, Target actual)
	{
		assertEquals(expected.getCorrelationKey(), actual.getCorrelationKey());
		assertEquals(expected.getEndpointIdentifierValue(), actual.getEndpointIdentifierValue());
		assertEquals(expected.getEndpointUrl(), actual.getEndpointUrl());
		assertEquals(expected.getOrganizationIdentifierValue(), actual.getOrganizationIdentifierValue());
	}

	@Test
	public void testReadOldJson() throws Exception
	{
		ObjectMapper mapper = ObjectMapperFactory.createObjectMapper(FhirContext.forR4());

		String oldJson = "{\"entries\":[{\"targetOrganizationIdentifierValue\":\"target.org\",\"endpointIdentifierValue\":\"endpoint.target.org\",\"targetEndpointUrl\":\"https://endpoint.target.org/fhir\"}]}";

		Targets targets = mapper.readValue(oldJson, Targets.class);
		assertNotNull(targets);
		assertNotNull(targets.getEntries());
		assertEquals(1, targets.getEntries().size());
		assertEquals("target.org", targets.getEntries().get(0).getOrganizationIdentifierValue());
		assertEquals("endpoint.target.org", targets.getEntries().get(0).getEndpointIdentifierValue());
		assertEquals("https://endpoint.target.org/fhir", targets.getEntries().get(0).getEndpointUrl());
	}

	@Test
	public void testReadNewJson() throws Exception
	{
		ObjectMapper mapper = ObjectMapperFactory.createObjectMapper(FhirContext.forR4());

		String oldJson = "{\"entries\":[{\"organizationIdentifierValue\":\"target.org\",\"endpointIdentifierValue\":\"endpoint.target.org\",\"endpointUrl\":\"https://endpoint.target.org/fhir\"}]}";

		Targets targets = mapper.readValue(oldJson, Targets.class);
		assertNotNull(targets);
		assertNotNull(targets.getEntries());
		assertEquals(1, targets.getEntries().size());
		assertEquals("target.org", targets.getEntries().get(0).getOrganizationIdentifierValue());
		assertEquals("endpoint.target.org", targets.getEntries().get(0).getEndpointIdentifierValue());
		assertEquals("https://endpoint.target.org/fhir", targets.getEntries().get(0).getEndpointUrl());
	}
}
