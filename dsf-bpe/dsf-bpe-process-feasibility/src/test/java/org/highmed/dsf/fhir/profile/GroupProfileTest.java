package org.highmed.dsf.fhir.profile;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_QUERY_TYPE;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_QUERY_TYPE_AQL;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_QUERY_URI;
import static org.highmed.dsf.bpe.variables.ConstantsFeasibility.GROUP_PROFILE;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.highmed.dsf.fhir.validation.ValidationSupportRule;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Group.GroupType;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;

public class GroupProfileTest
{
	private static final Logger logger = LoggerFactory.getLogger(GroupProfileTest.class);

	@ClassRule
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(
			Arrays.asList("highmed-extension-query.xml", "highmed-group.xml"),
			Arrays.asList("authorization-role-0.4.0.xml", "query-type.xml"),
			Arrays.asList("authorization-role-0.4.0.xml", "query-type.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testGroupProfileValid() throws Exception
	{
		Group group = new Group();
		group.getMeta().addProfile(GROUP_PROFILE);
		group.setType(GroupType.PERSON);
		group.setActual(false);
		group.addExtension().setUrl(EXTENSION_QUERY_URI).setValue(new Expression()
				.setLanguageElement(new CodeType(CODESYSTEM_QUERY_TYPE_AQL).setSystem(CODESYSTEM_QUERY_TYPE))
				.setExpression("SELECT COUNT(e) FROM EHR e"));

		ValidationResult result = resourceValidator.validate(group);
		result.getMessages().stream()
				.map(m -> m.getLocationString() + " " + m.getLocationLine() + ":" + m.getLocationCol() + " - " + m
						.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}
}
