package org.highmed.fhir.spring.config;

import org.highmed.fhir.dao.command.CommandFactory;
import org.highmed.fhir.dao.command.CommandFactoryImpl;
import org.highmed.fhir.service.ReferenceExtractorImpl;
import org.highmed.fhir.service.ReferenceResolver;
import org.highmed.fhir.service.ReferenceResolverImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandConfig
{
	@Value("${org.highmed.fhir.serverBase}")
	private String serverBase;

	@Value("${org.highmed.fhir.defaultPageCount}")
	private int defaultPageCount;

	@Autowired
	private DaoConfig daoConfig;

	@Autowired
	private HelperConfig helperConfig;

	@Autowired
	private SnapshotConfig snapshotConfig;

	@Autowired
	private EventConfig eventConfig;

	@Bean
	public ReferenceExtractorImpl referenceExtractor()
	{
		return new ReferenceExtractorImpl();
	}

	@Bean
	public ReferenceResolver referenceResolver()
	{
		return new ReferenceResolverImpl(serverBase, daoConfig.daoProvider(), helperConfig.responseGenerator(),
				helperConfig.exceptionHandler());
	}

	@Bean
	public CommandFactory commandFactory()
	{
		return new CommandFactoryImpl(serverBase, defaultPageCount, daoConfig.dataSource(), daoConfig.daoProvider(),
				referenceExtractor(), referenceResolver(), helperConfig.responseGenerator(),
				helperConfig.exceptionHandler(), eventConfig.eventManager(), eventConfig.eventGenerator(),
				snapshotConfig.snapshotGenerator(), snapshotConfig.snapshotDependencyAnalyzer(),
				helperConfig.parameterConverter());
	}
}
