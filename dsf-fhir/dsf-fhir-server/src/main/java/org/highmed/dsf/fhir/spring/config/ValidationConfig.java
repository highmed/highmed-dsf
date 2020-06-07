package org.highmed.dsf.fhir.spring.config;

import java.sql.Connection;

import org.highmed.dsf.fhir.service.ResourceValidator;
import org.highmed.dsf.fhir.service.ResourceValidatorImpl;
import org.highmed.dsf.fhir.service.ValidationSupportWithFetchFromDb;
import org.highmed.dsf.fhir.service.ValidationSupportWithFetchFromDbWithTransaction;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;

@Configuration
public class ValidationConfig
{
	@Autowired
	private DaoConfig daoConfig;

	@Autowired
	private FhirConfig fhirConfig;

	@Bean
	public ResourceValidator resourceValidator()
	{
		return new ResourceValidatorImpl(fhirConfig.fhirContext(), validationSupport());
	}

	@Bean
	public IValidationSupport validationSupport()
	{
		return new ValidationSupportChain(new InMemoryTerminologyServerValidationSupport(fhirConfig.fhirContext()),
				new ValidationSupportWithFetchFromDb(fhirConfig.fhirContext(), daoConfig.structureDefinitionDao(),
						daoConfig.structureDefinitionSnapshotDao(), daoConfig.codeSystemDao(), daoConfig.valueSetDao()),
				new DefaultProfileValidationSupport(fhirConfig.fhirContext()),
				new CommonCodeSystemsTerminologyService(fhirConfig.fhirContext()));
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public IValidationSupport validationSupportWithTransaction(Connection connection)
	{
		return new ValidationSupportChain(new InMemoryTerminologyServerValidationSupport(fhirConfig.fhirContext()),
				new ValidationSupportWithFetchFromDbWithTransaction(fhirConfig.fhirContext(),
						daoConfig.structureDefinitionDao(), daoConfig.structureDefinitionSnapshotDao(),
						daoConfig.codeSystemDao(), daoConfig.valueSetDao(), connection),
				new DefaultProfileValidationSupport(fhirConfig.fhirContext()),
				new CommonCodeSystemsTerminologyService(fhirConfig.fhirContext()));
	}
}
