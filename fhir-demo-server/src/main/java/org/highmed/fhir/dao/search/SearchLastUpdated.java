package org.highmed.fhir.dao.search;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.highmed.fhir.dao.search.SearchParameter.SearchParameterDefinition;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

@SearchParameterDefinition(name = SearchLastUpdated.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Resource-lastUpdated", type = SearchParamType.TOKEN, documentation = "When the resource version last changed")
public class SearchLastUpdated extends AbstractDateTimeSearch implements SearchParameter
{
	public static final String PARAMETER_NAME = "_lastUpdated";

	private final String jsonProperty;

	private List<Object> values = new ArrayList<>();

	public SearchLastUpdated(String resourceColumn)
	{
		super(PARAMETER_NAME);

		jsonProperty = "(" + resourceColumn + "->'meta'->>'lastUpdated')";
	}

	@Override
	public String getSubquery()
	{
		return valuesAndTypes.stream().map(this::getSubquery).collect(Collectors.joining(" AND "));
	}

	private String getSubquery(DateTimeValueAndTypeAndSearchType value)
	{
		switch (value.type)
		{
			case DATE:
				return getSubquery((LocalDate) value.value, value.searchType);
			case DATE_TIME:
				return getSubquery((LocalDateTime) value.value, value.searchType);
			case YEAR_MONTH_PERIOD:
			case YEAR_PERIOD:
				return getSubquery((LocalDatePair) value.value);
			default:
				return "";
		}
	}

	private String getSubquery(LocalDateTime value, DateTimeSearchType searchType)
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
		if (value instanceof LocalDateTime)
			statement.setTimestamp(parameterIndex, Timestamp.valueOf((LocalDateTime) value));
		else if (value instanceof LocalDate)
			statement.setDate(parameterIndex, Date.valueOf((LocalDate) value));
	}

	@Override
	public void reset()
	{
		values.clear();
	}
}
