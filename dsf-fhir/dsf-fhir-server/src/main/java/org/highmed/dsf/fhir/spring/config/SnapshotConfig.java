package org.highmed.dsf.fhir.spring.config;

import java.sql.Connection;

import org.highmed.dsf.fhir.service.SnapshotGenerator;
import org.highmed.dsf.fhir.service.SnapshotGeneratorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

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

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SnapshotGenerator snapshotGeneratorWithTransaction(Connection connection)
	{
		return new SnapshotGeneratorImpl(fhirConfig.fhirContext(),
				validationConfig.validationSupportWithTransaction(connection));
	}
}
