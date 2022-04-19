package org.highmed.dsf.bpe.spring.config;

import org.highmed.dsf.fhir.json.ObjectMapperFactory;
import org.highmed.dsf.fhir.variables.FhirResourceSerializer;
import org.highmed.dsf.fhir.variables.FhirResourcesListSerializer;
import org.highmed.dsf.fhir.variables.TargetSerializer;
import org.highmed.dsf.fhir.variables.TargetsSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class SerializerConfig
{
	@Autowired
	private FhirContext fhirContext;

	@Bean
	public ObjectMapper objectMapper()
	{
		return ObjectMapperFactory.createObjectMapper(fhirContext);
	}

	@Bean
	public FhirResourceSerializer fhirResourceSerializer()
	{
		return new FhirResourceSerializer(fhirContext);
	}

	@Bean
	public FhirResourcesListSerializer fhirResourcesListSerializer()
	{
		return new FhirResourcesListSerializer(objectMapper());
	}

	@Bean
	public TargetSerializer targetSerializer()
	{
		return new TargetSerializer(objectMapper());
	}

	@Bean
	public TargetsSerializer targetsSerializer()
	{
		return new TargetsSerializer(objectMapper());
	}
}
