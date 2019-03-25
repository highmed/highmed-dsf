package org.highmed.fhir.variables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Organization;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import ca.uhn.fhir.context.FhirContext;

public class MultiInstanceTargetTest
{
	private static final Logger logger = LoggerFactory.getLogger(MultiInstanceTargetTest.class);

	@Test
	public void testJson() throws Exception
	{
		final ObjectMapper objectMapper = objectMapper();

		Organization targetOrganization = new Organization();
		targetOrganization.setName("targetOrganization");
		targetOrganization.setIdElement(new IdType("Organiaztion", UUID.randomUUID().toString(), "1"));
		MultiInstanceTarget target = new MultiInstanceTarget(targetOrganization, UUID.randomUUID().toString());

		String targetOrganizationAsString = objectMapper.writeValueAsString(targetOrganization);
		logger.info("TargetOrganization as String: {}", targetOrganizationAsString);

		String targetAsString = objectMapper.writeValueAsString(target);

		logger.info("Target as String: {}", targetAsString);

		MultiInstanceTarget readTarget = objectMapper.readValue(targetAsString, MultiInstanceTarget.class);

		assertNotNull(readTarget);
		assertNotNull(readTarget.getCorrelationKey());
		assertNotNull(readTarget.getTargetOrganization());
		assertEquals(target.getCorrelationKey(), readTarget.getCorrelationKey());
		assertEquals(target.getTargetOrganization().getName(), readTarget.getTargetOrganization().getName());
	}

	private ObjectMapper objectMapper()
	{
		FhirContext fhirContext = FhirContext.forR4();

		ObjectMapper mapper = new ObjectMapper();

		mapper.getFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
		mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.setSerializationInclusion(Include.NON_EMPTY);

		// mapper.enable(SerializationFeature.INDENT_OUTPUT);

		SimpleModule module = new SimpleModule();
		module.addSerializer(Organization.class, new OrganizationSerializer(fhirContext));
		module.addDeserializer(Organization.class, new OrganizationDeserializer(fhirContext));

		mapper.registerModule(module);

		return mapper;
	}
}
