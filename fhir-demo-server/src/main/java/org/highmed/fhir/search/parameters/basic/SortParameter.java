package org.highmed.fhir.search.parameters.basic;

public class SortParameter
{
	public enum SortDirection
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
	private final SortDirection direction;

	public SortParameter(String sql, SortDirection direction)
	{
		this.sql = sql;
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
}
