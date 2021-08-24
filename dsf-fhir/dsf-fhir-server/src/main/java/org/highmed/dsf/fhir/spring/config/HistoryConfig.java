package org.highmed.dsf.fhir.spring.config;

import org.highmed.dsf.fhir.history.HistoryService;
import org.highmed.dsf.fhir.history.HistoryServiceImpl;
import org.highmed.dsf.fhir.history.user.HistoryUserFilterFactory;
import org.highmed.dsf.fhir.history.user.HistoryUserFilterFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HistoryConfig
{
	@Autowired
	private PropertiesConfig propertiesConfig;

	@Autowired
	private HelperConfig helperConfig;

	@Autowired
	private ReferenceConfig referenceConfig;

	@Autowired
	private DaoConfig daoConfig;

	@Bean
	public HistoryUserFilterFactory historyUserFilterFactory()
	{
		return new HistoryUserFilterFactoryImpl();
	}

	@Bean
	public HistoryService historyService()
	{
		return new HistoryServiceImpl(propertiesConfig.getServerBaseUrl(), propertiesConfig.getDefaultPageCount(),
				helperConfig.parameterConverter(), helperConfig.exceptionHandler(), helperConfig.responseGenerator(),
				referenceConfig.referenceCleaner(), daoConfig.historyDao(), historyUserFilterFactory());
	}
}
