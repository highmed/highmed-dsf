package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.NamingSystemDao;
import org.highmed.dsf.fhir.search.parameters.NamingSystemDate;
import org.highmed.dsf.fhir.search.parameters.NamingSystemName;
import org.highmed.dsf.fhir.search.parameters.NamingSystemStatus;
import org.highmed.dsf.fhir.search.parameters.user.NamingSystemUserFilter;
import org.hl7.fhir.r4.model.NamingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class NamingSystemDaoJdbc extends AbstractResourceDaoJdbc<NamingSystem> implements NamingSystemDao
{
	private static final Logger logger = LoggerFactory.getLogger(NamingSystemDaoJdbc.class);

	public NamingSystemDaoJdbc(DataSource dataSource, DataSource deletionDataSource, FhirContext fhirContext)
	{
		super(dataSource, deletionDataSource, fhirContext, NamingSystem.class, "naming_systems", "naming_system", "naming_system_id",
				NamingSystemUserFilter::new,
				with(NamingSystemDate::new, NamingSystemName::new, NamingSystemStatus::new), with());
	}

	@Override
	protected NamingSystem copy(NamingSystem resource)
	{
		return resource.copy();
	}

	@Override
	public Optional<NamingSystem> readByName(String name) throws SQLException
	{
		try (Connection connection = getDataSource().getConnection())
		{
			return readByNameWithTransaction(connection, name);
		}
	}

	@Override
	public Optional<NamingSystem> readByNameWithTransaction(Connection connection, String name) throws SQLException
	{
		Objects.requireNonNull(connection, "connection");
		if (name == null || name.isBlank())
			return Optional.empty();

		try (PreparedStatement statement = connection
				.prepareStatement("SELECT naming_system FROM current_naming_systems WHERE naming_system->>'name' = ?"))
		{
			statement.setString(1, name);

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
