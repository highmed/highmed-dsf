package org.highmed.dsf.bpe.spring.config;

import java.util.List;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.highmed.dsf.bpe.listener.CallActivityListener;
import org.highmed.dsf.bpe.listener.DefaultBpmnParseListener;
import org.highmed.dsf.bpe.listener.EndListener;
import org.highmed.dsf.bpe.listener.StartListener;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

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

	@Autowired
	private FhirConfig fhirConfig;

	@Autowired
	private FhirWebserviceClientProvider clientProvider;

	@Bean
	public DataSourceTransactionManager transactionManager()
	{
		return new DataSourceTransactionManager(transactionAwareDataSource());
	}

	@Bean
	public TransactionAwareDataSourceProxy transactionAwareDataSource()
	{
		return new TransactionAwareDataSourceProxy(dataSource());
	}

	@Bean
	public SimpleDriverDataSource dataSource()
	{
		return new SimpleDriverDataSource(new org.postgresql.Driver(), dbUrl, dbUsernameCamunda, dbPasswordCamunda);
	}

	@Bean
	public StartListener startListener()
	{
		return new StartListener();
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
	public SpringProcessEngineConfiguration processEngineConfiguration(List<ProcessEnginePlugin> processEnginePlugins)
	{
		var c = new SpringProcessEngineConfiguration();
		c.setProcessEngineName("highmed");
		c.setDataSource(transactionAwareDataSource());
		c.setTransactionManager(transactionManager());
		c.setDatabaseSchemaUpdate("false");
		c.setJobExecutorActivate(true);
		c.setCustomPreBPMNParseListeners(List.of(defaultBpmnParseListener()));

		logger.info("{} process engine plugin{} configured", processEnginePlugins.size(),
				processEnginePlugins.size() != 1 ? "s" : "");

		c.setProcessEnginePlugins(processEnginePlugins);
		return c;
	}

	@Bean
	public ProcessEngineFactoryBean processEngineFactory(List<ProcessEnginePlugin> processEnginePlugins)
	{
		var f = new ProcessEngineFactoryBean();
		f.setProcessEngineConfiguration(processEngineConfiguration(processEnginePlugins));
		return f;
	}
}
