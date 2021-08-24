package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import org.highmed.dsf.fhir.dao.OrganizationDao;
import org.highmed.dsf.fhir.dao.exception.ResourceDeletedException;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.IncludeParameterDefinition;
import org.highmed.dsf.fhir.search.IncludeParts;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractIdentifierParameter;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractReferenceParameter;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

@IncludeParameterDefinition(resourceType = OrganizationAffiliation.class, parameterName = OrganizationAffiliationPrimaryOrganization.PARAMETER_NAME, targetResourceTypes = Organization.class)
@SearchParameterDefinition(name = OrganizationAffiliationPrimaryOrganization.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/OrganizationAffiliation-primary-organization", type = SearchParamType.REFERENCE, documentation = "The organization that receives the services from the participating organization")
public class OrganizationAffiliationPrimaryOrganization extends AbstractReferenceParameter<OrganizationAffiliation>
{
	private static final String RESOURCE_TYPE_NAME = "OrganizationAffiliation";
	public static final String PARAMETER_NAME = "primary-organization";
	private static final String TARGET_RESOURCE_TYPE_NAME = "Organization";

	private static final String IDENTIFIERS_SUBQUERY = "(SELECT organization->'identifier' FROM current_organizations"
			+ " WHERE concat('Organization/', organization->>'id') = organization_affiliation->'organization'->>'reference')";

	public OrganizationAffiliationPrimaryOrganization()
	{
		super(OrganizationAffiliation.class, RESOURCE_TYPE_NAME, PARAMETER_NAME, TARGET_RESOURCE_TYPE_NAME);
	}

	@Override
	public String getFilterQuery()
	{
		switch (valueAndType.type)
		{
			case ID:
			case RESOURCE_NAME_AND_ID:
			case URL:
			case TYPE_AND_ID:
			case TYPE_AND_RESOURCE_NAME_AND_ID:
				return "organization_affiliation->'organization'->>'reference' = ?";
			case IDENTIFIER:
			{
				switch (valueAndType.identifier.type)
				{
					case CODE:
					case CODE_AND_SYSTEM:
					case SYSTEM:
						return IDENTIFIERS_SUBQUERY + " @> ?::jsonb";
					case CODE_AND_NO_SYSTEM_PROPERTY:
						return "(SELECT count(*) FROM jsonb_array_elements(" + IDENTIFIERS_SUBQUERY
								+ ") identifier WHERE identifier->>'value' = ? AND NOT (identifier ?? 'system')) > 0";
				}
			}
		}

		return "";
	}

	@Override
	public int getSqlParameterCount()
	{
		return 1;
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement,
			BiFunctionWithSqlException<String, Object[], Array> arrayCreator) throws SQLException
	{
		switch (valueAndType.type)
		{
			case ID:
			case RESOURCE_NAME_AND_ID:
			case TYPE_AND_ID:
			case TYPE_AND_RESOURCE_NAME_AND_ID:
				statement.setString(parameterIndex, TARGET_RESOURCE_TYPE_NAME + "/" + valueAndType.id);
				break;
			case URL:
				statement.setString(parameterIndex, valueAndType.url);
				break;
			case IDENTIFIER:
			{
				switch (valueAndType.identifier.type)
				{
					case CODE:
						statement.setString(parameterIndex,
								"[{\"value\": \"" + valueAndType.identifier.codeValue + "\"}]");
						break;
					case CODE_AND_SYSTEM:
						statement.setString(parameterIndex, "[{\"value\": \"" + valueAndType.identifier.codeValue
								+ "\", \"system\": \"" + valueAndType.identifier.systemValue + "\"}]");
						break;
					case CODE_AND_NO_SYSTEM_PROPERTY:
						statement.setString(parameterIndex, valueAndType.identifier.codeValue);
						break;
					case SYSTEM:
						statement.setString(parameterIndex,
								"[{\"system\": \"" + valueAndType.identifier.systemValue + "\"}]");
						break;
				}
			}
		}
	}

	@Override
	protected void doResolveReferencesForMatching(OrganizationAffiliation resource, DaoProvider daoProvider)
			throws SQLException
	{
		OrganizationDao dao = daoProvider.getOrganizationDao();
		Reference reference = resource.getOrganization();
		IIdType idType = reference.getReferenceElement();

		try
		{
			if (idType.hasVersionIdPart())
				dao.readVersion(UUID.fromString(idType.getIdPart()), idType.getVersionIdPartAsLong())
						.ifPresent(reference::setResource);
			else
				dao.read(UUID.fromString(idType.getIdPart())).ifPresent(reference::setResource);
		}
		catch (ResourceDeletedException e)
		{
			// ignore while matching, will result in a non match if this would have been the matching resource
		}
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof OrganizationAffiliation))
			return false;

		OrganizationAffiliation oA = (OrganizationAffiliation) resource;

		if (ReferenceSearchType.IDENTIFIER.equals(valueAndType.type))
		{
			if (oA.getOrganization().getResource() instanceof Organization)
			{
				Organization o = (Organization) oA.getOrganization().getResource();
				return o.getIdentifier().stream()
						.anyMatch(i -> AbstractIdentifierParameter.identifierMatches(valueAndType.identifier, i));
			}
			else
				return false;
		}
		else
		{
			String ref = oA.getOrganization().getReference();
			switch (valueAndType.type)
			{
				case ID:
					return ref.equals(TARGET_RESOURCE_TYPE_NAME + "/" + valueAndType.id);
				case RESOURCE_NAME_AND_ID:
					return ref.equals(valueAndType.resourceName + "/" + valueAndType.id);
				case URL:
					return ref.equals(valueAndType.url);
				default:
					return false;
			}
		}
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "organization_affiliation->'organization'->>'reference'";
	}

	@Override
	protected String getIncludeSql(IncludeParts includeParts)
	{
		if (includeParts.matches(RESOURCE_TYPE_NAME, PARAMETER_NAME, TARGET_RESOURCE_TYPE_NAME))
			return "(SELECT jsonb_build_array(organization) FROM current_organizations WHERE concat('Organization/', organization->>'id') = organization_affiliation->'organization'->>'reference') AS organizations";
		else
			return null;
	}

	@Override
	protected void modifyIncludeResource(IncludeParts includeParts, Resource resource, Connection connection)
	{
		// Nothing to do for organizations
	}
}
