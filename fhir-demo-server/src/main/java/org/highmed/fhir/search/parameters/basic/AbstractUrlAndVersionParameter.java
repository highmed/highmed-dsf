package org.highmed.fhir.search.parameters.basic;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.MetadataResource;

import com.google.common.base.Objects;

public abstract class AbstractUrlAndVersionParameter<R extends MetadataResource>
		extends AbstractCanonicalUrlParameter<R>
{
	public static final String PARAMETER_NAME = "url";

	private final String resourceColumn;

	public AbstractUrlAndVersionParameter(String resourceColumn)
	{
		super(PARAMETER_NAME);

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

	protected abstract boolean instanceOf(DomainResource resource);

	@Override
	public boolean matches(DomainResource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!instanceOf(resource))
			return false;

		MetadataResource mRes = (MetadataResource) resource;

		switch (valueAndType.type)
		{
			case PRECISE:
				return Objects.equal(mRes.getUrl(), valueAndType.url)
						&& (valueAndType.version == null || Objects.equal(mRes.getVersion(), valueAndType.version));
			case BELOW:
				return mRes.getUrl() != null && mRes.getUrl().startsWith(valueAndType.url)
						&& (valueAndType.version == null || Objects.equal(mRes.getVersion(), valueAndType.version));
			default:
				throw notDefined();
		}
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return resourceColumn + "->>'url'" + sortDirectionWithSpacePrefix;
	}
}
