package org.highmed.dsf.fhir.profile;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_QUERY_TYPE;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGMED_QUERY_TYPE_VALUE_AQL;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_QUERY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.PROFILE_HIGHMED_GROUP;
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
			Arrays.asList("highmed-authorization-role-0.4.0.xml", "highmed-query-type.xml"),
			Arrays.asList("highmed-authorization-role-0.4.0.xml", "highmed-query-type.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testGroupProfileValid() throws Exception
	{
		Group group = new Group();
		group.getMeta().addProfile(PROFILE_HIGHMED_GROUP);
		group.setType(GroupType.PERSON);
		group.setActual(false);
		group.addExtension().setUrl(EXTENSION_HIGHMED_QUERY).setValue(new Expression().setLanguageElement(
				new CodeType(CODESYSTEM_HIGMED_QUERY_TYPE_VALUE_AQL).setSystem(CODESYSTEM_HIGHMED_QUERY_TYPE))
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
