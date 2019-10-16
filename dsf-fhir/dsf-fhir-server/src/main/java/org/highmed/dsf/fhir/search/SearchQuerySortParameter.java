package org.highmed.dsf.fhir.search;

public class SearchQuerySortParameter
{
	public static enum SortDirection
	{
		ASC("", ""), DESC(" DESC", "-");

		private final String sqlModifier;
		private final String urlModifier;

		private SortDirection(String sqlModifier, String urlModifier)
		{
			this.sqlModifier = sqlModifier;
			this.urlModifier = urlModifier;
		}

		public String getSqlModifierWithSpacePrefix()
		{
			return sqlModifier;
		}

		public String getUrlModifier()
		{
			return urlModifier;
		}

		public static SortDirection fromString(String sortParameter)
		{
			if ('-' == sortParameter.charAt(0))
				return DESC;
			else
				return ASC;
		}
	}

	private final String sql;
	private final String parameterName;
	private final SortDirection direction;

	public SearchQuerySortParameter(String sql, String parameterName, SortDirection direction)
	{
		this.sql = sql;
		this.parameterName = parameterName;
		this.direction = direction;
	}

	public String getSql()
	{
		return sql;
	}

	public SortDirection getDirection()
	{
		return direction;
	}

	public String getParameterName()
	{
		return parameterName;
	}

	public String getBundleUriQueryParameterValuePart()
	{
		return getDirection().getUrlModifier() + getParameterName();
	}
}
