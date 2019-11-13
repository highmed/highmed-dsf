package org.highmed.dsf.fhir.hapi;

import static org.junit.Assert.*;

import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class SerializationTest
{
	@Test
	public void testSerializationReferenceWithVersionJson() throws Exception
	{
		FhirContext context = FhirContext.forR4();

		Reference ref = new Reference("http://foo.bar/fhir/Organization/id/_history/vid");
		Task task = new Task().setRequester(ref);
		IParser parser = context.newJsonParser();

		// versions will be striped unless we set stripVersionsFromReferences to false
		parser.setStripVersionsFromReferences(false);

		String json = parser.encodeResourceToString(task);
		Task task2 = parser.parseResource(Task.class, json);

		assertEquals(task.getRequester().getReference(), task2.getRequester().getReference());
	}

	@Test
	public void testSerializationReferenceWithVersionXml() throws Exception
	{
		FhirContext context = FhirContext.forR4();

		Reference ref = new Reference("http://foo.bar/fhir/Organization/id/_history/vid");
		Task task = new Task().setRequester(ref);
		IParser parser = context.newXmlParser();

		// versions will be striped unless we set stripVersionsFromReferences to false
		parser.setStripVersionsFromReferences(false);

		String json = parser.encodeResourceToString(task);
		Task task2 = parser.parseResource(Task.class, json);

		assertEquals(task.getRequester().getReference(), task2.getRequester().getReference());
	}

	@Test
	public void testSerializationReferenceDifferentServerJson() throws Exception
	{
		FhirContext context = FhirContext.forR4();

		Reference ref = new Reference("http://foo.bar/fhir/Organization/id");
		Task task = new Task().setRequester(ref);
		IParser parser = context.newJsonParser();
		parser.setServerBaseUrl("http://baz.bar/fhir");

		String json = parser.encodeResourceToString(task);
		Task task2 = parser.parseResource(Task.class, json);

		assertEquals(task.getRequester().getReference(), task2.getRequester().getReference());
	}

	@Test
	public void testSerializationReferenceSameServerJson() throws Exception
	{
		FhirContext context = FhirContext.forR4();

		Reference ref = new Reference("http://foo.bar/fhir/Organization/id");
		Task task = new Task().setRequester(ref);
		IParser parser = context.newJsonParser();
		parser.setServerBaseUrl("http://foo.bar/fhir");

		String json = parser.encodeResourceToString(task);
		Task task2 = parser.parseResource(Task.class, json);

		assertEquals(task.getRequester().getReference(), "http://foo.bar/fhir/" + task2.getRequester().getReference());
	}
}
