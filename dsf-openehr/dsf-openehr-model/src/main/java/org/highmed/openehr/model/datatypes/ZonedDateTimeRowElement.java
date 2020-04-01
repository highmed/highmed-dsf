package org.highmed.openehr.model.datatypes;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.highmed.openehr.model.structure.RowElement;

public class ZonedDateTimeRowElement implements RowElement
{
	private final ZonedDateTime value;

	public ZonedDateTimeRowElement(ZonedDateTime value)
	{
		this.value = value;
	}

	public ZonedDateTime getValue()
	{
		return value;
	}

	@Override
	public String getValueAsString()
	{
		return value == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value);
	}

	public static ZonedDateTimeRowElement fromString(String value)
	{
		try
		{
			return new ZonedDateTimeRowElement(ZonedDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		}
		catch (DateTimeParseException e)
		{
			throw new RuntimeException(e);
		}
	}
}
