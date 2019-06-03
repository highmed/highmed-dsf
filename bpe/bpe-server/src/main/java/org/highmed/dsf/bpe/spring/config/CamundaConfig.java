package org.highmed.dsf.bpe.spring.config;

import java.util.List;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	@Value("${org.highmed.bpe.db.url}")
	private String dbUrl;

	@Value("${org.highmed.bpe.db.username.camunda}")
	private String dbUsernameCamunda;

	@Value("${org.highmed.bpe.db.password.camunda}")
	private String dbPasswordCamunda;

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
	public SpringProcessEngineConfiguration processEngineConfiguration(List<ProcessEnginePlugin> processEnginePlugins)
	{
		var c = new SpringProcessEngineConfiguration();
		c.setProcessEngineName("highmed");
		c.setDataSource(transactionAwareDataSource());
		c.setTransactionManager(transactionManager());
		c.setDatabaseSchemaUpdate("false");
		c.setJobExecutorActivate(true);

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
