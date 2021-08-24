package org.highmed.mpi.client.message;

public class QueryParameter
{
	private final String field;
	private final String value;
	private final String location;

	public static QueryParameter createQueryParameterForQpd3(String field, String value)
	{
		return new QueryParameter(field, value, "");
	}

	public static QueryParameter createQueryParameterForNonQpd3(String location, String value)
	{
		return new QueryParameter("", value, location);
	}

	private QueryParameter(String field, String value, String location)
	{
		this.field = field;
		this.value = value;
		this.location = location;
	}

	public String getField()
	{
		return field;
	}

	public String getValue()
	{
		return value;
	}

	public String getLocation()
	{
		return location;
	}

	public boolean hasLocation()
	{
		return !location.isBlank();
	}
}