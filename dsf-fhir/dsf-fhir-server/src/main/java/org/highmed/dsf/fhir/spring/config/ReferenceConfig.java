package org.highmed.dsf.fhir.spring.config;

import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceCleanerImpl;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceExtractorImpl;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ReferenceResolverImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReferenceConfig
{
	@Autowired
	private PropertiesConfig propertiesConfig;

	@Autowired
	private HelperConfig helperConfig;

	@Autowired
	private DaoConfig daoConfig;

	@Autowired
	private ClientConfig clientConfig;

	@Bean
	public ReferenceExtractor referenceExtractor()
	{
		return new ReferenceExtractorImpl();
	}

	@Bean
	public ReferenceResolver referenceResolver()
	{
		return new ReferenceResolverImpl(propertiesConfig.getServerBaseUrl(), daoConfig.daoProvider(),
				helperConfig.responseGenerator(), helperConfig.exceptionHandler(), clientConfig.clientProvider(),
				helperConfig.parameterConverter());
	}

	@Bean
	public ReferenceCleaner referenceCleaner()
	{
		return new ReferenceCleanerImpl(referenceExtractor());
	}
}
