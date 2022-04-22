package org.highmed.dsf.fhir.search.parameters;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.search.SearchQueryParameterError.SearchQueryParameterErrorType;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractSearchParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Resource;
import org.postgresql.util.PGobject;

import ca.uhn.fhir.parser.DataFormatException;

@SearchParameterDefinition(name = ResourceId.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Resource-id", type = SearchParamType.STRING, documentation = "Logical id of this resource")
public class ResourceId<R extends Resource> extends AbstractSearchParameter<R>
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
		String firstValue = getFirst(queryParameters, PARAMETER_NAME);

		if (firstValue == null)
			return; // parameter not defined

		id = toId(firstValue, queryParameters.get(PARAMETER_NAME));
	}

	private UUID toId(String firstValue, List<String> values)
	{
		if (values != null && values.size() > 1)
			addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNSUPPORTED_NUMBER_OF_VALUES,
					PARAMETER_NAME, values));

		if (firstValue.isBlank())
		{
			addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE, PARAMETER_NAME,
					values));
			return null;
		}

		try
		{
			return UUID.fromString(firstValue);
		}
		catch (IllegalArgumentException e)
		{
			addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE, PARAMETER_NAME,
					values, e));
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
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement,
			BiFunctionWithSqlException<String, Object[], Array> arrayCreator) throws SQLException
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
	public boolean matches(Resource resource)
	{
		if (isDefined())
			return Objects.equals(resource.getId(), id.toString());
		else
			throw notDefined();
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return resourceIdColumn + sortDirectionWithSpacePrefix;
	}
}
