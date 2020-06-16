package org.highmed.dsf.fhir.spring.config;

import org.highmed.dsf.fhir.service.SnapshotGenerator;
import org.highmed.dsf.fhir.service.SnapshotGeneratorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnapshotConfig
{
	@Autowired
	private FhirConfig fhirConfig;

	@Autowired
	private ValidationConfig validationConfig;

	@Bean
	public SnapshotGenerator snapshotGenerator()
	{
		return new SnapshotGeneratorImpl(fhirConfig.fhirContext(), validationConfig.validationSupport());
	}
}