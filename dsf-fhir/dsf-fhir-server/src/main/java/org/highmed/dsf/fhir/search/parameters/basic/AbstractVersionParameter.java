package org.highmed.dsf.fhir.search.parameters.basic;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.search.SearchQueryParameterError.SearchQueryParameterErrorType;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.Resource;

public abstract class AbstractVersionParameter<R extends MetadataResource> extends AbstractTokenParameter<R>
{
	public static final String PARAMETER_NAME = "version";

	private final String resourceColumn;

	private String version;

	public AbstractVersionParameter(String resourceColumn)
	{
		super(PARAMETER_NAME);

		this.resourceColumn = resourceColumn;
	}

	@Override
	protected void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		super.configureSearchParameter(queryParameters);

		if (valueAndType != null && valueAndType.type == TokenSearchType.CODE)
			version = valueAndType.codeValue;
		else if (valueAndType != null)
			addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE, PARAMETER_NAME,
					queryParameters.get(PARAMETER_NAME)));
	}

	@Override
	public boolean isDefined()
	{
		return super.isDefined() && version != null;
	}

	@Override
	public String getFilterQuery()
	{
		return resourceColumn + "->>'version' " + (valueAndType.negated ? "<>" : "=") + " ?";
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
		statement.setString(parameterIndex, version);
	}

	protected abstract boolean instanceOf(Resource resource);

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!instanceOf(resource))
			return false;

		MetadataResource mRes = (MetadataResource) resource;

		if (valueAndType.negated)
			return !Objects.equals(mRes.getVersion(), version);
		else
			return Objects.equals(mRes.getVersion(), version);
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return resourceColumn + "->>'version'" + sortDirectionWithSpacePrefix;
	}
}
