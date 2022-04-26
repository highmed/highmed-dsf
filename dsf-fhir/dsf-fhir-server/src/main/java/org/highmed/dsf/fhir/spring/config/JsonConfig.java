package org.highmed.dsf.fhir.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Configuration
public class JsonConfig
{
	@Bean
	public ObjectMapper objectMapper()
	{
		JsonMapper jsonMapper = JsonMapper.builder().disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
				.serializationInclusion(Include.NON_NULL).serializationInclusion(Include.NON_EMPTY)
				.disable(Feature.AUTO_CLOSE_TARGET).build();

		return jsonMapper;
	}
}
