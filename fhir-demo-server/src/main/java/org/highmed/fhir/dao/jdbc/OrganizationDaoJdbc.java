package org.highmed.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.OrganizationDao;
import org.highmed.fhir.dao.exception.ResourceDeletedException;
import org.highmed.fhir.search.parameters.OrganizationActive;
import org.highmed.fhir.search.parameters.OrganizationEndpoint;
import org.highmed.fhir.search.parameters.OrganizationIdentifier;
import org.highmed.fhir.search.parameters.OrganizationName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class OrganizationDaoJdbc extends AbstractDomainResourceDaoJdbc<Organization> implements OrganizationDao
{
	private static final Logger logger = LoggerFactory.getLogger(OrganizationDaoJdbc.class);

	public OrganizationDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Organization.class, "organizations", "organization", "organization_id",
				OrganizationName::new, OrganizationEndpoint::new, OrganizationIdentifier::new, OrganizationActive::new);
	}

	@Override
	protected Organization copy(Organization resource)
	{
		return resource.copy();
	}

	@Override
	public Optional<Organization> readByThumbprint(String thumbprintHex) throws SQLException, ResourceDeletedException
	{
		if (thumbprintHex == null)
			return Optional.empty();

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement statement = connection
						.prepareStatement("SELECT " + getResourceColumn() + ", deleted FROM " + getResourceTable()
								+ " WHERE organization->'extension' @> ?::jsonb ORDER BY version LIMIT 1"))
		{

			String search = "[{\"url\": \"http://highmed.org/fhir/StructureDefinition/certificate-thumbprint\", \"valueString\": \""
					+ thumbprintHex + "\"}]";
			statement.setString(1, search);

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				if (result.next())
				{
					Organization organization = getResource(result, 1);
					if (result.getBoolean(2))
					{
						logger.debug("{} with thumprint {}, IdPart {} found, but marked as deleted.",
								getResourceTypeName(), thumbprintHex, organization.getIdElement().getIdPart());
						throw new ResourceDeletedException(
								new IdType(getResourceTypeName(), organization.getIdElement().getIdPart()));
					}
					else
					{
						logger.debug("{} with thumprint {}, IdPart {} found.", getResourceTypeName(),
								getResourceTypeName(), thumbprintHex, organization.getIdElement().getIdPart());
						return Optional.of(organization);
					}
				}
				else
					return Optional.empty();
			}
		}
	}
}
