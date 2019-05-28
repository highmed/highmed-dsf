package org.highmed.dsf.bpe.db;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

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

	private DbMigrator()
	{
	}

	public static void migrate(Properties dbProperties)
	{
		try
		{
			Class.forName(dbProperties.getProperty("db.driver"));
		}
		catch (ClassNotFoundException e)
		{
			logger.error("Error while loading db driver class", e);
			throw new RuntimeException(e);
		}

		try (BasicDataSource dataSource = new BasicDataSource())
		{
			dataSource.setDriverClassName(dbProperties.getProperty("db.driver"));
			dataSource.setUrl(dbProperties.getProperty("db.url"));
			dataSource.setUsername(dbProperties.getProperty("db.migration-username"));
			dataSource.setPassword(dbProperties.getProperty("db.migration-password"));

			try (Connection connection = dataSource.getConnection())
			{
				Database database = DatabaseFactory.getInstance()
						.findCorrectDatabaseImplementation(new JdbcConnection(connection));
				Liquibase liquibase = new Liquibase("db/db.changelog.xml", new ClassLoaderResourceAccessor(), database);

				ChangeLogParameters changeLogParameters = liquibase.getChangeLogParameters();
				changeLogParameters.set("liquibase_user", dbProperties.getProperty("db.migration-username"));
				changeLogParameters.set("server_users_group", dbProperties.getProperty("db.server_users_group"));
				changeLogParameters.set("server_user", dbProperties.getProperty("db.server_user"));
				changeLogParameters.set("server_user_password", dbProperties.getProperty("db.server_user_password"));
				changeLogParameters.set("camunda_users_group", dbProperties.getProperty("db.camunda_users_group"));
				changeLogParameters.set("camunda_user", dbProperties.getProperty("db.camunda_user"));
				changeLogParameters.set("camunda_user_password", dbProperties.getProperty("db.camunda_user_password"));

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
