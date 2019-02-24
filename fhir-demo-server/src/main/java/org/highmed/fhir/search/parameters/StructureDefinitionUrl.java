package org.highmed.fhir.search.parameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.fhir.search.parameters.basic.AbstractCanonicalUrlParameter;
import org.highmed.fhir.search.parameters.basic.SearchParameter;
import org.highmed.fhir.search.parameters.basic.SearchParameter.SearchParameterDefinition;
import org.highmed.fhir.search.parameters.basic.SortParameter.SortDirection;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.StructureDefinition;

import com.google.common.base.Objects;

@SearchParameterDefinition(name = StructureDefinitionUrl.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/conformance-url", type = SearchParamType.URI, documentation = "The uri that identifies the structure definition")
public class StructureDefinitionUrl extends AbstractCanonicalUrlParameter<StructureDefinition>
{
	public static final String PARAMETER_NAME = "url";
	public static final String RESOURCE_COLUMN = "structure_definition";

	public static StructureDefinitionUrl precise(SortDirection sortDirection, String urlAndVersion)
	{
		return precise(RESOURCE_COLUMN, sortDirection, urlAndVersion);
	}

	public static StructureDefinitionUrl precise(String resourceColumn, SortDirection sortDirection,
			String urlAndVersion)
	{
		CanonicalUrlAndSearchType valueAndType = StructureDefinitionUrl.toValueAndType(urlAndVersion,
				UriSearchType.PRECISE);
		return new StructureDefinitionUrl(resourceColumn, sortDirection, valueAndType.url, valueAndType.version,
				valueAndType.type);
	}

	public static StructureDefinitionUrl below(SortDirection sortDirection, String urlAndVersion)
	{
		return below(RESOURCE_COLUMN, sortDirection, urlAndVersion);
	}

	public static StructureDefinitionUrl below(String resourceColumn, SortDirection sortDirection, String urlAndVersion)
	{
		CanonicalUrlAndSearchType valueAndType = StructureDefinitionUrl.toValueAndType(urlAndVersion,
				UriSearchType.BELOW);
		return new StructureDefinitionUrl(resourceColumn, sortDirection, valueAndType.url, valueAndType.version,
				valueAndType.type);
	}

	private final String resourceColumn;

	public StructureDefinitionUrl()
	{
		this(RESOURCE_COLUMN);
	}

	public StructureDefinitionUrl(String resourceColumn)
	{
		super(PARAMETER_NAME);

		this.resourceColumn = resourceColumn;
	}

	public StructureDefinitionUrl(SortDirection sortDirection, String url, String version, UriSearchType type)
	{
		this(RESOURCE_COLUMN, sortDirection, url, version, type);
	}

	public StructureDefinitionUrl(String resourceColumn, SortDirection sortDirection, String url, String version,
			UriSearchType type)
	{
		super(PARAMETER_NAME, sortDirection, url, version, type);

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
