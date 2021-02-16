package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import org.highmed.dsf.fhir.dao.PractitionerDao;
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
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

@IncludeParameterDefinition(resourceType = PractitionerRole.class, parameterName = PractitionerRolePractitioner.PARAMETER_NAME, targetResourceTypes = Practitioner.class)
@SearchParameterDefinition(name = PractitionerRolePractitioner.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/PractitionerRole-practitioner", type = SearchParamType.REFERENCE, documentation = "Practitioner that is able to provide the defined services for the organization")
public class PractitionerRolePractitioner extends AbstractReferenceParameter<PractitionerRole>
{
	private static final String RESOURCE_TYPE_NAME = "PractitionerRole";
	public static final String PARAMETER_NAME = "practitioner";
	private static final String TARGET_RESOURCE_TYPE_NAME = "Practitioner";

	private static final String PRACTITIONER_IDENTIFIERS_SUBQUERY = "(SELECT practitioner->'identifier' FROM current_practitioners"
			+ " WHERE concat('Practitioner/', practitioner->>'id') = practitioner_role->'practitioner'->>'reference')";

	public PractitionerRolePractitioner()
	{
		super(PractitionerRole.class, RESOURCE_TYPE_NAME, PARAMETER_NAME, TARGET_RESOURCE_TYPE_NAME);
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
				return "practitioner_role->'practitioner'->>'reference' = ?";
			case IDENTIFIER:
			{
				switch (valueAndType.identifier.type)
				{
					case CODE:
					case CODE_AND_SYSTEM:
					case SYSTEM:
						return PRACTITIONER_IDENTIFIERS_SUBQUERY + " @> ?::jsonb";
					case CODE_AND_NO_SYSTEM_PROPERTY:
						return "(SELECT count(*) FROM jsonb_array_elements(" + PRACTITIONER_IDENTIFIERS_SUBQUERY
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
	protected void doResolveReferencesForMatching(PractitionerRole resource, DaoProvider daoProvider)
			throws SQLException
	{
		PractitionerDao dao = daoProvider.getPractitionerDao();
		Reference reference = resource.getPractitioner();
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

		if (!(resource instanceof PractitionerRole))
			return false;

		PractitionerRole pR = (PractitionerRole) resource;

		if (ReferenceSearchType.IDENTIFIER.equals(valueAndType.type))
		{
			if (pR.getPractitioner().getResource() instanceof Practitioner)
			{
				Practitioner p = (Practitioner) pR.getPractitioner().getResource();
				return p.getIdentifier().stream()
						.anyMatch(i -> AbstractIdentifierParameter.identifierMatches(valueAndType.identifier, i));
			}
			else
				return false;
		}
		else
		{
			String ref = pR.getPractitioner().getReference();
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
		return "practitioner_role->'practitioner'->>'reference'";
	}

	@Override
	protected String getIncludeSql(IncludeParts includeParts)
	{
		if (includeParts.matches(RESOURCE_TYPE_NAME, PARAMETER_NAME, TARGET_RESOURCE_TYPE_NAME))
			return "(SELECT jsonb_build_array(practitioner) FROM current_practitioners WHERE concat('Practitioner/', practitioner->>'id') = practitioner_role->'practitioner'->>'reference') AS practitioners";
		else
			return null;
	}

	@Override
	protected void modifyIncludeResource(IncludeParts includeParts, Resource resource, Connection connection)
	{
		// Nothing to do for practitioners
	}
}
