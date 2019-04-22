package org.highmed.fhir.spring.config;

import org.highmed.fhir.dao.command.CommandFactory;
import org.highmed.fhir.dao.command.ReferenceExtractor;
import org.highmed.fhir.dao.command.ReferenceReplacer;
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
	public ReferenceReplacer referenceReplacer()
	{
		return new ReferenceReplacer();
	}

	@Bean
	public ReferenceExtractor referenceExtractor()
	{
		return new ReferenceExtractor();
	}

	@Bean
	public CommandFactory commandFactory()
	{
		return new CommandFactory(serverBase, defaultPageCount, daoConfig.dataSource(), daoConfig.daoProvider(),
				referenceReplacer(), referenceExtractor(), helperConfig.responseGenerator(),
				helperConfig.exceptionHandler(), eventConfig.eventManager(), eventConfig.eventGenerator(),
				snapshotConfig.snapshotGenerator(), snapshotConfig.snapshotDependencyAnalyzer(),
				helperConfig.parameterConverter());
	}
}
