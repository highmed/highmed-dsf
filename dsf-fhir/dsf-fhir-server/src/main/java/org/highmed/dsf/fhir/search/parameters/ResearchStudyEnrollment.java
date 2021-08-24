package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import org.highmed.dsf.fhir.dao.GroupDao;
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
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Resource;

@IncludeParameterDefinition(resourceType = ResearchStudy.class, parameterName = ResearchStudyEnrollment.PARAMETER_NAME, targetResourceTypes = Group.class)
@SearchParameterDefinition(name = ResearchStudyEnrollment.PARAMETER_NAME, definition = "http://highmed.org/fhir/SearchParameter/ResearchStudy-enrollment", type = SearchParamType.REFERENCE, documentation = "Search by research study enrollment")
public class ResearchStudyEnrollment extends AbstractReferenceParameter<ResearchStudy>
{
	private static final String RESOURCE_TYPE_NAME = "ResearchStudy";
	public static final String PARAMETER_NAME = "enrollment";
	private static final String TARGET_RESOURCE_TYPE_NAME = "Group";

	public ResearchStudyEnrollment()
	{
		super(ResearchStudy.class, RESOURCE_TYPE_NAME, PARAMETER_NAME, TARGET_RESOURCE_TYPE_NAME);
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
				return "? IN (SELECT reference->>'reference' FROM jsonb_array_elements(research_study->'enrollment') AS reference)";

			case IDENTIFIER:
			{
				switch (valueAndType.identifier.type)
				{
					case CODE:
					case CODE_AND_SYSTEM:
					case SYSTEM:
						return "(SELECT jsonb_agg(identifier) FROM (SELECT identifier FROM current_groups, jsonb_array_elements(group_json->'identifier') identifier"
								+ " WHERE concat('Group/', group_json->>'id') IN (SELECT reference->>'reference' FROM jsonb_array_elements(research_study->'enrollment') reference)"
								+ " ) AS identifiers) @> ?::jsonb";
					case CODE_AND_NO_SYSTEM_PROPERTY:
						return "(SELECT count(*) FROM (SELECT identifier FROM current_groups, jsonb_array_elements(group_json->'identifier') identifier"
								+ " WHERE concat('Group/', group_json->>'id') IN (SELECT reference->>'reference' FROM jsonb_array_elements(research_study->'enrollment') reference)"
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
	protected void doResolveReferencesForMatching(ResearchStudy resource, DaoProvider daoProvider) throws SQLException
	{
		GroupDao dao = daoProvider.getGroupDao();
		for (Reference reference : resource.getEnrollment())
		{
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
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof ResearchStudy))
			return false;

		ResearchStudy rs = (ResearchStudy) resource;

		if (ReferenceSearchType.IDENTIFIER.equals(valueAndType.type))
		{
			return rs.getEnrollment().stream().map(Reference::getResource).filter(r -> r instanceof Group)
					.flatMap(r -> ((Group) r).getIdentifier().stream())
					.anyMatch(i -> AbstractIdentifierParameter.identifierMatches(valueAndType.identifier, i));
		}
		else
		{
			return rs.getEnrollment().stream().map(Reference::getReference).anyMatch(ref ->
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
		return "(SELECT string_agg(reference->>'reference', ' ') FROM jsonb_array_elements(research_study->'enrollment') AS reference)";
	}

	@Override
	protected String getIncludeSql(IncludeParts includeParts)
	{
		if (includeParts.matches(RESOURCE_TYPE_NAME, PARAMETER_NAME, TARGET_RESOURCE_TYPE_NAME))
			return "(SELECT jsonb_agg(group_json) FROM current_groups WHERE concat('Group/', group_json->>'id') IN (SELECT reference->>'reference' FROM jsonb_array_elements(research_study->'enrollment') AS reference)) AS groups";
		else
			return null;
	}

	@Override
	protected void modifyIncludeResource(IncludeParts includeParts, Resource resource, Connection connection)
	{
		// Nothing to do for groups
	}
}
