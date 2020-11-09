package org.highmed.dsf.bpe.spring.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.dbcp2.BasicDataSource;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.highmed.dsf.bpe.camunda.MultiVersionSpringProcessEngineConfiguration;
import org.highmed.dsf.bpe.delegate.DelegateProvider;
import org.highmed.dsf.bpe.delegate.DelegateProviderImpl;
import org.highmed.dsf.bpe.listener.CallActivityListener;
import org.highmed.dsf.bpe.listener.DefaultBpmnParseListener;
import org.highmed.dsf.bpe.listener.EndListener;
import org.highmed.dsf.bpe.listener.StartListener;
import org.highmed.dsf.bpe.plugin.ProcessPluginDefinitionAndClassLoader;
import org.highmed.dsf.bpe.plugin.ProcessPluginProvider;
import org.highmed.dsf.bpe.plugin.ProcessPluginProviderImpl;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CamundaConfig
{
	private static final Logger logger = LoggerFactory.getLogger(CamundaConfig.class);

	@Value("${org.highmed.dsf.bpe.db.url}")
	private String dbUrl;

	@Value("${org.highmed.dsf.bpe.db.camunda_user}")
	private String dbUsernameCamunda;

	@Value("${org.highmed.dsf.bpe.db.camunda_user_password}")
	private String dbPasswordCamunda;

	@Value("${org.highmed.dsf.bpe.process_plugin_directroy:process}")
	private String pluginDirectory;

	@Autowired
	private FhirConfig fhirConfig;

	@Autowired
	private FhirWebserviceClientProvider clientProvider;

	@Autowired
	private ApplicationContext applicationContext;

	@Bean
	public PlatformTransactionManager transactionManager()
	{
		return new DataSourceTransactionManager(dataSource());
	}

	@Bean
	public TransactionAwareDataSourceProxy transactionAwareDataSource()
	{
		return new TransactionAwareDataSourceProxy(dataSource());
	}

	@Bean
	public BasicDataSource dataSource()
	{
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(Driver.class.getName());
		dataSource.setUrl(dbUrl);
		dataSource.setUsername(dbUsernameCamunda);
		dataSource.setPassword(dbPasswordCamunda);

		dataSource.setTestOnBorrow(true);
		dataSource.setValidationQuery("SELECT 1");
		return dataSource;
	}

	@Bean
	public StartListener startListener()
	{
		return new StartListener(fhirConfig.taskHelper());
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
	public SpringProcessEngineConfiguration processEngineConfiguration(
			@SuppressWarnings("rawtypes") List<TypedValueSerializer> baseSerializers) throws IOException
	{
		List<ProcessPluginDefinitionAndClassLoader> definitions = processPluginProvider().getDefinitions();

		@SuppressWarnings("rawtypes")
		List<TypedValueSerializer> serializers = Stream
				.concat(baseSerializers.stream(), delegateProvider().getAdditionalTypedValueSerializers())
				.collect(Collectors.toList());

		List<ProcessEnginePlugin> plugins = definitions.stream()
				.map(ProcessPluginDefinitionAndClassLoader::getProcessPlugin).collect(Collectors.toList());

		var c = new MultiVersionSpringProcessEngineConfiguration(delegateProvider());
		c.setProcessEngineName("highmed");
		c.setDataSource(transactionAwareDataSource());
		c.setTransactionManager(transactionManager());
		c.setDatabaseSchemaUpdate("false");
		c.setJobExecutorActivate(true);
		c.setCustomPreBPMNParseListeners(List.of(defaultBpmnParseListener()));

		logger.info("{} variable serializer{} configured", serializers.size(), serializers.size() != 1 ? "s" : "");
		logger.debug("Variable serializer{}: {}", serializers.size() != 1 ? "s" : "",
				serializers.stream().map(p -> p.getClass().getName()).collect(Collectors.joining(", ", "[", "]")));
		c.setCustomPreVariableSerializers(serializers);

		logger.info("{} process plugin{} configured", plugins.size(), plugins.size() != 1 ? "s" : "");
		logger.debug("Process plugin{}: {}", plugins.size() != 1 ? "s" : "",
				definitions.stream().map(def -> def.getDefinition()).map(p -> p.getClass().getName())
						.collect(Collectors.joining(", ", "[", "]")));
		c.setProcessEnginePlugins(plugins);

		return c;
	}

	@Bean
	public DelegateProvider delegateProvider() throws IOException
	{
		return new DelegateProviderImpl(processPluginProvider().getClassLoadersByProcessDefinitionKeyAndVersion(),
				ClassLoader.getSystemClassLoader(),
				processPluginProvider().getApplicationContextsByProcessDefinitionKeyAndVersion(), applicationContext);
	}

	@Bean
	public ProcessEngineFactoryBean processEngineFactory(
			@SuppressWarnings("rawtypes") List<TypedValueSerializer> baseSerializers) throws IOException
	{
		var f = new ProcessEngineFactoryBean();
		f.setProcessEngineConfiguration(processEngineConfiguration(baseSerializers));
		return f;
	}

	@Bean
	public ProcessPluginProvider processPluginProvider() throws IOException
	{
		Path pluginDirectoryPath = Paths.get(pluginDirectory);

		if (!Files.isDirectory(pluginDirectoryPath))
			throw new IOException("Process plug in directory '" + pluginDirectoryPath.toString() + "' not readable");

		return new ProcessPluginProviderImpl(pluginDirectoryPath, applicationContext);
	}
}
