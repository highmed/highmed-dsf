package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.OrganizationAffiliationDao;
import org.highmed.dsf.fhir.search.parameters.OrganizationAffiliationActive;
import org.highmed.dsf.fhir.search.parameters.OrganizationAffiliationEndpoint;
import org.highmed.dsf.fhir.search.parameters.OrganizationAffiliationIdentifier;
import org.highmed.dsf.fhir.search.parameters.OrganizationAffiliationParticipatingOrganization;
import org.highmed.dsf.fhir.search.parameters.OrganizationAffiliationPrimaryOrganization;
import org.highmed.dsf.fhir.search.parameters.OrganizationAffiliationRole;
import org.highmed.dsf.fhir.search.parameters.user.OrganizationAffiliationUserFilter;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class OrganizationAffiliationDaoJdbc extends AbstractResourceDaoJdbc<OrganizationAffiliation>
		implements OrganizationAffiliationDao
{
	private static final Logger logger = LoggerFactory.getLogger(OrganizationAffiliationDaoJdbc.class);

	public OrganizationAffiliationDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource,
			FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, OrganizationAffiliation.class,
				"organization_affiliations", "organization_affiliation", "organization_affiliation_id",
				OrganizationAffiliationUserFilter::new,
				with(OrganizationAffiliationActive::new, OrganizationAffiliationEndpoint::new,
						OrganizationAffiliationIdentifier::new, OrganizationAffiliationParticipatingOrganization::new,
						OrganizationAffiliationPrimaryOrganization::new, OrganizationAffiliationRole::new),
				with());
	}

	@Override
	protected OrganizationAffiliation copy(OrganizationAffiliation resource)
	{
		return resource.copy();
	}

	@Override
	public List<OrganizationAffiliation> readActiveNotDeletedByMemberOrganizationIdentifierIncludingOrganizationIdentifiersWithTransaction(
			Connection connection, String identifierValue) throws SQLException
	{
		Objects.requireNonNull(connection, "connection");
		if (identifierValue == null || identifierValue.isBlank())
			return Collections.emptyList();

		try (PreparedStatement statement = connection.prepareStatement("SELECT organization_affiliation"
				+ ",(SELECT identifiers->>'value' FROM current_organizations, jsonb_array_elements(organization->'identifier') AS identifiers "
				+ "WHERE identifiers->>'system' = 'http://highmed.org/sid/organization-identifier' "
				+ "AND concat('Organization/', organization->>'id') = organization_affiliation->'organization'->>'reference' LIMIT 1) AS organization_identifier "
				+ "FROM current_organization_affiliations WHERE organization_affiliation->>'active' = 'true' AND "
				+ "(SELECT organization->'identifier' FROM current_organizations WHERE organization->>'active' = 'true' AND "
				+ "concat('Organization/', organization->>'id') = organization_affiliation->'participatingOrganization'->>'reference') @> ?::jsonb"))
		{
			statement.setString(1, "[{\"system\": \"http://highmed.org/sid/organization-identifier\", \"value\": \""
					+ identifierValue + "\"}]");

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				List<OrganizationAffiliation> affiliations = new ArrayList<>();

				while (result.next())
				{
					OrganizationAffiliation oA = getResource(result, 1);
					String organizationIdentifier = result.getString(2);

					oA.getParticipatingOrganization().getIdentifier()
							.setSystem("http://highmed.org/sid/organization-identifier").setValue(identifierValue);
					oA.getOrganization().getIdentifier().setSystem("http://highmed.org/sid/organization-identifier")
							.setValue(organizationIdentifier);
					affiliations.add(oA);
				}

				return affiliations;
			}
		}
	}
}
