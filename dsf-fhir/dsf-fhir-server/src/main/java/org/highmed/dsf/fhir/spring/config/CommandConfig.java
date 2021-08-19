package org.highmed.dsf.fhir.spring.config;

import java.sql.Connection;

import org.highmed.dsf.fhir.dao.command.CommandFactory;
import org.highmed.dsf.fhir.dao.command.CommandFactoryImpl;
import org.highmed.dsf.fhir.dao.command.TransactionEventHandler;
import org.highmed.dsf.fhir.dao.command.TransactionResources;
import org.highmed.dsf.fhir.dao.command.ValidationHelper;
import org.highmed.dsf.fhir.dao.command.ValidationHelperImpl;
import org.highmed.dsf.fhir.event.EventHandler;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.highmed.dsf.fhir.validation.SnapshotGenerator;
import org.highmed.dsf.fhir.validation.SnapshotGeneratorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import ca.uhn.fhir.context.support.IValidationSupport;

@Configuration
public class CommandConfig
{
	@Autowired
	private PropertiesConfig propertiesConfig;

	@Autowired
	private DaoConfig daoConfig;

	@Autowired
	private HelperConfig helperConfig;

	@Autowired
	private EventConfig eventConfig;

	@Autowired
	private ReferenceConfig referenceConfig;

	@Autowired
	private SnapshotConfig snapshotConfig;

	@Autowired
	private AuthorizationConfig authorizationConfig;

	@Autowired
	private ValidationConfig validationConfig;

	@Autowired
	private FhirConfig fhirConfig;

	@Bean
	public CommandFactory commandFactory()
	{
		return new CommandFactoryImpl(propertiesConfig.getServerBaseUrl(), propertiesConfig.getDefaultPageCount(),
				daoConfig.dataSource(), daoConfig.daoProvider(), referenceConfig.referenceExtractor(),
				referenceConfig.referenceResolver(), referenceConfig.referenceCleaner(),
				helperConfig.responseGenerator(), helperConfig.exceptionHandler(), helperConfig.parameterConverter(),
				eventConfig.eventManager(), eventConfig.eventGenerator(), authorizationConfig.authorizationHelper(),
				validationConfig.validationHelper(), snapshotConfig.snapshotGenerator(),
				this::transactionResourceFactory);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public TransactionResources transactionResourceFactory(Connection connection)
	{
		IValidationSupport validationSupport = validationConfig.validationSupportWithTransaction(connection);

		ValidationHelper validationHelper = new ValidationHelperImpl(
				new ResourceValidatorImpl(fhirConfig.fhirContext(), validationSupport),
				helperConfig.responseGenerator());

		SnapshotGenerator snapshotGenerator = new SnapshotGeneratorImpl(fhirConfig.fhirContext(), validationSupport);

		TransactionEventHandler transactionEventHandler = new TransactionEventHandler(eventConfig.eventManager(),
				validationSupport instanceof EventHandler ? (EventHandler) validationSupport : null);

		return new TransactionResources(validationHelper, snapshotGenerator, transactionEventHandler);
	}
}
