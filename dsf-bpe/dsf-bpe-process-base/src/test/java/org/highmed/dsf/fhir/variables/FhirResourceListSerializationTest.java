package org.highmed.dsf.fhir.variables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.highmed.dsf.fhir.json.ObjectMapperFactory;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Task;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

public class FhirResourceListSerializationTest
{
	private static final Logger logger = LoggerFactory.getLogger(FhirResourceListSerializationTest.class);

	@Test
	public void testEmptyFhirResourceListSerialization() throws Exception
	{
		ObjectMapper mapper = ObjectMapperFactory.createObjectMapper(FhirContext.forR4());

		FhirResourcesList list = new FhirResourcesList();

		String listAsString = mapper.writeValueAsString(list);
		assertNotNull(listAsString);

		logger.debug("Empty fhir resource list json: '{}'", listAsString);

		FhirResourcesList readList = mapper.readValue(listAsString, FhirResourcesList.class);
		assertNotNull(readList);
		assertNotNull(readList.getResources());
		assertNotNull(readList.getResourcesAndCast());
		assertTrue(readList.getResources().isEmpty());
	}

	@Test
	public void testNonEmptyFhirResourceListSerialization() throws Exception
	{
		ObjectMapper mapper = ObjectMapperFactory.createObjectMapper(FhirContext.forR4());

		Task task = new Task();
		Patient patient = new Patient();
		FhirResourcesList list = new FhirResourcesList(task, patient);

		String listAsString = mapper.writeValueAsString(list);
		assertNotNull(listAsString);

		logger.debug("Non empty fhir resource list json: '{}'", listAsString);

		FhirResourcesList readList = mapper.readValue(listAsString, FhirResourcesList.class);
		assertNotNull(readList);
		assertNotNull(readList.getResources());
		assertNotNull(readList.getResourcesAndCast());
		assertEquals(2, readList.getResources().size());
	}
}
