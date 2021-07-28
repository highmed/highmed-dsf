package org.highmed.dsf.fhir.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceExtractorTest
{
	private static final Logger logger = LoggerFactory.getLogger(ReferenceExtractorTest.class);

	private ReferenceExtractor referenceExtractor;

	@Before
	public void before() throws Exception
	{
		referenceExtractor = new ReferenceExtractorImpl();
	}

	@Test
	public void testExtractTaskReferences() throws Exception
	{
		Extension e0 = new Extension("url0", new Reference("task.ref"));
		Extension e1 = new Extension("url1", new Reference("task.input.extension.ref"));
		Extension e2 = new Extension("url2", new StringType("value"));
		Extension e3 = new Extension("url3", new Reference("task.input.extension.extension.extension.ref"));
		e1.addExtension(e2);
		e2.addExtension(e3);

		Task t = new Task();
		t.addExtension(e0);
		t.addInput().setValue(new Reference("task.input.ref")).addExtension(e1);

		List<ResourceReference> refs = referenceExtractor.getReferences(t).collect(Collectors.toList());

		logger.debug("refs: {}", refs.stream().map(ResourceReference::getLocation).collect(Collectors.toList()));

		assertNotNull(refs);
		assertEquals(4, refs.size());
		assertEquals("task.input.ref", refs.get(0).getReference().getReference());
		assertEquals("task.input.extension.ref", refs.get(1).getReference().getReference());
		assertEquals("task.input.extension.extension.extension.ref", refs.get(2).getReference().getReference());
		assertEquals("task.ref", refs.get(3).getReference().getReference());
	}
}
