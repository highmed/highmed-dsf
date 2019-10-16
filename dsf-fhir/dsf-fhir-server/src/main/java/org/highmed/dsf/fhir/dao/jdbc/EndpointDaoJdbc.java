package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.EndpointDao;
import org.highmed.dsf.fhir.search.parameters.EndpointIdentifier;
import org.highmed.dsf.fhir.search.parameters.EndpointName;
import org.highmed.dsf.fhir.search.parameters.EndpointOrganization;
import org.highmed.dsf.fhir.search.parameters.EndpointStatus;
import org.hl7.fhir.r4.model.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class EndpointDaoJdbc extends AbstractResourceDaoJdbc<Endpoint> implements EndpointDao
{
	private static final Logger logger = LoggerFactory.getLogger(EndpointDaoJdbc.class);

	public EndpointDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Endpoint.class, "endpoints", "endpoint", "endpoint_id",
				EndpointOrganization::new, EndpointIdentifier::new, EndpointName::new, EndpointStatus::new);
	}

	@Override
	protected Endpoint copy(Endpoint resource)
	{
		return resource.copy();
	}

	@Override
	public boolean existsActiveNotDeletedByAddress(String address) throws SQLException
	{
		if (address == null || address.isBlank())
			return false;

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM " + getResourceTable()
						+ " WHERE " + getResourceColumn() + "->>'address' = ? AND " + getResourceColumn()
						+ "->>'status' = 'active' AND NOT deleted"))
		{
			statement.setString(1, address);

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				return result.next() && result.getInt(1) > 0;
			}
		}
	}
}
