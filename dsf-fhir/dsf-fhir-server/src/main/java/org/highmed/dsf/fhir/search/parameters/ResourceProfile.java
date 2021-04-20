package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractCanonicalUrlParameter;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractDateTimeParameter;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractUrlAndVersionParameter;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = ResourceProfile.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Resource-profile", type = SearchParamType.URI, documentation = "Profiles this resource claims to conform to")
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
				return resourceColumn + "->'meta'->'profile' ?? ?";
			case BELOW:
				return resourceColumn + "->'meta'->>'profile' ~ ?";
			default:
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
		if (!isDefined())
			throw notDefined();

		switch (valueAndType.type)
		{
			case PRECISE:
				statement.setString(parameterIndex, valueAndType.url);
				return;
			case BELOW:
				statement.setString(parameterIndex, valueAndType.url + ".*");
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
				return resource.getMeta().getProfile().stream().anyMatch(p -> p.getValue().equals(valueAndType.url));
			case BELOW:
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
