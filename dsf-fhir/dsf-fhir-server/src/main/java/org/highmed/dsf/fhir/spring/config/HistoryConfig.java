package org.highmed.dsf.fhir.spring.config;

import org.highmed.dsf.fhir.history.HistoryService;
import org.highmed.dsf.fhir.history.HistoryServiceImpl;
import org.highmed.dsf.fhir.history.user.HistoryUserFilterFactory;
import org.highmed.dsf.fhir.history.user.HistoryUserFilterFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HistoryConfig
{
	@Value("${org.highmed.dsf.fhir.serverBase}")
	private String serverBase;

	@Value("${org.highmed.dsf.fhir.defaultPageCount}")
	private int defaultPageCount;

	@Autowired
	private HelperConfig helperConfig;

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
		return new HistoryServiceImpl(serverBase, defaultPageCount, helperConfig.parameterConverter(),
				helperConfig.exceptionHandler(), helperConfig.responseGenerator(), daoConfig.historyDao(),
				historyUserFilterFactory());
	}
}
