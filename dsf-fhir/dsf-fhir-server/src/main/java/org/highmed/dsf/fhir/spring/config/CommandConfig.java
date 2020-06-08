package org.highmed.dsf.fhir.spring.config;

import org.highmed.dsf.fhir.dao.command.AuthorizationHelper;
import org.highmed.dsf.fhir.dao.command.AuthorizationHelperImpl;
import org.highmed.dsf.fhir.dao.command.CommandFactory;
import org.highmed.dsf.fhir.dao.command.CommandFactoryImpl;
import org.highmed.dsf.fhir.dao.command.ValidationHelper;
import org.highmed.dsf.fhir.dao.command.ValidationHelperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandConfig
{
	@Value("${org.highmed.dsf.fhir.serverBase}")
	private String serverBase;

	@Value("${org.highmed.dsf.fhir.defaultPageCount}")
	private int defaultPageCount;

	@Autowired
	private DaoConfig daoConfig;

	@Autowired
	private HelperConfig helperConfig;

	@Autowired
	private SnapshotConfig snapshotConfig;

	@Autowired
	private EventConfig eventConfig;

	@Autowired
	private ReferenceConfig referenceConfig;

	@Autowired
	private AuthorizationConfig authorizationConfig;

	@Autowired
	private ValidationConfig validationConfig;

	@Bean
	public CommandFactory commandFactory()
	{
		return new CommandFactoryImpl(serverBase, defaultPageCount, daoConfig.dataSource(), daoConfig.daoProvider(),
				referenceConfig.referenceExtractor(), referenceConfig.referenceResolver(),
				referenceConfig.referenceCleaner(), helperConfig.responseGenerator(), helperConfig.exceptionHandler(),
				eventConfig.eventManager(), eventConfig.eventGenerator(),
				snapshotConfig::snapshotGeneratorWithTransaction, helperConfig.parameterConverter(),
				authorizationHelper(), validationHelper());
	}

	@Bean
	public AuthorizationHelper authorizationHelper()
	{
		return new AuthorizationHelperImpl(authorizationConfig.authorizationRuleProvider(),
				helperConfig.responseGenerator());
	}

	@Bean
	public ValidationHelper validationHelper()
	{
		return new ValidationHelperImpl(validationConfig.resourceValidator(), helperConfig.responseGenerator());
	}
}
