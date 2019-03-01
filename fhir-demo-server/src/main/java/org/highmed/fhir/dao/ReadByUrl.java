package org.highmed.fhir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.highmed.fhir.function.BiFunctionWithSqlException;
import org.hl7.fhir.r4.model.DomainResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ReadByUrl<R extends DomainResource>
{
	private static final Logger logger = LoggerFactory.getLogger(ReadByUrl.class);

	private final Supplier<DataSource> dataSourceSupplier;
	private final BiFunctionWithSqlException<ResultSet, Integer, R> resourceExtractor;

	private final String resourceTable;
	private final String resourceColumn;
	private final String resourceIdColumn;

	ReadByUrl(Supplier<DataSource> dataSourceSupplier,
			BiFunctionWithSqlException<ResultSet, Integer, R> resourceExtractor, String resourceTable,
			String resourceColumn, String resourceIdColumn)
	{
		this.dataSourceSupplier = dataSourceSupplier;
		this.resourceExtractor = resourceExtractor;
		this.resourceTable = resourceTable;
		this.resourceColumn = resourceColumn;
		this.resourceIdColumn = resourceIdColumn;
	}

	Optional<R> readByUrl(String urlAndVersion) throws SQLException
	{
		if (urlAndVersion == null || urlAndVersion.isBlank())
			return Optional.empty();

		String[] split = urlAndVersion.split("[|]");
		if (split.length < 1 || split.length > 2)
			return Optional.empty();

		String versionSql = split.length == 2 ? ("AND " + resourceColumn + "->>'version' = ? ") : "";
		String sql = "SELECT DISTINCT ON(" + resourceIdColumn + ") " + resourceColumn + " FROM " + resourceTable
				+ " WHERE NOT deleted AND " + resourceColumn + "->>'url' = ? " + versionSql + "ORDER BY "
				+ resourceIdColumn + ", version LIMIT 1";

		try (Connection connection = dataSourceSupplier.get().getConnection();
				PreparedStatement statement = connection.prepareStatement(sql))
		{
			statement.setString(1, split[0]);
			if (split.length == 2)
				statement.setString(2, split[1]);

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
					return Optional.of(resourceExtractor.apply(result, 1));
				else
					return Optional.empty();
			}
		}
	}
}
