package org.highmed.openehr.model.datatypes;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.highmed.openehr.model.structure.RowElement;

public class ZonedDateTimeRowElement implements RowElement
{
	private final ZonedDateTime value;

	public ZonedDateTimeRowElement(ZonedDateTime value)
	{
		this.value = value;
	}

	@Override
	public String getValueAsString()
	{
		return value == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value);
	}
}
