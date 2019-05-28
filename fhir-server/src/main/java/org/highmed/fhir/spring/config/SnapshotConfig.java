package org.highmed.fhir.spring.config;

import org.highmed.fhir.service.SnapshotDependencyAnalyzer;
import org.highmed.fhir.service.SnapshotGenerator;
import org.highmed.fhir.service.SnapshotGeneratorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnapshotConfig
{
	@Autowired
	private FhirConfig fhirConfig;

	@Bean
	public SnapshotGenerator snapshotGenerator()
	{
		return new SnapshotGeneratorImpl(fhirConfig.fhirContext(), fhirConfig.validationSupport());
	}

	@Bean
	public SnapshotDependencyAnalyzer snapshotDependencyAnalyzer()
	{
		return new SnapshotDependencyAnalyzer();
	}
}
