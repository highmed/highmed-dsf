package org.highmed.dsf.fhir.hapi;

import static org.junit.Assert.*;

import java.util.UUID;

import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class TaskTest
{
	private static final Logger logger = LoggerFactory.getLogger(TaskTest.class);

	@Test
	public void testRequester() throws Exception
	{
		Task t = new Task();
		t.setRequester(new Reference("Organization/" + UUID.randomUUID().toString()));

		FhirContext context = FhirContext.forR4();

		String txt = context.newJsonParser().setPrettyPrint(true).encodeResourceToString(t);
		assertNotNull(txt);
		logger.debug(txt);
	}

	@Test
	public void testRestrictionRecipient() throws Exception
	{
		Task t = new Task();
		t.getRestriction().addRecipient(new Reference("Organization/" + UUID.randomUUID().toString()));

		FhirContext context = FhirContext.forR4();

		String txt = context.newJsonParser().setPrettyPrint(true).encodeResourceToString(t);
		assertNotNull(txt);
		logger.debug(txt);
	}
}
