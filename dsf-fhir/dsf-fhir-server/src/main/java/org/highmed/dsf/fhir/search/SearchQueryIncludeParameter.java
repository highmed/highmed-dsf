package org.highmed.dsf.fhir.search;

import java.sql.Connection;
import java.sql.SQLException;

import org.highmed.dsf.fhir.function.BiConsumerWithSqlException;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Resource;

public class SearchQueryIncludeParameter
{
	private final String sql;
	private final IncludeParts includeParts;

	private final BiConsumerWithSqlException<Resource, Connection> includeResourceModifier;

	public SearchQueryIncludeParameter(String sql, IncludeParts includeParts)
	{
		this(sql, includeParts, null);
	}

	/**
	 * @param sql
	 *            not <code>null</code>
	 * @param includeParts
	 *            not <code>null</code>
	 * @param includeResourceModifier
	 *            Use this {@link BiConsumerWithSqlException} to modify the include resources. This consumer can be used
	 *            if the resources returned by the include SQL are not complete and additional content needs to be
	 *            retrieved from a not included column. For example the content of a {@link Binary} resource might not
	 *            be stored in the json column.
	 */
	public SearchQueryIncludeParameter(String sql, IncludeParts includeParts,
			BiConsumerWithSqlException<Resource, Connection> includeResourceModifier)
	{
		this.sql = sql;
		this.includeParts = includeParts;
		this.includeResourceModifier = includeResourceModifier;
	}

	public String getBundleUriQueryParameterValues()
	{
		return includeParts.toBundleUriQueryParameterValue();
	}

	public String getSql()
	{
		return sql;
	}

	public void modifyIncludeResource(Resource resource, Connection connection) throws SQLException
	{
		if (includeResourceModifier != null)
			includeResourceModifier.accept(resource, connection);
	}
}
