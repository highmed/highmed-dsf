package org.highmed.dsf.fhir.spring.config;

import java.sql.Connection;

import org.highmed.dsf.fhir.dao.command.ValidationHelper;
import org.highmed.dsf.fhir.dao.command.ValidationHelperImpl;
import org.highmed.dsf.fhir.service.ValidationSupportWithCache;
import org.highmed.dsf.fhir.service.ValidationSupportWithFetchFromDb;
import org.highmed.dsf.fhir.service.ValidationSupportWithFetchFromDbWithTransaction;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;

@Configuration
public class ValidationConfig
{
	@Autowired
	private DaoConfig daoConfig;

	@Autowired
	private FhirConfig fhirConfig;

	@Autowired
	private HelperConfig helperConfig;

	@Bean
	public IValidationSupport validationSupport()
	{
		return new ValidationSupportWithCache(fhirConfig.fhirContext(),
				validationSupportChain(new ValidationSupportWithFetchFromDb(fhirConfig.fhirContext(),
						daoConfig.structureDefinitionDao(), daoConfig.structureDefinitionSnapshotDao(),
						daoConfig.codeSystemDao(), daoConfig.valueSetDao(), daoConfig.measureDao())));
	}

	private ValidationSupportChain validationSupportChain(IValidationSupport dbSupport)
	{
		DefaultProfileValidationSupport dpvs = new DefaultProfileValidationSupport(FhirContext.forR4());
		dpvs.fetchCodeSystem(""); // FIXME HAPI bug workaround, to initialize
		dpvs.fetchAllStructureDefinitions(); // FIXME HAPI bug workaround, to initialize

		return new ValidationSupportChain(new InMemoryTerminologyServerValidationSupport(fhirConfig.fhirContext()),
				dbSupport, dpvs, new CommonCodeSystemsTerminologyService(fhirConfig.fhirContext()));
	}

	@Bean
	public ResourceValidator resourceValidator()
	{
		return new ResourceValidatorImpl(fhirConfig.fhirContext(), validationSupport());
	}

	@Bean
	public ValidationHelper validationHelper()
	{
		return new ValidationHelperImpl(resourceValidator(), helperConfig.responseGenerator());
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public IValidationSupport validationSupportWithTransaction(Connection connection)
	{
		ValidationSupportWithCache validationSupport = new ValidationSupportWithCache(fhirConfig.fhirContext(),
				validationSupportChain(new ValidationSupportWithFetchFromDbWithTransaction(fhirConfig.fhirContext(),
						daoConfig.structureDefinitionDao(), daoConfig.structureDefinitionSnapshotDao(),
						daoConfig.codeSystemDao(), daoConfig.valueSetDao(), connection)));

		return validationSupport.populateCache(validationSupport().fetchAllConformanceResources());
	}
}
