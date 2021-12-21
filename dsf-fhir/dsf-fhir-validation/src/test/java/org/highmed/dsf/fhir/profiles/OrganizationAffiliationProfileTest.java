package org.highmed.dsf.fhir.profiles;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.UUID;

import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.highmed.dsf.fhir.validation.ValidationSupportRule;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;

public class OrganizationAffiliationProfileTest
{
	private static final Logger logger = LoggerFactory.getLogger(OrganizationAffiliationProfileTest.class);

	@ClassRule
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(
			Arrays.asList("highmed-organization-affiliation-0.5.0.xml", "highmed-organization-0.5.0.xml",
					"highmed-organization-parent-0.5.0.xml"),
			Arrays.asList("highmed-read-access-tag-0.5.0.xml", "highmed-organization-role-0.6.0.xml"),
			Arrays.asList("highmed-read-access-tag-0.5.0.xml", "highmed-organization-role-0.6.0.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testOrganizationAffiliationProfileValid() throws Exception
	{
		OrganizationAffiliation a = new OrganizationAffiliation();
		a.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/organization-affiliation");
		a.setActive(true);
		a.getOrganization().setReference("Organization/" + UUID.randomUUID().toString());
		a.getParticipatingOrganization().setReference("Organization/" + UUID.randomUUID().toString());
		a.getCodeFirstRep().getCodingFirstRep().setSystem("http://highmed.org/fhir/CodeSystem/organization-role")
				.setCode("MeDIC");
		a.getEndpointFirstRep().setReference("Endpoint/" + UUID.randomUUID().toString());

		ValidationResult result = resourceValidator.validate(a);
		result.getMessages().stream().map(m -> m.getLocationString() + " " + m.getLocationLine() + ":"
				+ m.getLocationCol() + " - " + m.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}
}
