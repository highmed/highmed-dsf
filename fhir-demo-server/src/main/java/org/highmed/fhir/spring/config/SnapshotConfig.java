package org.highmed.fhir.spring.config;

import org.highmed.fhir.service.SnapshotGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class SnapshotConfig
{
	@Autowired
	private FhirContext fhirContext;

	@Bean
	public SnapshotGenerator snapshotGenerator()
	{
		return new SnapshotGenerator(fhirContext/* TODO structureDefinitions */);
	}
}
