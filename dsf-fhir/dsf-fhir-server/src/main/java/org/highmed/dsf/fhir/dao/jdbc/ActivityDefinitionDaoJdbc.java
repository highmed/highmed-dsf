package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.ActivityDefinitionDao;
import org.highmed.dsf.fhir.search.parameters.ActivityDefinitionDate;
import org.highmed.dsf.fhir.search.parameters.ActivityDefinitionIdentifier;
import org.highmed.dsf.fhir.search.parameters.ActivityDefinitionName;
import org.highmed.dsf.fhir.search.parameters.ActivityDefinitionStatus;
import org.highmed.dsf.fhir.search.parameters.ActivityDefinitionUrl;
import org.highmed.dsf.fhir.search.parameters.ActivityDefinitionVersion;
import org.highmed.dsf.fhir.search.parameters.user.ActivityDefinitionUserFilter;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class ActivityDefinitionDaoJdbc extends AbstractResourceDaoJdbc<ActivityDefinition>
		implements ActivityDefinitionDao
{
	private static final Logger logger = LoggerFactory.getLogger(ActivityDefinitionDaoJdbc.class);

	private final ReadByUrlDaoJdbc<ActivityDefinition> readByUrl;

	public ActivityDefinitionDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource,
			FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, ActivityDefinition.class, "activity_definitions",
				"activity_definition", "activity_definition_id", ActivityDefinitionUserFilter::new,
				with(ActivityDefinitionDate::new, ActivityDefinitionIdentifier::new, ActivityDefinitionName::new,
						ActivityDefinitionStatus::new, ActivityDefinitionUrl::new, ActivityDefinitionVersion::new),
				with());

		readByUrl = new ReadByUrlDaoJdbc<ActivityDefinition>(this::getDataSource, this::getResource, getResourceTable(),
				getResourceColumn());
	}

	@Override
	protected ActivityDefinition copy(ActivityDefinition resource)
	{
		return resource.copy();
	}

	@Override
	public Optional<ActivityDefinition> readByUrlAndVersion(String urlAndVersion) throws SQLException
	{
		return readByUrl.readByUrlAndVersion(urlAndVersion);
	}

	@Override
	public Optional<ActivityDefinition> readByUrlAndVersionWithTransaction(Connection connection, String urlAndVersion)
			throws SQLException
	{
		return readByUrl.readByUrlAndVersionWithTransaction(connection, urlAndVersion);
	}

	@Override
	public Optional<ActivityDefinition> readByUrlAndVersion(String url, String version) throws SQLException
	{
		return readByUrl.readByUrlAndVersion(url, version);
	}

	@Override
	public Optional<ActivityDefinition> readByUrlAndVersionWithTransaction(Connection connection, String url,
			String version) throws SQLException
	{
		return readByUrl.readByUrlAndVersionWithTransaction(connection, url, version);
	}

	@Override
	public Optional<ActivityDefinition> readByProcessUrlVersionAndStatusDraftOrActiveWithTransaction(
			Connection connection, String processUrl, String processVersion) throws SQLException
	{
		Objects.requireNonNull(connection, "connection");
		Objects.requireNonNull(processUrl, "processUrl");
		if (processUrl.isBlank())
			throw new IllegalArgumentException("processUrl blank");
		Objects.requireNonNull(processVersion, "processVersion");
		if (processVersion.isBlank())
			throw new IllegalArgumentException("processVersion blank");

		try (PreparedStatement statement = connection
				.prepareStatement("SELECT activity_definition FROM current_activity_definitions WHERE "
						+ "activity_definition->>'url' = ? AND " + "activity_definition->>'version' = ? AND "
						+ "(lower(activity_definition->>'status') = 'draft' OR lower(activity_definition->>'status') = 'active')"))
		{
			statement.setString(1, processUrl);
			statement.setString(2, processVersion);

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				List<ActivityDefinition> definitions = new ArrayList<>();
				if (result.next())
					definitions.add(getResource(result, 1));

				if (definitions.size() != 1)
				{
					logger.warn(
							"ActivityDefinition with process-url '{}' and process-version '{}' not found, or more than one",
							processUrl, processVersion);
					return Optional.empty();
				}
				else
					return Optional.of(definitions.get(0));
			}
		}
	}
}
