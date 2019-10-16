package org.highmed.dsf.tools.db;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.changelog.ChangeLogParameters;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public final class DbMigrator
{
	private static final Logger logger = LoggerFactory.getLogger(DbMigrator.class);

	private static final String DB_DRIVER = "db.driver";
	private static final String DB_URL = "db.url";
	private static final String DB_LIQUIBASE_USER = "db.liquibase_user";
	private static final String DB_LIQUIBASE_USER_PASSWORD = "db.liquibase_user_password";

	private static final String DB_SERVER_USERS_GROUP = "db.server_users_group";
	private static final String DB_SERVER_USER = "db.server_user";
	private static final String DB_SERVER_USER_PASSWORD = "db.server_user_password";

	private static final String[] STANDARD_PROPERTIES = { DB_DRIVER, DB_URL, DB_LIQUIBASE_USER,
			DB_LIQUIBASE_USER_PASSWORD, DB_SERVER_USERS_GROUP, DB_SERVER_USER, DB_SERVER_USER_PASSWORD };
	private static final String[] STANDARD_CHANGE_LOG_PARAMETER_NAMES = { DB_LIQUIBASE_USER, DB_SERVER_USERS_GROUP,
			DB_SERVER_USER, DB_SERVER_USER_PASSWORD };

	private String prefix;
	private final Properties properties;
	private final List<String> changeLogParameterNames = new ArrayList<>();

	// "camunda_users_group", "camunda_user", "camunda_user_password"
	public DbMigrator(String prefix, Properties properties, String... additionalChangeLogParameterNames)
	{
		this.prefix = prefix;
		this.properties = properties;

		this.changeLogParameterNames.addAll(Arrays.asList(STANDARD_CHANGE_LOG_PARAMETER_NAMES));
		this.changeLogParameterNames.addAll(Arrays.asList(additionalChangeLogParameterNames));

		checkProperties(STANDARD_PROPERTIES, additionalChangeLogParameterNames);
	}

	private void checkProperties(String[] standardProperties, String[] additionalChangeLogParameterNames)
	{
		List<String> missingParameters = Stream
				.concat(Arrays.stream(standardProperties), Arrays.stream(additionalChangeLogParameterNames))
				.map(parameterName -> prefix + parameterName)
				.map(parameterName -> properties.get(parameterName) != null
						&& !properties.get(parameterName).toString().isBlank() ? null : parameterName)
				.filter(missingParameter -> missingParameter != null).collect(Collectors.toList());

		if (!missingParameters.isEmpty())
		{
			logger.error("DB properties has missing entries:  {}", missingParameters);
			throw new RuntimeException("Db properties missing: " + missingParameters);
		}
	}

	public void migrate()
	{
		try
		{
			Class.forName(properties.getProperty(prefix + DB_DRIVER));
		}
		catch (ClassNotFoundException e)
		{
			logger.error("Error while loading db driver class", e);
			throw new RuntimeException(e);
		}

		try (BasicDataSource dataSource = new BasicDataSource())
		{
			dataSource.setDriverClassName(properties.getProperty(prefix + DB_DRIVER));
			dataSource.setUrl(properties.getProperty(prefix + DB_URL));
			dataSource.setUsername(properties.getProperty(prefix + DB_LIQUIBASE_USER));
			dataSource.setPassword(properties.getProperty(prefix + DB_LIQUIBASE_USER_PASSWORD));

			try (Connection connection = dataSource.getConnection())
			{
				Database database = DatabaseFactory.getInstance()
						.findCorrectDatabaseImplementation(new JdbcConnection(connection));
				Liquibase liquibase = new Liquibase("db/db.changelog.xml", new ClassLoaderResourceAccessor(), database);

				ChangeLogParameters changeLogParameters = liquibase.getChangeLogParameters();
				changeLogParameterNames.forEach(parameterName -> changeLogParameters.set(parameterName,
						properties.getProperty(prefix + parameterName)));

				logger.info("Executing DB migration ...");
				liquibase.update(new Contexts());
				logger.info("Executing DB migration [Done]");
			}
		}
		catch (SQLException e)
		{
			logger.error("Error while accessing db", e);
			throw new RuntimeException(e);
		}
		catch (LiquibaseException e)
		{
			logger.error("Error while running liquibase", e);
			throw new RuntimeException(e);
		}
	}

	public static void retryOnConnectException(int times, Runnable run)
	{
		if (times <= 0)
			return;

		try
		{
			run.run();
		}
		catch (RuntimeException e)
		{
			Throwable cause = e;
			while (!(cause instanceof ConnectException) && cause.getCause() != null)
				cause = cause.getCause();

			if (cause instanceof ConnectException && times > 1)
			{
				logger.warn("ConnectException: trying again in 1s");
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e1)
				{
				}
				retryOnConnectException(--times, run);
			}
			else
				throw e;
		}
	}
}
