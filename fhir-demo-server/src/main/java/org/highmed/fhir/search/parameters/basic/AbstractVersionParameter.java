package org.highmed.fhir.search.parameters.basic;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.MetadataResource;

import com.google.common.base.Objects;

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
	}

	@Override
	public boolean isDefined()
	{
		return super.isDefined() && version != null;
	}

	@Override
	public String getFilterQuery()
	{
		return resourceColumn + "->>'version' = ?";
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
		statement.setString(parameterIndex, version);
	}

	protected abstract boolean instanceOf(DomainResource resource);

	@Override
	public boolean matches(DomainResource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!instanceOf(resource))
			return false;

		MetadataResource mRes = (MetadataResource) resource;

		return Objects.equal(mRes.getVersion(), version);
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return resourceColumn + "->>'version'" + sortDirectionWithSpacePrefix;
	}
}
