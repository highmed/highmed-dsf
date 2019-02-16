package org.highmed.fhir.spring.config;

import org.highmed.fhir.service.ResourceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class ValidationConfig
{
	@Autowired
	private FhirContext fhirContext;

	@Bean
	public ResourceValidator resourceValidator()
	{
		return new ResourceValidator(fhirContext/* TODO structureDefinitions */);
	}
}
