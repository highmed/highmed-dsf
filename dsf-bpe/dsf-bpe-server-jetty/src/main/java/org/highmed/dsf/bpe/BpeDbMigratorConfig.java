package org.highmed.dsf.bpe;

import java.util.Map;

import org.highmed.dsf.tools.db.DbMigratorConfig;
import org.highmed.dsf.tools.docker.secrets.DockerSecretsPropertySourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
@PropertySource(value = "file:conf/config.properties", encoding = "UTF-8", ignoreResourceNotFound = true)
public class BpeDbMigratorConfig implements DbMigratorConfig
{
	private static final String DB_LIQUIBASE_USER = "db.liquibase_user";
	private static final String DB_SERVER_USERS_GROUP = "db.server_users_group";
	private static final String DB_SERVER_USER = "db.server_user";
	private static final String DB_SERVER_USER_PASSWORD = "db.server_user_password";
	private static final String DB_CAMUNDA_USERS_GROUP = "db.camunda_users_group";
	private static final String DB_CAMUNDA_USER = "db.camunda_user";
	private static final String DB_CAMUNDA_USER_PASSWORD = "db.camunda_user_password";

	@Value("${org.highmed.dsf.bpe.db.url}")
	private String dbUrl;

	@Value("${org.highmed.dsf.bpe.db.liquibase.username:liquibase_user}")
	private String dbLiquibaseUsername;

	@Value("${org.highmed.dsf.bpe.db.liquibase.password}")
	private char[] dbLiquibasePassword;

	@Value("${org.highmed.dsf.bpe.db.user.group:bpe_users}")
	private String dbUsersGroup;

	@Value("${org.highmed.dsf.bpe.db.user.username:bpe_server_user}")
	private String dbUsername;

	@Value("${org.highmed.dsf.bpe.db.user.password}")
	private char[] dbPassword;

	@Value("${org.highmed.dsf.bpe.db.user.camunda.group:camunda_users}")
	private String dbCamundaUsersGroup;

	@Value("${org.highmed.dsf.bpe.db.user.camunda.username:camunda_server_user}")
	private String dbCamundaUsername;

	@Value("${org.highmed.dsf.bpe.db.user.camunda.password}")
	private char[] dbCamundaPassword;

	@Bean // static in order to initialize before @Configuration classes
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(
			ConfigurableEnvironment environment)
	{
		new DockerSecretsPropertySourceFactory(environment, "org.highmed.dsf.bpe.db.liquibase.password",
				"org.highmed.dsf.bpe.db.user.password", "org.highmed.dsf.bpe.db.user.camunda.password")
						.readDockerSecretsAndAddPropertiesToEnvironment();

		return new PropertySourcesPlaceholderConfigurer();
	}

	@Override
	public String getDbUrl()
	{
		return dbUrl;
	}

	@Override
	public String getDbLiquibaseUsername()
	{
		return dbLiquibaseUsername;
	}

	@Override
	public char[] getDbLiquibasePassword()
	{
		return dbLiquibasePassword;
	}

	@Override
	public Map<String, String> getChangeLogParameters()
	{
		return Map.of(DB_LIQUIBASE_USER, dbLiquibaseUsername, DB_SERVER_USERS_GROUP, dbUsersGroup, DB_SERVER_USER,
				dbUsername, DB_SERVER_USER_PASSWORD, toString(dbPassword), DB_CAMUNDA_USERS_GROUP, dbCamundaUsersGroup,
				DB_CAMUNDA_USER, dbCamundaUsername, DB_CAMUNDA_USER_PASSWORD, toString(dbCamundaPassword));
	}

	private String toString(char[] password)
	{
		return password == null ? null : String.valueOf(password);
	}
}
