package org.highmed.dsf.fhir.search.parameters.basic;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.search.SearchQueryParameterError.SearchQueryParameterErrorType;
import org.hl7.fhir.r4.model.DomainResource;

public abstract class AbstractDateTimeParameter<R extends DomainResource> extends AbstractSearchParameter<R>
{
	public static enum DateTimeSearchType
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

	private List<DateTimeValueAndTypeAndSearchType> valuesAndTypes = new ArrayList<>();

	public AbstractDateTimeParameter(String parameterName)
	{
		super(parameterName);
	}

	@Override
	protected final void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		List<String> parameters = queryParameters.getOrDefault(parameterName, Collections.emptyList());

		parameters.stream().limit(2).map(value -> parse(value, parameters)).filter(v -> v != null)
				.collect(Collectors.toCollection(() -> valuesAndTypes));

		DateTimeValueAndTypeAndSearchType first = valuesAndTypes.size() < 1 ? null : valuesAndTypes.get(0);
		DateTimeValueAndTypeAndSearchType second = valuesAndTypes.size() < 2 ? null : valuesAndTypes.get(1);

		if (parameters.size() > 2)
			addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNSUPPORTED_NUMBER_OF_VALUES,
					parameterName, parameters, "More than two " + parameterName + " values"));
		else if (valuesAndTypes.size() == 2)
		{
			// if two search operators, only for example lt and gt are allowed in combination
			if (!((EnumSet.of(DateTimeSearchType.GE, DateTimeSearchType.GT).contains(first.searchType)
					&& EnumSet.of(DateTimeSearchType.LE, DateTimeSearchType.LT).contains(second.searchType))
					|| (EnumSet.of(DateTimeSearchType.GE, DateTimeSearchType.GT).contains(first.searchType)
							&& EnumSet.of(DateTimeSearchType.LE, DateTimeSearchType.LT).contains(second.searchType))))
				addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNSUPPORTED_NUMBER_OF_VALUES,
						parameterName, parameters,
						"Seach operators " + first.searchType + " and " + second.searchType + " can't be combined"));
		}

		if (valuesAndTypes.size() > 1 && (!((EnumSet.of(DateTimeSearchType.GE, DateTimeSearchType.GT)
				.contains(first.searchType)
				&& EnumSet.of(DateTimeSearchType.LE, DateTimeSearchType.LT).contains(second.searchType))
				|| (EnumSet.of(DateTimeSearchType.GE, DateTimeSearchType.GT).contains(first.searchType)
						&& EnumSet.of(DateTimeSearchType.LE, DateTimeSearchType.LT).contains(second.searchType)))))
		{
			valuesAndTypes.clear();
			valuesAndTypes.add(first);
		}
	}

	private DateTimeValueAndTypeAndSearchType parse(String parameterValue, List<String> parameterValues)
	{
		final String fixedParameterValue = parameterValue.replace(' ', '+');

		if (Arrays.stream(DateTimeSearchType.values()).map(t -> t.prefix)
				.anyMatch(prefix -> fixedParameterValue.toLowerCase().startsWith(prefix)))
		{
			String prefix = fixedParameterValue.substring(0, 2);
			String value = fixedParameterValue.substring(2, fixedParameterValue.length()).toUpperCase();
			return parseValue(value, DateTimeSearchType.valueOf(prefix.toUpperCase()), fixedParameterValue,
					parameterValues);
		}
		else
			return parseValue(fixedParameterValue, DateTimeSearchType.EQ, fixedParameterValue, parameterValues);
	}

	// yyyy-mm-ddThh:mm:ss[Z|(+|-)hh:mm]
	private DateTimeValueAndTypeAndSearchType parseValue(String value, DateTimeSearchType searchType,
			String parameterValue, List<String> parameterValues)
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

		if (DateTimeSearchType.EQ.equals(searchType))
		{
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
		}

		addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE, parameterName,
				parameterValues, parameterValue + " not parsable"));
		return null;
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
}
