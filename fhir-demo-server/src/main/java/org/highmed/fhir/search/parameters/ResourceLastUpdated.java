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

import org.highmed.fhir.search.parameters.basic.AbstractDateTimeParameter;
import org.highmed.fhir.search.parameters.basic.SearchParameter;
import org.highmed.fhir.search.parameters.basic.SearchParameter.SearchParameterDefinition;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

@SearchParameterDefinition(name = ResourceLastUpdated.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Resource-lastUpdated", type = SearchParamType.DATE, documentation = "When the resource version last changed")
public class ResourceLastUpdated extends AbstractDateTimeParameter<DomainResource>
{
	public static final String PARAMETER_NAME = "_lastUpdated";

	private static String toJsonProperty(String resourceColumn)
	{
		return "(" + resourceColumn + "->'meta'->>'lastUpdated')";
	}

	private final String jsonProperty;
	private List<Object> values = new ArrayList<>();

	public ResourceLastUpdated(String resourceColumn)
	{
		super(PARAMETER_NAME);

		jsonProperty = toJsonProperty(resourceColumn);
	}

	public ResourceLastUpdated(String resourceColumn, ZonedDateTime dateTime, DateTimeSearchType type)
	{
		super(PARAMETER_NAME, dateTime, type);

		jsonProperty = toJsonProperty(resourceColumn);
	}

	public ResourceLastUpdated(String resourceColumn, ZonedDateTime dateTime1, DateTimeSearchType type1,
			ZonedDateTime dateTime2, DateTimeSearchType type2)
	{
		super(PARAMETER_NAME, dateTime1, type1, dateTime2, type2);

		jsonProperty = toJsonProperty(resourceColumn);
	}

	public ResourceLastUpdated(String resourceColumn, LocalDate date, DateTimeSearchType type)
	{
		super(PARAMETER_NAME, date, type);

		jsonProperty = toJsonProperty(resourceColumn);
	}

	public ResourceLastUpdated(String resourceColumn, LocalDate date1, DateTimeSearchType type1, LocalDate date2,
			DateTimeSearchType type2)
	{
		super(PARAMETER_NAME, date1, type1, date2, type2);

		jsonProperty = toJsonProperty(resourceColumn);
	}

	public ResourceLastUpdated(String resourceColumn, int year)
	{
		super(PARAMETER_NAME, year);

		jsonProperty = toJsonProperty(resourceColumn);
	}

	public ResourceLastUpdated(String resourceColumn, int year, int month)
	{
		super(PARAMETER_NAME, year, month);

		jsonProperty = toJsonProperty(resourceColumn);
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

	@Override
	public boolean matches(DomainResource resource)
	{
		if (!isDefined())
			throw SearchParameter.notDefined();

		ZonedDateTime lastUpdated = toZonedDateTime(resource.getMeta().getLastUpdated());
		return lastUpdated != null && getValuesAndTypes().stream().allMatch(value -> matches(lastUpdated, value));
	}

	private ZonedDateTime toZonedDateTime(java.util.Date date)
	{
		if (date == null)
			return null;

		return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	private boolean matches(ZonedDateTime lastUpdated, DateTimeValueAndTypeAndSearchType value)
	{
		switch (value.type)
		{
			case ZONED_DATE_TIME:
				return matches(lastUpdated, (ZonedDateTime) value.value, value.searchType);
			case LOCAL_DATE:
				return matches(lastUpdated.toLocalDate(), (LocalDate) value.value, value.searchType);
			case YEAR_MONTH_PERIOD:
			case YEAR_PERIOD:
				return matches(lastUpdated.toLocalDate(), (LocalDatePair) value.value);
			default:
				throw SearchParameter.notDefined();
		}
	}

	private boolean matches(ZonedDateTime lastUpdated, ZonedDateTime value, DateTimeSearchType type)
	{
		switch (type)
		{
			case EQ:
				return lastUpdated.equals(value);
			case GT:
				return lastUpdated.isAfter(value);
			case GE:
				return lastUpdated.isAfter(value) || lastUpdated.equals(value);
			case LT:
				return lastUpdated.isBefore(value);
			case LE:
				return lastUpdated.isBefore(value) || lastUpdated.equals(value);
			case NE:
				return !lastUpdated.isEqual(value);
			default:
				throw SearchParameter.notDefined();
		}
	}

	private boolean matches(LocalDate lastUpdated, LocalDate value, DateTimeSearchType type)
	{
		switch (type)
		{
			case EQ:
				return lastUpdated.equals(value);
			case GT:
				return lastUpdated.isAfter(value);
			case GE:
				return lastUpdated.isAfter(value) || lastUpdated.equals(value);
			case LT:
				return lastUpdated.isBefore(value);
			case LE:
				return lastUpdated.isBefore(value) || lastUpdated.equals(value);
			case NE:
				return !lastUpdated.isEqual(value);
			default:
				throw SearchParameter.notDefined();
		}
	}

	private boolean matches(LocalDate lastUpdated, LocalDatePair value)
	{
		return (lastUpdated.isAfter(value.startInclusive) || lastUpdated.isEqual(value.startInclusive))
				&& lastUpdated.isBefore(value.endExclusive);
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return jsonProperty + "::timestamp" + sortDirectionWithSpacePrefix;
	}
}
