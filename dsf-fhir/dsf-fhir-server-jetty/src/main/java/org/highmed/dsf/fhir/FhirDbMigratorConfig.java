package org.highmed.dsf.fhir;

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
public class FhirDbMigratorConfig implements DbMigratorConfig
{
	private static final String DB_LIQUIBASE_USER = "db.liquibase_user";
	private static final String DB_SERVER_USERS_GROUP = "db.server_users_group";
	private static final String DB_SERVER_USER = "db.server_user";
	private static final String DB_SERVER_USER_PASSWORD = "db.server_user_password";
	private static final String DB_SERVER_PERMANENT_DELETE_USERS_GROUP = "db.server_permanent_delete_users_group";
	private static final String DB_SERVER_PERMANENT_DELETE_USER = "db.server_permanent_delete_user";
	private static final String DB_SERVER_PERMANENT_DELETE_USER_PASSWORD = "db.server_permanent_delete_user_password";

	@Value("${org.highmed.dsf.fhir.db.url}")
	private String dbUrl;

	@Value("${org.highmed.dsf.fhir.db.liquibase.username:liquibase_user}")
	private String dbLiquibaseUsername;

	@Value("${org.highmed.dsf.fhir.db.liquibase.password}")
	private char[] dbLiquibasePassword;

	@Value("${org.highmed.dsf.fhir.db.user.group:fhir_users}")
	private String dbUsersGroup;

	@Value("${org.highmed.dsf.fhir.db.user.username:fhir_server_user}")
	private String dbUsername;

	@Value("${org.highmed.dsf.fhir.db.user.password}")
	private char[] dbPassword;

	@Value("${org.highmed.dsf.fhir.db.user.permanent.delete.group:fhir_permanent_delete_users}")
	private String dbPermanentDeleteUsersGroup;

	@Value("${org.highmed.dsf.fhir.db.user.permanent.delete.username:fhir_server_permanent_delete_user}")
	private String dbPermanentDeleteUsername;

	@Value("${org.highmed.dsf.fhir.db.user.permanent.delete.password}")
	private char[] dbPermanentDeletePassword;

	@Bean // static in order to initialize before @Configuration classes
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(
			ConfigurableEnvironment environment)
	{
		new DockerSecretsPropertySourceFactory(environment).readDockerSecretsAndAddPropertiesToEnvironment();

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
				dbUsername, DB_SERVER_USER_PASSWORD, toString(dbPassword), DB_SERVER_PERMANENT_DELETE_USERS_GROUP,
				dbPermanentDeleteUsersGroup, DB_SERVER_PERMANENT_DELETE_USER, dbPermanentDeleteUsername,
				DB_SERVER_PERMANENT_DELETE_USER_PASSWORD, toString(dbPermanentDeletePassword));
	}

	private String toString(char[] password)
	{
		return password == null ? null : String.valueOf(password);
	}
}
