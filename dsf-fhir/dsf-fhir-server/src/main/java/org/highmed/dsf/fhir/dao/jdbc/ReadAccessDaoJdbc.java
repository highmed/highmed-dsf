package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.authentication.UserRole;
import org.highmed.dsf.fhir.dao.ReadAccessDao;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.parser.DataFormatException;

public class ReadAccessDaoJdbc implements ReadAccessDao, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(ReadAccessDaoJdbc.class);

	private final DataSource dataSource;

	public ReadAccessDaoJdbc(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(dataSource, "dataSource");
	}

	@Override
	public List<String> getAccessTypes(Connection connection, UUID resourceId, long version, UserRole role,
			UUID organizationId) throws SQLException
	{
		Objects.requireNonNull(connection, "connection");
		Objects.requireNonNull(resourceId, "resourceId");
		if (version <= 0)
			throw new IllegalArgumentException("version <= 0");
		Objects.requireNonNull(role, "role");
		Objects.requireNonNull(organizationId, "organizationId");

		try (PreparedStatement statement = connection.prepareStatement(getReadAllowedQuery(role)))
		{
			statement.setObject(1, uuidToPgObject(resourceId));
			statement.setLong(2, version);
			statement.setObject(3, uuidToPgObject(organizationId));

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				List<String> accessTypes = new ArrayList<>();
				while (result.next())
					accessTypes.add(result.getString(1));
				return accessTypes;
			}
		}
	}

	private String getReadAllowedQuery(UserRole role)
	{
		switch (role)
		{
			case LOCAL:
				return "SELECT DISTINCT access_type FROM read_access WHERE resource_id = ? AND resource_version = ? AND (access_type = 'ALL' OR access_type = 'LOCAL' OR organization_id = ?) ORDER BY access_type";
			case REMOTE:
				return "SELECT DISTINCT access_type FROM read_access WHERE resource_id = ? AND resource_version = ? AND (access_type = 'ALL' OR organization_id = ?) ORDER BY access_type";
			default:
				throw new IllegalArgumentException(UserRole.class.getName() + " " + role + " not supported");
		}
	}

	private PGobject uuidToPgObject(UUID uuid)
	{
		if (uuid == null)
			return null;

		try
		{
			PGobject o = new PGobject();
			o.setType("UUID");
			o.setValue(uuid.toString());
			return o;
		}
		catch (DataFormatException | SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
}
