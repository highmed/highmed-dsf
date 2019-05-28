package org.highmed.fhir.spring.config;

import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.service.ResourceValidatorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfig
{
	@Autowired
	private FhirConfig fhirConfig;

	@Bean
	public ResourceValidator resourceValidator()
	{
		return new ResourceValidatorImpl(fhirConfig.fhirContext(), fhirConfig.validationSupport());
	}
}
