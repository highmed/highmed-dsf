package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.UserRole;
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

	public ActivityDefinitionDaoJdbc(DataSource dataSource, DataSource deletionDataSource, FhirContext fhirContext)
	{
		super(dataSource, deletionDataSource, fhirContext, ActivityDefinition.class, "activity_definitions",
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
	public Optional<ActivityDefinition> readByOrganizationTypeUserRoleProcessUrlVersionMessageNameAndNotRetiredWithTransaction(
			Connection connection, OrganizationType recipientOrganizationType,
			OrganizationType requesterOrganizationType, UserRole userRole, String processUrl, String processVersion,
			String messageName) throws SQLException
	{
		Objects.requireNonNull(connection, "connection");
		Objects.requireNonNull(recipientOrganizationType, "recipientOrganizationType");
		Objects.requireNonNull(requesterOrganizationType, "requesterOrganizationType");
		Objects.requireNonNull(userRole, "userRole");
		Objects.requireNonNull(processUrl, "processUrl");
		if (processUrl.isBlank())
			throw new IllegalArgumentException("processUrl blank");
		Objects.requireNonNull(processVersion, "processVersion");
		if (processVersion.isBlank())
			throw new IllegalArgumentException("processVersion blank");
		Objects.requireNonNull(messageName, "messageName");
		if (messageName.isBlank())
			throw new IllegalArgumentException("messageName blank");

		try (PreparedStatement statement = connection
				.prepareStatement("SELECT activity_definition FROM current_activity_definitions WHERE "
						+ "activity_definition->>'url' = ? AND " + "activity_definition->>'version' = ? AND "
						+ "activity_definition->'extension' @> ?::jsonb AND "
						+ "lower(activity_definition->>'status') <> 'retired'"))
		{
			String extension = "[{\"url\":\"http://highmed.org/fhir/StructureDefinition/extension-process-authorization\",\"extension\":["
					+ "{\"url\":\"message-name\",\"valueString\":\"" + messageName + "\"},"
					+ "{\"url\":\"authorization-roles\",\"extension\":[{\"url\":\"authorization-role\",\"valueCoding\":{\"code\":\""
					+ userRole.toString()
					+ "\",\"system\":\"http://highmed.org/fhir/CodeSystem/authorization-role\"}}]},"
					+ "{\"url\":\"requester-organization-types\",\"extension\":[{\"url\":\"requester-organization-type\",\"valueCoding\":{\"code\":\""
					+ requesterOrganizationType.toString()
					+ "\",\"system\":\"http://highmed.org/fhir/CodeSystem/organization-type\"}}]},"
					+ "{\"url\":\"recipient-organization-types\",\"extension\":[{\"url\":\"recipient-organization-type\",\"valueCoding\":{\"code\":\""
					+ recipientOrganizationType.toString()
					+ "\",\"system\":\"http://highmed.org/fhir/CodeSystem/organization-type\"}}]}]}]";

			statement.setString(1, processUrl);
			statement.setString(2, processVersion);
			statement.setString(3, extension);

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
