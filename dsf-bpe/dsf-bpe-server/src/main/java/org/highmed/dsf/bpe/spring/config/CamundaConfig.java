package org.highmed.dsf.bpe.spring.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializerFactory;
import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.highmed.dsf.bpe.camunda.FallbackSerializerFactory;
import org.highmed.dsf.bpe.camunda.MultiVersionSpringProcessEngineConfiguration;
import org.highmed.dsf.bpe.delegate.DelegateProvider;
import org.highmed.dsf.bpe.delegate.DelegateProviderImpl;
import org.highmed.dsf.bpe.listener.CallActivityListener;
import org.highmed.dsf.bpe.listener.DebugLoggingBpmnParseListener;
import org.highmed.dsf.bpe.listener.DefaultBpmnParseListener;
import org.highmed.dsf.bpe.listener.DefaultUserTaskListener;
import org.highmed.dsf.bpe.listener.EndListener;
import org.highmed.dsf.bpe.listener.StartListener;
import org.highmed.dsf.bpe.plugin.ProcessPluginProvider;
import org.highmed.dsf.bpe.plugin.ProcessPluginProviderImpl;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CamundaConfig
{
	@Autowired
	private PropertiesConfig propertiesConfig;

	@Autowired
	private FhirConfig fhirConfig;

	@Autowired
	private FhirWebserviceClientProvider clientProvider;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	@SuppressWarnings("rawtypes")
	private List<TypedValueSerializer> baseSerializers;

	@Autowired
	private Environment environment;

	@Bean
	public PlatformTransactionManager transactionManager()
	{
		return new DataSourceTransactionManager(camundaDataSource());
	}

	@Bean
	public TransactionAwareDataSourceProxy transactionAwareDataSource()
	{
		return new TransactionAwareDataSourceProxy(camundaDataSource());
	}

	@Bean
	public BasicDataSource camundaDataSource()
	{
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(Driver.class.getName());
		dataSource.setUrl(propertiesConfig.getDbUrl());
		dataSource.setUsername(propertiesConfig.getDbCamundaUsername());
		dataSource.setPassword(toString(propertiesConfig.getDbCamundaPassword()));

		dataSource.setTestOnBorrow(true);
		dataSource.setValidationQuery("SELECT 1");
		return dataSource;
	}

	private String toString(char[] password)
	{
		return password == null ? null : String.valueOf(password);
	}

	@Bean
	public StartListener startListener()
	{
		return new StartListener(fhirConfig.taskHelper(), clientProvider.getLocalBaseUrl());
	}

	@Bean
	public EndListener endListener()
	{
		return new EndListener(clientProvider.getLocalWebserviceClient(), fhirConfig.taskHelper());
	}

	@Bean
	public CallActivityListener callActivityListener()
	{
		return new CallActivityListener();
	}

	@Bean
	public DefaultBpmnParseListener defaultBpmnParseListener()
	{
		return new DefaultBpmnParseListener(startListener(), endListener(), callActivityListener());
	}

	@Bean
	public DebugLoggingBpmnParseListener debugLoggingBpmnParseListener()
	{
		return new DebugLoggingBpmnParseListener(propertiesConfig.getDebugLogMessageOnActivityStart(),
				propertiesConfig.getDebugLogMessageOnActivityEnd(), propertiesConfig.getDebugLogMessageVariables());
	}

	@Bean
	public SpringProcessEngineConfiguration processEngineConfiguration() throws IOException
	{
		var c = new MultiVersionSpringProcessEngineConfiguration(delegateProvider());
		c.setProcessEngineName("dsf");
		c.setDataSource(transactionAwareDataSource());
		c.setTransactionManager(transactionManager());
		c.setDatabaseSchemaUpdate("false");
		c.setJobExecutorActivate(true);
		c.setCustomPreBPMNParseListeners(List.of(defaultBpmnParseListener(), debugLoggingBpmnParseListener()));
		c.setCustomPreVariableSerializers(baseSerializers);
		c.setFallbackSerializerFactory(getFallbackSerializerFactory());

		// see also MultiVersionSpringProcessEngineConfiguration
		c.setInitializeTelemetry(false);
		c.setTelemetryReporterActivate(false);

		return c;
	}

	@Bean
	public VariableSerializerFactory getFallbackSerializerFactory()
	{
		return new FallbackSerializerFactory(delegateProvider().getTypedValueSerializers());
	}

	@Bean
	public DelegateProvider delegateProvider()
	{
		return new DelegateProviderImpl(processPluginProvider().getClassLoadersByProcessDefinitionKeyAndVersion(),
				ClassLoader.getSystemClassLoader(),
				processPluginProvider().getApplicationContextsByProcessDefinitionKeyAndVersion(), applicationContext);
	}

	@Bean
	public ProcessEngineFactoryBean processEngineFactory() throws IOException
	{
		var f = new ProcessEngineFactoryBean();
		f.setProcessEngineConfiguration(processEngineConfiguration());
		return f;
	}

	@Bean
	public ProcessPluginProvider processPluginProvider()
	{
		Path processPluginDirectoryPath = propertiesConfig.getProcessPluginDirectory();

		if (!Files.isDirectory(processPluginDirectoryPath))
			throw new RuntimeException(
					"Process plug in directory '" + processPluginDirectoryPath.toString() + "' not readable");

		return new ProcessPluginProviderImpl(fhirConfig.fhirContext(), processPluginDirectoryPath, applicationContext,
				environment);
	}

	@Bean
	public DefaultUserTaskListener defaultUserTaskListener()
	{
		return new DefaultUserTaskListener(fhirConfig.clientProvider(), fhirConfig.organizationProvider(),
				fhirConfig.questionnaireResponseHelper(), fhirConfig.taskHelper(), fhirConfig.readAccessHelper());
	}
}
