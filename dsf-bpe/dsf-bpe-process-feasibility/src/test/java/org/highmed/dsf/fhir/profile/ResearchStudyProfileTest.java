package org.highmed.dsf.fhir.profile;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.UUID;

import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.highmed.dsf.fhir.validation.ValidationSupportRule;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.ResearchStudy.ResearchStudyStatus;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;

public class ResearchStudyProfileTest
{
	private static final Logger logger = LoggerFactory.getLogger(ResearchStudyProfileTest.class);

	@ClassRule
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(
			Arrays.asList("highmed-extension-participating-medic-0.3.0.xml",
					"highmed-extension-participating-ttp-0.3.0.xml", "highmed-research-study-feasibility-0.3.0.xml"),
			Arrays.asList("authorization-role-0.3.0.xml", "organization-type-0.3.0.xml"),
			Arrays.asList("authorization-role-0.3.0.xml", "organization-type-0.3.0.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testResearchStudyProfileValid() throws Exception
	{
		ResearchStudy res = new ResearchStudy();
		res.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-research-study-feasibility");
		res.getIdentifierFirstRep().setSystem("http://highmed.org/fhir/NamingSystem/research-study-identifier")
				.setValue(UUID.randomUUID().toString());
		res.setStatus(ResearchStudyStatus.ACTIVE);
		res.addEnrollment().setReference("Group/" + UUID.randomUUID().toString());
		Reference medicRef1 = new Reference().setType("Organization");
		medicRef1.getIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
				.setValue("MeDIC 1");
		res.addExtension("http://highmed.org/fhir/StructureDefinition/participating-medic", medicRef1);
		Reference medicRef2 = new Reference().setType("Organization");
		medicRef2.getIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
				.setValue("MeDIC 2");
		res.addExtension("http://highmed.org/fhir/StructureDefinition/participating-medic", medicRef2);
		Reference ttpRef = new Reference().setType("Organization");
		ttpRef.getIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
				.setValue("TTP");
		res.addExtension("http://highmed.org/fhir/StructureDefinition/participating-ttp", ttpRef);

		ValidationResult result = resourceValidator.validate(res);
		result.getMessages().stream().map(m -> m.getLocationString() + " " + m.getLocationLine() + ":"
				+ m.getLocationCol() + " - " + m.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}
}
