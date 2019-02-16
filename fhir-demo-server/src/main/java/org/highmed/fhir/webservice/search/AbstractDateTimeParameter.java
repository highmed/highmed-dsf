package org.highmed.fhir.webservice.search;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

public abstract class AbstractDateTimeParameter implements WsSearchParameter
{
	protected static enum DateTimeSearchType
	{
		EQ("eq", "="), NE("ne", "<>"), GT("gt", ">"), LT("lt", "<"), GE("ge", ">="), LE("le", "<=");

		public final String prefix;
		public final String operator;

		private DateTimeSearchType(String prefix, String operator)
		{
			this.prefix = prefix;
			this.operator = operator;
		}
	}

	protected static enum DateTimeType
	{
		ZONED_DATE_TIME, LOCAL_DATE, YEAR_PERIOD, YEAR_MONTH_PERIOD;
	}

	protected static class DateTimeValueAndTypeAndSearchType
	{
		public final Object value;
		public final DateTimeType type;
		public final DateTimeSearchType searchType;

		private DateTimeValueAndTypeAndSearchType(Object value, DateTimeType type, DateTimeSearchType searchType)
		{
			this.value = value;
			this.type = type;
			this.searchType = searchType;
		}
	}

	protected static class LocalDatePair
	{
		public final LocalDate startInclusive;
		public final LocalDate endExclusive;

		private LocalDatePair(LocalDate startInclusive, LocalDate endExclusive)
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
	private static final DateTimeFormatter DATE_TIME_FORMAT_OUT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");
	private static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

	private final String parameterName;

	private List<DateTimeValueAndTypeAndSearchType> valuesAndTypes = new ArrayList<>();

	public AbstractDateTimeParameter(String parameterName)
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
		final String fixedParameter = parameter.replace(' ', '+');

		if (Arrays.stream(DateTimeSearchType.values()).map(t -> t.prefix)
				.anyMatch(prefix -> fixedParameter.toLowerCase().startsWith(prefix)))
		{
			String prefix = fixedParameter.substring(0, 2);
			String value = fixedParameter.substring(2, fixedParameter.length()).toUpperCase();
			return parseValue(value, DateTimeSearchType.valueOf(prefix.toUpperCase()));
		}
		else
			return parseValue(fixedParameter, DateTimeSearchType.EQ);
	}

	/**
	 * @return list contains max 2 values
	 */
	public List<DateTimeValueAndTypeAndSearchType> getValuesAndTypes()
	{
		return valuesAndTypes;
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
			case ZONED_DATE_TIME:
				return ((ZonedDateTime) value.value).format(DATE_TIME_FORMAT_OUT);
			case LOCAL_DATE:
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
			return new DateTimeValueAndTypeAndSearchType(ZonedDateTime.parse(value, DATE_TIME_FORMAT),
					DateTimeType.ZONED_DATE_TIME, searchType);
		}
		catch (DateTimeParseException e)
		{
			// not a date-time, ignore
		}

		try
		{
			// TODO fix control flow by exception
			return new DateTimeValueAndTypeAndSearchType(
					ZonedDateTime.parse(value, DATE_TIME_FORMAT.withZone(ZoneId.systemDefault())),
					DateTimeType.ZONED_DATE_TIME, searchType);
		}
		catch (DateTimeParseException e)
		{
			// not a date-time, ignore
		}

		try
		{
			// TODO fix control flow by exception
			return new DateTimeValueAndTypeAndSearchType(LocalDate.parse(value, DATE_FORMAT), DateTimeType.LOCAL_DATE,
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
