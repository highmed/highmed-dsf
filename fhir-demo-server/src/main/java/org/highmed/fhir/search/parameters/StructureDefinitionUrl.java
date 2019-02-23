package org.highmed.fhir.search.parameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.fhir.search.parameters.basic.AbstractCanonicalUrlParameter;
import org.highmed.fhir.search.parameters.basic.SearchParameter;
import org.highmed.fhir.search.parameters.basic.SearchParameter.SearchParameterDefinition;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.StructureDefinition;

import com.google.common.base.Objects;

@SearchParameterDefinition(name = StructureDefinitionUrl.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/conformance-url", type = SearchParamType.URI, documentation = "The uri that identifies the structure definition")
public class StructureDefinitionUrl extends AbstractCanonicalUrlParameter<StructureDefinition>
{
	public static final String PARAMETER_NAME = "url";

	private final String resourceColumn;

	public StructureDefinitionUrl()
	{
		this(null, null, null);
	}

	public StructureDefinitionUrl(String url, String version, UriSearchType type)
	{
		this("structure_definition", url, version, type);
	}

	public StructureDefinitionUrl(String resourceColumn)
	{
		this(resourceColumn, null, null, null);
	}

	public StructureDefinitionUrl(String resourceColumn, String url, String version, UriSearchType type)
	{
		super(PARAMETER_NAME, url, version, type);

		this.resourceColumn = resourceColumn;
	}

	@Override
	public String getFilterQuery()
	{
		String versionSubQuery = hasVersion() ? " AND " + resourceColumn + "->>'version' = ?" : "";

		switch (valueAndType.type)
		{
			case PRECISE:
				return resourceColumn + "->>'url' = ?" + versionSubQuery;
			case BELOW:
				return resourceColumn + "->>'url' LIKE ?" + versionSubQuery;
			default:
				return "";
		}
	}

	@Override
	public int getSqlParameterCount()
	{
		return hasVersion() ? 2 : 1;
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement)
			throws SQLException
	{
		if (subqueryParameterIndex == 1)
			switch (valueAndType.type)
			{
				case PRECISE:
					statement.setString(parameterIndex, valueAndType.url);
					return;
				case BELOW:
					statement.setString(parameterIndex, valueAndType.url + "%");
					return;
				default:
					return;

			}
		else if (subqueryParameterIndex == 2)
			statement.setString(parameterIndex, valueAndType.version);
	}

	@Override
	public boolean matches(StructureDefinition resource)
	{
		if (!isDefined())
			throw SearchParameter.notDefined();

		switch (valueAndType.type)
		{
			case PRECISE:
				return Objects.equal(resource.getUrl(), valueAndType.url)
						&& (valueAndType.version == null || Objects.equal(resource.getVersion(), valueAndType.version));
			case BELOW:
				return resource.getUrl() != null && resource.getUrl().startsWith(valueAndType.url)
						&& (valueAndType.version == null || Objects.equal(resource.getVersion(), valueAndType.version));
			default:
				throw SearchParameter.notDefined();
		}
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return resourceColumn + "->>'url'" + sortDirectionWithSpacePrefix;
	}
}
