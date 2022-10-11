package org.highmed.dsf.bpe.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class LastEventTimeDaoJdbc extends AbstractDaoJdbc implements LastEventTimeDao, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(LastEventTimeDaoJdbc.class);

	private final String type;

	public LastEventTimeDaoJdbc(BasicDataSource dataSource, String type)
	{
		super(dataSource);

		this.type = type;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(type, "type");
		if (type.isBlank())
			throw new IllegalArgumentException("type is blank");
	}

	@Override
	public Optional<LocalDateTime> readLastEventTime() throws SQLException
	{
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"SELECT last_event FROM last_events WHERE type = ? AND last_event IS NOT NULL"))
		{
			statement.setString(1, type);

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
					return Optional.of(result.getTimestamp(1).toLocalDateTime());
				else
					return Optional.empty();
			}
		}
	}

	@Override
	public LocalDateTime writeLastEventTime(LocalDateTime lastEvent) throws SQLException
	{
		Objects.requireNonNull(lastEvent, "lastEvent");

		lastEvent = lastEvent.truncatedTo(ChronoUnit.MILLIS);

		try (Connection connection = dataSource.getConnection())
		{
			connection.setReadOnly(false);

			try (PreparedStatement statement = connection.prepareStatement(
					"INSERT INTO last_events VALUES (?, ?) ON CONFLICT (type) WHERE type = ? DO UPDATE SET last_event = ?"))
			{
				statement.setString(1, type);
				statement.setTimestamp(2, Timestamp.valueOf(lastEvent));
				statement.setString(3, type);
				statement.setTimestamp(4, Timestamp.valueOf(lastEvent));

				logger.trace("Executing query '{}'", statement);
				statement.execute();
			}
		}

		return lastEvent;
	}
}
