package org.highmed.dsf.fhir.spring.config;

import java.sql.Connection;
import java.util.Locale;

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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.i18n.HapiLocalizer;

@Configuration
public class FhirConfig
{
	@Autowired
	private DaoConfig daoConfig;

	@Bean
	public FhirContext fhirContext()
	{
		FhirContext context = FhirContext.forR4();
		HapiLocalizer localizer = new HapiLocalizer()
		{
			@Override
			public Locale getLocale()
			{
				return Locale.ROOT;
			}
		};
		context.setLocalizer(localizer);
		return context;
	}

	@Bean
	public IValidationSupport validationSupport()
	{
		return new ValidationSupportChain(new InMemoryTerminologyServerValidationSupport(fhirContext()),
				new ValidationSupportWithFetchFromDb(fhirContext(), daoConfig.structureDefinitionDao(),
						daoConfig.structureDefinitionSnapshotDao(), daoConfig.codeSystemDao(), daoConfig.valueSetDao()),
				new DefaultProfileValidationSupport(fhirContext()),
				new CommonCodeSystemsTerminologyService(fhirContext()));
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public IValidationSupport validationSupportWithTransaction(Connection connection)
	{
		return new ValidationSupportChain(new InMemoryTerminologyServerValidationSupport(fhirContext()),
				new ValidationSupportWithFetchFromDbWithTransaction(fhirContext(), daoConfig.structureDefinitionDao(),
						daoConfig.structureDefinitionSnapshotDao(), daoConfig.codeSystemDao(), daoConfig.valueSetDao(),
						connection),
				new DefaultProfileValidationSupport(fhirContext()),
				new CommonCodeSystemsTerminologyService(fhirContext()));
	}
}
