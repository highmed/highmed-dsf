package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.search.parameters.StructureDefinitionIdentifier;
import org.highmed.dsf.fhir.search.parameters.StructureDefinitionUrl;
import org.highmed.dsf.fhir.search.parameters.StructureDefinitionVersion;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

abstract class AbstractStructureDefinitionDaoJdbc extends AbstractResourceDaoJdbc<StructureDefinition>
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractStructureDefinitionDaoJdbc.class);

	private final ReadByUrlDaoJdbc<StructureDefinition> readByUrl;

	public AbstractStructureDefinitionDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext, String resourceTable,
			String resourceColumn, String resourceIdColumn)
	{
		super(dataSource, fhirContext, StructureDefinition.class, resourceTable, resourceColumn, resourceIdColumn,
				() -> new StructureDefinitionIdentifier(resourceColumn),
				() -> new StructureDefinitionUrl(resourceColumn), () -> new StructureDefinitionVersion(resourceColumn));

		readByUrl = new ReadByUrlDaoJdbc<StructureDefinition>(this::getDataSource, this::getResource, resourceTable,
				resourceColumn, resourceIdColumn);
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
		return readByUrl.readByUrl(urlAndVersion);
	}
}
