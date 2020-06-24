package org.highmed.dsf.fhir.history;

import java.sql.Array;
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

import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractDateTimeParameter;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Resource;

public class AtParameter extends AbstractDateTimeParameter<DomainResource>
{
	public static final String PARAMETER_NAME = "_at";

	private List<Object> values = new ArrayList<>();

	public AtParameter()
	{
		super(PARAMETER_NAME);
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

		return "last_updated::timestamp " + searchType.operator + " ?";
	}

	private String getSubquery(LocalDate value, DateTimeSearchType searchType)
	{
		values.add(value);

		return "last_updated::date " + searchType.operator + " ?";
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
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement,
			BiFunctionWithSqlException<String, Object[], Array> arrayCreator) throws SQLException
	{
		Object value = values.get(subqueryParameterIndex - 1);

		if (value instanceof ZonedDateTime)
			statement.setTimestamp(parameterIndex, Timestamp
					.valueOf(((ZonedDateTime) value).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()));
		else if (value instanceof LocalDate)
			statement.setDate(parameterIndex, Date.valueOf((LocalDate) value));
	}

	@Override
	public boolean matches(Resource resource)
	{
		// Not implemented for history
		throw new UnsupportedOperationException();
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		// Not implemented for history
		throw new UnsupportedOperationException();
	}
}
