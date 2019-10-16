package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import org.highmed.dsf.fhir.dao.EndpointDao;
import org.highmed.dsf.fhir.dao.exception.ResourceDeletedException;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryIncludeParameter.IncludeParts;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractIdentifierParameter;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractReferenceParameter;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = OrganizationEndpoint.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Organization.endpoint", type = SearchParamType.REFERENCE, documentation = "Technical endpoints providing access to services operated for the organization")
public class OrganizationEndpoint extends AbstractReferenceParameter<Organization>
{
	private static final String RESOURCE_TYPE_NAME = "Organization";
	public static final String PARAMETER_NAME = "endpoint";
	private static final String TARGET_RESOURCE_TYPE_NAME = "Endpoint";

	public OrganizationEndpoint()
	{
		super(Organization.class, RESOURCE_TYPE_NAME, PARAMETER_NAME, TARGET_RESOURCE_TYPE_NAME);
	}

	@Override
	public String getFilterQuery()
	{
		switch (valueAndType.type)
		{
			case ID:
			case RESOURCE_NAME_AND_ID:
			case URL:
				return "? IN (SELECT reference->>'reference' FROM jsonb_array_elements(organization->'endpoint') AS reference)";

			case IDENTIFIER:
			{
				switch (valueAndType.identifier.type)
				{
					case CODE:
					case CODE_AND_SYSTEM:
					case SYSTEM:
						return "(SELECT jsonb_agg(identifier) FROM (SELECT identifier FROM current_endpoints, jsonb_array_elements(endpoint->'identifier') identifier"
								+ " WHERE concat('Endpoint/', endpoint->>'id') IN (SELECT reference->>'reference' FROM jsonb_array_elements(organization->'endpoint') reference)"
								+ " ) AS identifiers) @> ?::jsonb";
					case CODE_AND_NO_SYSTEM_PROPERTY:
						return "(SELECT count(*) FROM (SELECT identifier FROM current_endpoints, jsonb_array_elements(endpoint->'identifier') identifier"
								+ " WHERE concat('Endpoint/', endpoint->>'id') IN (SELECT reference->>'reference' FROM jsonb_array_elements(organization->'endpoint') reference)"
								+ " ) AS identifiers WHERE identifier->>'value' = ? AND NOT (identifier ?? 'system')) > 0";
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
				statement.setString(parameterIndex, TARGET_RESOURCE_TYPE_NAME + "/" + valueAndType.id);
				break;
			case RESOURCE_NAME_AND_ID:
				statement.setString(parameterIndex, valueAndType.resourceName + "/" + valueAndType.id);
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
	protected void doResolveReferencesForMatching(Organization resource, DaoProvider daoProvider) throws SQLException
	{
		EndpointDao dao = daoProvider.getEndpointDao();
		for (Reference reference : resource.getEndpoint())
		{
			IIdType idType = reference.getReferenceElement();

			if (idType.hasVersionIdPart())
			{
				dao.readVersion(UUID.fromString(idType.getIdPart()), idType.getVersionIdPartAsLong())
						.ifPresent(reference::setResource);
			}
			else
			{
				try
				{
					dao.read(UUID.fromString(idType.getIdPart())).ifPresent(reference::setResource);
				}
				catch (ResourceDeletedException e)
				{
					// ignore while matching, will result in a non match if this would have been the matching resource
				}
			}
		}
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Organization))
			return false;

		Organization o = (Organization) resource;

		if (ReferenceSearchType.IDENTIFIER.equals(valueAndType.type))
		{
			return o.getEndpoint().stream().map(e -> e.getResource()).filter(r -> r instanceof Endpoint)
					.flatMap(r -> ((Endpoint) r).getIdentifier().stream())
					.anyMatch(i -> AbstractIdentifierParameter.identifierMatches(valueAndType.identifier, i));
		}
		else
		{
			return o.getEndpoint().stream().map(e -> e.getReference()).anyMatch(ref ->
			{
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
			});
		}
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "(SELECT string_agg(reference->>'reference', ' ') FROM jsonb_array_elements(organization->'endpoint') AS reference)";
	}

	@Override
	protected String getIncludeSql(IncludeParts includeParts)
	{
		if (includeParts.matches(RESOURCE_TYPE_NAME, PARAMETER_NAME, TARGET_RESOURCE_TYPE_NAME))
			return "(SELECT jsonb_agg(endpoint) FROM current_endpoints"
					+ " WHERE concat('Endpoint/', endpoint->>'id') IN (SELECT reference->>'reference' FROM jsonb_array_elements(organization->'endpoint') AS reference)"
					+ ") AS endpoints";
		else
			return null;
	}

	@Override
	protected void doModifyIncludeResource(Resource resource, Connection connection)
	{
		// Nothing to do for endpoints
	}
}
