package org.highmed.fhir.search.parameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.highmed.fhir.search.parameters.basic.AbstractSearchParameter;
import org.highmed.fhir.search.parameters.basic.SearchParameter;
import org.highmed.fhir.search.parameters.basic.SearchParameter.SearchParameterDefinition;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

import com.google.common.base.Objects;

@SearchParameterDefinition(name = ResourceId.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Resource-id", type = SearchParamType.TOKEN, documentation = "Logical id of this artifact")
public class ResourceId extends AbstractSearchParameter<DomainResource>
{
	public static final String PARAMETER_NAME = "_id";

	private final String resourceIdColumn;
	private String id;

	public ResourceId(String resourceIdColumn)
	{
		super(PARAMETER_NAME);

		this.resourceIdColumn = resourceIdColumn;
	}

	@Override
	protected void configureSearchParameter(MultivaluedMap<String, String> queryParameters)
	{
		id = toId(queryParameters.getFirst(PARAMETER_NAME));
	}

	private String toId(String id)
	{
		return id == null || id.isBlank() ? null : id;
	}

	@Override
	public boolean isDefined()
	{
		return id != null;
	}

	@Override
	public String getFilterQuery()
	{
		return resourceIdColumn + " = ?";
	}

	@Override
	public int getSqlParameterCount()
	{
		return 1;
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement)
			throws SQLException
	{
		statement.setString(parameterIndex, id);
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		bundleUri.replaceQueryParam(PARAMETER_NAME, id);
	}

	@Override
	public boolean matches(DomainResource resource)
	{
		if (isDefined())
			return Objects.equal(resource.getId(), id);
		else
			throw SearchParameter.notDefined();
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return resourceIdColumn + sortDirectionWithSpacePrefix;
	}
}
