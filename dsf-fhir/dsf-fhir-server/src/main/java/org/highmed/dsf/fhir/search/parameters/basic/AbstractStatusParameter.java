package org.highmed.dsf.fhir.search.parameters.basic;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.core.UriBuilder;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.search.SearchQueryParameterError.SearchQueryParameterErrorType;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.Resource;

public class AbstractStatusParameter<R extends MetadataResource> extends AbstractTokenParameter<R>
{
	public static final String PARAMETER_NAME = "status";

	private final String resourceColumn;
	private final Class<R> resourceType;

	private PublicationStatus status;

	public AbstractStatusParameter(String resourceColumn, Class<R> resourceType)
	{
		super(PARAMETER_NAME);

		this.resourceColumn = resourceColumn;
		this.resourceType = resourceType;
	}

	@Override
	protected void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		super.configureSearchParameter(queryParameters);

		if (valueAndType != null && valueAndType.type == TokenSearchType.CODE)
			status = toStatus(valueAndType.codeValue, queryParameters.get(parameterName));
	}

	private PublicationStatus toStatus(String status, List<String> parameterValues)
	{
		if (status == null || status.isBlank())
			return null;

		try
		{
			return PublicationStatus.fromCode(status);
		}
		catch (FHIRException e)
		{
			addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE, parameterName,
					parameterValues, e));
			return null;
		}
	}

	@Override
	public boolean isDefined()
	{
		return super.isDefined() && status != null;
	}

	@Override
	public String getFilterQuery()
	{
		return resourceColumn + "->>'status' " + (valueAndType.negated ? "<>" : "=") + " ?";
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
		statement.setString(parameterIndex, status.toCode());
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		bundleUri.replaceQueryParam(PARAMETER_NAME + (valueAndType.negated ? ":not" : ""), status.toCode());
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!resourceType.isInstance(resource))
			return false;

		if (valueAndType.negated)
			return !Objects.equals(((MetadataResource) resource).getStatus(), status);
		else
			return Objects.equals(((MetadataResource) resource).getStatus(), status);
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return resourceColumn + "->>'status'" + sortDirectionWithSpacePrefix;
	}
}
