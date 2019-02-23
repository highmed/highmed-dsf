package org.highmed.fhir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.search.parameters.StructureDefinitionUrl;
import org.highmed.fhir.search.parameters.basic.AbstractCanonicalUrlParameter.UriSearchType;
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
	
	public StructureDefinitionUrl createStructureDefinitionUrl(String url, String version, UriSearchType type)
	{
		return new StructureDefinitionUrl(getResourceColumn(), url, version, type);
	}

	public List<StructureDefinition> readAll() throws SQLException
	{
		try (Connection connection = getDataSource().getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT DISTINCT ON(" + getResourceIdColumn()
						+ ")" + getResourceColumn() + " WHERE NOT deleted FROM " + getResourceTable() + " ORDER BY "
						+ getResourceIdColumn() + ", version LIMIT 1"))
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
	
	public StructureDefinition readByUrl(String url)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
