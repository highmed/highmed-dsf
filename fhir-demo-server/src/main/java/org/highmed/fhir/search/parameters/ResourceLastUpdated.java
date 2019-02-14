package org.highmed.fhir.search.parameters;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.highmed.fhir.search.SearchParameter;
import org.highmed.fhir.webservice.search.AbstractDateTimeSearch;
import org.highmed.fhir.webservice.search.WsSearchParameter.SearchParameterDefinition;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

@SearchParameterDefinition(name = ResourceLastUpdated.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Resource-lastUpdated", type = SearchParamType.TOKEN, documentation = "When the resource version last changed")
public class ResourceLastUpdated extends AbstractDateTimeSearch implements SearchParameter
{
	public static final String PARAMETER_NAME = "_lastUpdated";

	private final String jsonProperty;

	private List<Object> values = new ArrayList<>();

	public ResourceLastUpdated(String resourceColumn)
	{
		super(PARAMETER_NAME);

		jsonProperty = "(" + resourceColumn + "->'meta'->>'lastUpdated')";
	}

	@Override
	public String getFilterQuery()
	{
		return getValuesAndTypes().stream().map(this::getSubquery).collect(Collectors.joining(" AND "));
	}

	private String getSubquery(DateTimeValueAndTypeAndSearchType value)
	{
		switch (value.type)
		{
			case ZONED_DATE_TIME:
				return getSubquery((ZonedDateTime) value.value, value.searchType);
			case LOCAL_DATE:
				return getSubquery((LocalDate) value.value, value.searchType);
			case YEAR_MONTH_PERIOD:
			case YEAR_PERIOD:
				return getSubquery((LocalDatePair) value.value);
			default:
				return "";
		}
	}

	private String getSubquery(ZonedDateTime value, DateTimeSearchType searchType)
	{
		values.add(value);

		return jsonProperty + "::timestamp " + searchType.operator + " ?";
	}

	private String getSubquery(LocalDate value, DateTimeSearchType searchType)
	{
		values.add(value);

		return jsonProperty + "::date " + searchType.operator + " ?";
	}

	private String getSubquery(LocalDatePair value)
	{
		return getSubquery(value.startInclusive, DateTimeSearchType.GE) + " AND "
				+ getSubquery(value.endExclusive, DateTimeSearchType.LT);
	}

	@Override
	public int getSqlParameterCount()
	{
		return values.size();
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement)
			throws SQLException
	{
		Object value = values.get(subqueryParameterIndex);

		if (value instanceof ZonedDateTime)
			statement.setTimestamp(parameterIndex, Timestamp
					.valueOf(((ZonedDateTime) value).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()));
		else if (value instanceof LocalDate)
			statement.setDate(parameterIndex, Date.valueOf((LocalDate) value));
	}
}
