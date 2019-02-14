package org.highmed.fhir.dao.search;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

public abstract class AbstractDateTimeSearch implements SearchParameter
{
	protected static enum DateTimeSearchType
	{
		EQ("eq", "="), NE("ne", "<>"), GT("gt", ">"), LT("lt", "<"), GE("ge", ">="), LE("le", "<=");

		final String prefix;
		final String operator;

		DateTimeSearchType(String prefix, String operator)
		{
			this.prefix = prefix;
			this.operator = operator;
		}
	}

	protected static enum DateTimeType
	{
		DATE_TIME, DATE, YEAR_PERIOD, YEAR_MONTH_PERIOD;
	}

	protected static class DateTimeValueAndTypeAndSearchType
	{
		final Object value;
		final DateTimeType type;
		final DateTimeSearchType searchType;

		DateTimeValueAndTypeAndSearchType(Object value, DateTimeType type, DateTimeSearchType searchType)
		{
			this.value = value;
			this.type = type;
			this.searchType = searchType;
		}
	}

	protected static class LocalDatePair
	{
		final LocalDate startInclusive;
		final LocalDate endExclusive;

		LocalDatePair(LocalDate startInclusive, LocalDate endExclusive)
		{
			this.startInclusive = startInclusive;
			this.endExclusive = endExclusive;
		}

		@Override
		public String toString()
		{
			return ">= " + startInclusive + " && < " + endExclusive;
		}
	}

	private static final Pattern YEAR_PATTERN = Pattern.compile("[0-9]{4}");
	private static final Pattern YEAR_MONTH_PATTERN = Pattern.compile("([0-9]{4})-([0-9]{2})");
	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_DATE_TIME;
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");
	private static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

	private final String parameterName;

	protected List<DateTimeValueAndTypeAndSearchType> valuesAndTypes = new ArrayList<>();

	public AbstractDateTimeSearch(String parameterName)
	{
		this.parameterName = parameterName;
	}

	public void configure(MultivaluedMap<String, String> queryParameters)
	{
		List<String> parameters = queryParameters.getOrDefault(parameterName, Collections.emptyList());
		parameters.stream().limit(2).map(this::parse).filter(v -> v != null)
				.collect(Collectors.toCollection(() -> valuesAndTypes));
	}

	private DateTimeValueAndTypeAndSearchType parse(String parameter)
	{
		if (Arrays.stream(DateTimeSearchType.values()).map(t -> t.prefix)
				.anyMatch(prefix -> parameter.toLowerCase().startsWith(prefix)))
		{
			String prefix = parameter.substring(0, 2);
			String value = parameter.substring(2, parameter.length()).toUpperCase();
			return parseValue(value, DateTimeSearchType.valueOf(prefix.toUpperCase()));
		}
		else
			return parseValue(parameter, DateTimeSearchType.EQ);
	}

	@Override
	public boolean isDefined()
	{
		return !valuesAndTypes.isEmpty();
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		bundleUri.replaceQueryParam(parameterName,
				valuesAndTypes.stream().map(value -> value.searchType.prefix + toUrlValue(value)).toArray());
	}

	private String toUrlValue(DateTimeValueAndTypeAndSearchType value)
	{
		switch (value.type)
		{
			case DATE_TIME:
				return ((LocalDateTime) value.value).format(DATE_TIME_FORMAT);
			case DATE:
				return ((LocalDate) value.value).format(DATE_FORMAT);
			case YEAR_PERIOD:
				return ((LocalDatePair) value.value).startInclusive.format(YEAR_FORMAT);
			case YEAR_MONTH_PERIOD:
				return ((LocalDatePair) value.value).startInclusive.format(YEAR_MONTH_FORMAT);
			default:
				return "";
		}
	}

	// yyyy-mm-ddThh:mm:ss[Z|(+|-)hh:mm]
	private DateTimeValueAndTypeAndSearchType parseValue(String value, DateTimeSearchType searchType)
	{
		try
		{
			// TODO fix control flow by exception
			return new DateTimeValueAndTypeAndSearchType(LocalDateTime.parse(value, DATE_TIME_FORMAT),
					DateTimeType.DATE_TIME, searchType);
		}
		catch (DateTimeParseException e)
		{
			// not a date-time, ignore
		}

		try
		{
			// TODO fix control flow by exception
			return new DateTimeValueAndTypeAndSearchType(LocalDate.parse(value, DATE_FORMAT), DateTimeType.DATE,
					searchType);
		}
		catch (DateTimeParseException e)
		{
			// not a date, ignore
		}

		Matcher yearMonthMatcher = YEAR_MONTH_PATTERN.matcher(value);
		if (yearMonthMatcher.matches())
		{
			int year = Integer.parseInt(yearMonthMatcher.group(1));
			int month = Integer.parseInt(yearMonthMatcher.group(2));
			return new DateTimeValueAndTypeAndSearchType(
					new LocalDatePair(LocalDate.of(year, month, 1), LocalDate.of(year, month, 1).plusMonths(1)),
					DateTimeType.YEAR_MONTH_PERIOD, DateTimeSearchType.EQ);
		}

		Matcher yearMatcher = YEAR_PATTERN.matcher(value);
		if (yearMatcher.matches())
		{
			int year = Integer.parseInt(yearMatcher.group());
			return new DateTimeValueAndTypeAndSearchType(
					new LocalDatePair(LocalDate.of(year, 1, 1), LocalDate.of(year, 1, 1).plusYears(1)),
					DateTimeType.YEAR_PERIOD, DateTimeSearchType.EQ);
		}

		return null;
	}
}
