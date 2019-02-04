package org.highmed.fhir.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class FhirConfig
{
	@Bean
	public FhirContext fhirContext()
	{
		FhirContext fhirContext = FhirContext.forR4();
		return fhirContext;
	}
}
