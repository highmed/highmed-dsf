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
import org.highmed.dsf.fhir.search.parameters.basic.AbstractReferenceParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Resource;

@IncludeParameterDefinition(resourceType = Measure.class, parameterName = MeasureDependsOn.PARAMETER_NAME,
		targetResourceTypes = Library.class)
@SearchParameterDefinition(name = MeasureDependsOn.PARAMETER_NAME,
		definition = "http://hl7.org/fhir/SearchParameter/Measure-depends-on", type = SearchParamType.REFERENCE,
		documentation = "TODO")
public class MeasureDependsOn extends AbstractReferenceParameter<Measure>
{
	public static final String PARAMETER_NAME = "depends-on";
	private static final String RESOURCE_TYPE_NAME = "Measure";
	private static final String TARGET_RESOURCE_TYPE_NAME = "Library";

	public MeasureDependsOn()
	{
		super(Measure.class, RESOURCE_TYPE_NAME, PARAMETER_NAME, TARGET_RESOURCE_TYPE_NAME);
	}

	@Override
	public String getFilterQuery()
	{
		if (valueAndType.type == ReferenceSearchType.URL)
		{
			return "? IN (SELECT canonical FROM jsonb_array_elements(measure->'library') AS canonical)";
		}
		else
		{
			return "";
		}
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
		if (valueAndType.type == ReferenceSearchType.URL)
		{
			statement.setString(parameterIndex, valueAndType.url);
		}
	}

	@Override
	protected void doResolveReferencesForMatching(Measure resource, DaoProvider daoProvider) throws SQLException
	{
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Measure))
			return false;

		Measure o = (Measure) resource;

		return o.getLibrary().stream().anyMatch(ref -> {
			if (valueAndType.type == ReferenceSearchType.URL)
			{
				return ref.equals(valueAndType.url);
			}
			else
			{
				return false;
			}
		});
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "(SELECT string_agg(canonical, ' ') FROM jsonb_array_elements(measure->'library') AS canonical)";
	}

	@Override
	protected String getIncludeSql(IncludeParts includeParts)
	{
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
