package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractCanonicalUrlParameter;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = ResourceProfile.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Resource-profile", type = SearchParamType.TOKEN, documentation = "Profiles this resource claims to conform to")
public class ResourceProfile<R extends DomainResource> extends AbstractCanonicalUrlParameter<R>
{
	public static final String PARAMETER_NAME = "_profile";

	private final String resourceColumn;

	public ResourceProfile(String resourceColumn)
	{
		super(PARAMETER_NAME);

		this.resourceColumn = resourceColumn;
	}

	@Override
	public String getFilterQuery()
	{
		switch (valueAndType.type)
		{
			case PRECISE:
				if (valueAndType.version != null)
					return resourceColumn + "->'meta'->'profile' ?? ?";
				else
					// entries without version or entries with version - ignoring the version
					return "(" + resourceColumn + "->'meta'->'profile' ?? ?"
							+ " OR EXISTS (SELECT 1 FROM (SELECT jsonb_array_elements_text(" + resourceColumn
							+ "->'meta'->'profile') AS profile) AS profiles WHERE profile LIKE ?))";
			case BELOW:
				return "EXISTS (SELECT 1 FROM (SELECT jsonb_array_elements_text(" + resourceColumn
						+ "->'meta'->'profile') AS profile) AS profiles WHERE profile LIKE ?)";
			default:
				return "";
		}
	}

	@Override
	public int getSqlParameterCount()
	{
		switch (valueAndType.type)
		{
			case PRECISE:
				return valueAndType.version != null ? 1 : 2;
			case BELOW:
				return 1;
			default:
				return 0;
		}
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement,
			BiFunctionWithSqlException<String, Object[], Array> arrayCreator) throws SQLException
	{
		if (!isDefined())
			throw notDefined();

		switch (valueAndType.type)
		{
			case PRECISE:
				if (valueAndType.version != null)
					statement.setString(parameterIndex, valueAndType.url + "|" + valueAndType.version);
				else
				{
					if (subqueryParameterIndex == 1)
						statement.setString(parameterIndex, valueAndType.url);
					if (subqueryParameterIndex == 2)
						statement.setString(parameterIndex, valueAndType.url + "|%");
				}
				return;
			case BELOW:
				if (valueAndType.version != null)
					statement.setString(parameterIndex, valueAndType.url + "%|" + valueAndType.version);
				else
					statement.setString(parameterIndex, valueAndType.url + "%");
				return;
			default:
				return;
		}
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		switch (valueAndType.type)
		{
			case PRECISE:
				if (valueAndType.version != null)
					return resource.getMeta().getProfile().stream()
							.anyMatch(p -> p.getValue().equals(valueAndType.url + "|" + valueAndType.version));
				else
					return resource.getMeta().getProfile().stream()
							.anyMatch(p -> p.getValue().equals(valueAndType.url));
			case BELOW:
				if (valueAndType.version != null)
					return resource.getMeta().getProfile().stream()
							.anyMatch(p -> p.getValue().startsWith(valueAndType.url)
									&& p.getValue().endsWith("|" + valueAndType.version));
				else
					return resource.getMeta().getProfile().stream()
							.anyMatch(p -> p.getValue().startsWith(valueAndType.url));
			default:
				throw notDefined();
		}
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return resourceColumn + "->'meta'->>'profile'" + sortDirectionWithSpacePrefix;
	}
}
