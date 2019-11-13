package org.highmed.dsf.fhir.service;

import static org.junit.Assert.*;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.terminologies.ValueSetExpander.ValueSetExpansionOutcome;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class ValueSetExpanderTest
{
	private static final Logger logger = LoggerFactory.getLogger(ValueSetExpanderTest.class);

	@Test
	public void testExpand() throws Exception
	{
		FhirContext context = FhirContext.forR4();
		var validationSupport = new DefaultProfileValidationSupportWithCustomResources();

		CodeSystem cS = new CodeSystem();
		cS.setUrl("http://test/fhir/CodeSystem/foo");
		cS.setVersion("2.0.0");
		cS.addConcept().setCode("Test1");
		cS.addConcept().setCode("Test2");
		cS.addConcept().setCode("Test3");
		cS.addConcept().setCode("Test4");

		validationSupport.addOrReplaceCodeSystem(cS);

		ValueSetExpander expander = new ValueSetExpanderImpl(context, validationSupport);

		ValueSet vS = new ValueSet();
		vS.setUrl("http://test/fhir/ValueSet/foo");
		vS.getCompose().addInclude().setSystem(cS.getUrl()).setVersion("1.0.0");

		ValueSetExpansionOutcome expand = expander.expand(vS);

		assertNotNull(expand);
		assertNull(expand.getError());
		assertNull(expand.getErrorClass());
		assertNotNull(expand.getValueset());

		logger.debug("CodeSystem: {}", context.newXmlParser().setPrettyPrint(true).encodeResourceToString(cS));
		logger.debug("ValueSet: {}", context.newXmlParser().setPrettyPrint(true).encodeResourceToString(vS));
		logger.debug("Expanded ValueSet: {}",
				context.newXmlParser().setPrettyPrint(true).encodeResourceToString(expand.getValueset()));
	}
}
