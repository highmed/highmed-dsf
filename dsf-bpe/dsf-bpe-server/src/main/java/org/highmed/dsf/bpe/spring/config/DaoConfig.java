package org.highmed.dsf.bpe.spring.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.bpe.dao.ProcessPluginResourcesDao;
import org.highmed.dsf.bpe.dao.ProcessPluginResourcesDaoJdbc;
import org.highmed.dsf.bpe.dao.ProcessStateDao;
import org.highmed.dsf.bpe.dao.ProcessStateDaoJdbc;
import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoConfig
{
	@Value("${org.highmed.dsf.bpe.db.url}")
	private String dbUrl;

	@Value("${org.highmed.dsf.bpe.db.server_user:bpe_server_user}")
	private String dbUsername;

	@Value("${org.highmed.dsf.bpe.db.server_user_password}")
	private String dbPassword;

	@Bean
	public BasicDataSource dataSource()
	{
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(Driver.class.getName());
		dataSource.setUrl(dbUrl);
		dataSource.setUsername(dbUsername);
		dataSource.setPassword(dbPassword);
		dataSource.setDefaultReadOnly(true);

		dataSource.setTestOnBorrow(true);
		dataSource.setValidationQuery("SELECT 1");
		return dataSource;
	}

	@Bean
	public ProcessPluginResourcesDao processPluginResourcesDao()
	{
		return new ProcessPluginResourcesDaoJdbc(dataSource());
	}

	@Bean
	public ProcessStateDao processStateDao()
	{
		return new ProcessStateDaoJdbc(dataSource());
	}
}
