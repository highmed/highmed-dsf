package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.IncludeParameterDefinition;
import org.highmed.dsf.fhir.search.IncludeParts;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractCanonicalReferenceParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Resource;

@IncludeParameterDefinition(resourceType = Measure.class, parameterName = MeasureDependsOn.PARAMETER_NAME, targetResourceTypes = Library.class)
@SearchParameterDefinition(name = MeasureDependsOn.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Measure-depends-on", type = SearchParamType.REFERENCE, documentation = "What resource is being referenced")
public class MeasureDependsOn extends AbstractCanonicalReferenceParameter<Measure>
{
	private static final String RESOURCE_TYPE_NAME = "Measure";
	public static final String PARAMETER_NAME = "depends-on";
	private static final String TARGET_RESOURCE_TYPE_NAME = "Library";

	public MeasureDependsOn()
	{
		super(Measure.class, RESOURCE_TYPE_NAME, PARAMETER_NAME, TARGET_RESOURCE_TYPE_NAME);
	}

	@Override
	public boolean isDefined()
	{
		return super.isDefined() && ReferenceSearchType.URL.equals(valueAndType.type);
	}

	@Override
	public String getFilterQuery()
	{
		if (ReferenceSearchType.URL.equals(valueAndType.type))
			return "(measure->'library' ?? ? OR measure->'relatedArtifact' @> ?::jsonb)";

		return "";
	}

	@Override
	public int getSqlParameterCount()
	{
		return 2;
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement,
			BiFunctionWithSqlException<String, Object[], Array> arrayCreator) throws SQLException
	{
		if (ReferenceSearchType.URL.equals(valueAndType.type))
		{
			if (subqueryParameterIndex == 1)
				statement.setString(parameterIndex, valueAndType.url);
			else if (subqueryParameterIndex == 2)
				statement.setString(parameterIndex,
						"[{\"type\": \"depends-on\", \"resource\": \"" + valueAndType.url + "\"}]");
		}
	}

	@Override
	protected void doResolveReferencesForMatching(Measure resource, DaoProvider daoProvider) throws SQLException
	{
		// Nothing to do for libraries
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Measure))
			return false;

		Measure o = (Measure) resource;

		return o.getLibrary().stream().anyMatch(ref -> ref.equals(valueAndType.url));
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		// TODO Measure.relatedArtifact.where(type='depends-on').resource
		return "(SELECT string_agg(canonical, ' ') FROM jsonb_array_elements(measure->'library') AS canonical)";
	}

	@Override
	protected String getIncludeSql(IncludeParts includeParts)
	{
		// TODO Measure.relatedArtifact.where(type='depends-on').resource
		if (includeParts.matches(RESOURCE_TYPE_NAME, PARAMETER_NAME, TARGET_RESOURCE_TYPE_NAME))
			return "(SELECT jsonb_agg(library) FROM current_libraries WHERE library->'url' IN (SELECT canonical FROM jsonb_array_elements(measure->'library') AS canonical)) AS libraries";
		else
			return null;
	}

	@Override
	protected void modifyIncludeResource(IncludeParts includeParts, Resource resource, Connection connection)
	{
		// Nothing to do for libraries
	}
}
