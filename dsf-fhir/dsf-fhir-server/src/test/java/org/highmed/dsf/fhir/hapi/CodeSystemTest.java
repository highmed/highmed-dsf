package org.highmed.dsf.fhir.hapi;

import java.util.Date;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemHierarchyMeaning;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class CodeSystemTest
{
	private static final Logger logger = LoggerFactory.getLogger(CodeSystemTest.class);

	private static FhirContext fhirContext = FhirContext.forR4();

	@Test
	public void testCodeSystem() throws Exception
	{
		CodeSystem codeSystem = new CodeSystem();
		codeSystem.setUrl("http://highmed.org/fhir/CodeSystem/organization-role");
		codeSystem.setVersion("0.1.0");
		codeSystem.setName("HiGHmed_Organization_Type");
		codeSystem.setTitle("HiGHmed Organization Type");
		codeSystem.setStatus(PublicationStatus.ACTIVE);
		codeSystem.setExperimental(false);
		codeSystem.setDate(new Date());
		codeSystem.setPublisher("HiGHmed");
		codeSystem.setCaseSensitive(true);
		codeSystem.setContent(CodeSystemContentMode.COMPLETE);
		codeSystem.setVersionNeeded(false);
		codeSystem.setHierarchyMeaning(CodeSystemHierarchyMeaning.GROUPEDBY);
		ConceptDefinitionComponent c1 = codeSystem.addConcept();
		c1.setCode("TTP");
		c1.setDefinition("Trusted Third Party");
		ConceptDefinitionComponent c2 = codeSystem.addConcept();
		c2.setCode("MeDIC");
		c2.setDefinition("Medical Data Integration Center");

		String s = fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(codeSystem);
		logger.debug(s);
	}
}
