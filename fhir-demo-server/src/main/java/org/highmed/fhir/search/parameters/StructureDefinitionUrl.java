package org.highmed.fhir.search.parameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.fhir.search.SearchParameter;
import org.highmed.fhir.webservice.search.AbstractCanonicalUrlParameter;
import org.highmed.fhir.webservice.search.WsSearchParameter.SearchParameterDefinition;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.StructureDefinition;

import com.google.common.base.Objects;

@SearchParameterDefinition(name = StructureDefinitionUrl.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/conformance-url", type = SearchParamType.URI, documentation = "The uri that identifies the structure definition")
public class StructureDefinitionUrl extends AbstractCanonicalUrlParameter
		implements SearchParameter<StructureDefinition>
{
	public static final String PARAMETER_NAME = "url";

	public StructureDefinitionUrl()
	{
		super(PARAMETER_NAME);
	}

	@Override
	public String getFilterQuery()
	{
		String versionSubQuery = hasVersion() ? " AND structure_definition->>'version' = ?" : "";

		switch (valueAndType.type)
		{
			case PRECISE:
				return "structure_definition->>'url' = ?" + versionSubQuery;
			case BELOW:
				return "structure_definition->>'url' LIKE ?" + versionSubQuery;
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
		switch (valueAndType.type)
		{
			case PRECISE:
				return Objects.equal(resource.getUrl(), valueAndType.url)
						&& (valueAndType.version == null || Objects.equal(resource.getVersion(), valueAndType.version));
			case BELOW:
				return resource.getUrl() != null && resource.getUrl().startsWith(valueAndType.url)
						&& (valueAndType.version == null || Objects.equal(resource.getVersion(), valueAndType.version));
			default:
				return false;
		}
	}
}
