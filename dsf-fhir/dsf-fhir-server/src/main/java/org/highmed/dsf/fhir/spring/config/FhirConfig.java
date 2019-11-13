package org.highmed.dsf.fhir.spring.config;

import org.highmed.dsf.fhir.service.DefaultProfileValidationSupportWithFetchFromDb;
import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class FhirConfig
{
	@Autowired
	private DaoConfig daoConfig;

	@Bean
	public FhirContext fhirContext()
	{
		return FhirContext.forR4();
	}

	@Bean
	public IValidationSupport validationSupport()
	{
		return new DefaultProfileValidationSupportWithFetchFromDb(fhirContext(), daoConfig.structureDefinitionDao(),
				daoConfig.structureDefinitionSnapshotDao(), daoConfig.codeSystemDao(), daoConfig.valueSetDao());
	}
}
