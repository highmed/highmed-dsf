package org.highmed.fhir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.search.parameters.StructureDefinitionUrl;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public abstract class AbstractStructureDefinitionDao extends AbstractDomainResourceDao<StructureDefinition>
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractStructureDefinitionDao.class);

	public AbstractStructureDefinitionDao(BasicDataSource dataSource, FhirContext fhirContext, String resourceTable,
			String resourceColumn, String resourceIdColumn)
	{
		super(dataSource, fhirContext, StructureDefinition.class, resourceTable, resourceColumn, resourceIdColumn,
				() -> new StructureDefinitionUrl(resourceColumn));
	}

	public List<StructureDefinition> readAll() throws SQLException
	{
		try (Connection connection = getDataSource().getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT DISTINCT ON(" + getResourceIdColumn()
						+ ") " + getResourceColumn() + " FROM " + getResourceTable() + " WHERE NOT deleted ORDER BY "
						+ getResourceIdColumn() + ", version"))
		{
			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				List<StructureDefinition> all = new ArrayList<>();

				while (result.next())
					all.add(getResource(result, 1));

				return all;
			}
		}
	}

	public Optional<StructureDefinition> readByUrl(String urlAndVersion) throws SQLException
	{
		if (urlAndVersion == null || urlAndVersion.isBlank())
			return Optional.empty();

		String[] split = urlAndVersion.split("[|]");
		if (split.length < 1 || split.length > 2)
			return Optional.empty();

		String versionSql = split.length == 2 ? (getResourceColumn() + "->>'version' = ?") : "";
		String sql = "SELECT DISTINCT ON(" + getResourceIdColumn() + ") " + getResourceColumn() + " FROM "
				+ getResourceTable() + " WHERE NOT deleted AND " + getResourceColumn() + "->>'url' = ? " + versionSql
				+ "ORDER BY " + getResourceIdColumn() + ", version LIMIT 1";

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement statement = connection.prepareStatement(sql))
		{
			statement.setString(1, split[0]);
			if (split.length == 2)
				statement.setString(2, split[1]);

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
					return Optional.of(getResource(result, 1));
				else
					return Optional.empty();
			}
		}
	}
}
