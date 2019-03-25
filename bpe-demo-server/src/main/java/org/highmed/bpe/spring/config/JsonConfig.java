package org.highmed.bpe.spring.config;

import org.highmed.fhir.variables.OrganizationDeserializer;
import org.highmed.fhir.variables.OrganizationSerializer;
import org.hl7.fhir.r4.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
public class JsonConfig
{
	@Autowired
	private FhirConfig fhirConfig;

	@Bean
	public ObjectMapper objectMapper()
	{
		ObjectMapper mapper = new ObjectMapper();

		mapper.getFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
		mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.setSerializationInclusion(Include.NON_EMPTY);

		// mapper.enable(SerializationFeature.INDENT_OUTPUT);

		SimpleModule module = new SimpleModule();
		module.addSerializer(Organization.class, new OrganizationSerializer(fhirConfig.fhirContext()));
		module.addDeserializer(Organization.class, new OrganizationDeserializer(fhirConfig.fhirContext()));

		mapper.registerModule(module);

		return mapper;
	}
}
