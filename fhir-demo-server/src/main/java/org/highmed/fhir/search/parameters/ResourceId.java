package org.highmed.fhir.search.parameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import org.highmed.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.fhir.search.parameters.basic.AbstractSearchParameter;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.postgresql.util.PGobject;

import com.google.common.base.Objects;

import ca.uhn.fhir.parser.DataFormatException;

@SearchParameterDefinition(name = ResourceId.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Resource-id", type = SearchParamType.TOKEN, documentation = "Logical id of this artifact")
public class ResourceId<R extends DomainResource> extends AbstractSearchParameter<R>
{
	public static final String PARAMETER_NAME = "_id";

	private final String resourceIdColumn;
	private UUID id;

	public ResourceId(String resourceIdColumn)
	{
		super(PARAMETER_NAME);

		this.resourceIdColumn = resourceIdColumn;
	}

	@Override
	protected void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		id = toId(getFirst(queryParameters, PARAMETER_NAME));
	}

	private UUID toId(String id)
	{
		try
		{
			return id == null || id.isBlank() ? null : UUID.fromString(id);
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
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
		statement.setObject(parameterIndex, asUuidPgObject(id));
	}

	private PGobject asUuidPgObject(UUID uuid)
	{
		if (uuid == null)
			return null;

		try
		{
			PGobject o = new PGobject();
			o.setType("UUID");
			o.setValue(uuid.toString());
			return o;
		}
		catch (DataFormatException | SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		bundleUri.replaceQueryParam(PARAMETER_NAME, id.toString());
	}

	@Override
	public boolean matches(DomainResource resource)
	{
		if (isDefined())
			return Objects.equal(resource.getId(), id.toString());
		else
			throw notDefined();
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return resourceIdColumn + sortDirectionWithSpacePrefix;
	}
}
