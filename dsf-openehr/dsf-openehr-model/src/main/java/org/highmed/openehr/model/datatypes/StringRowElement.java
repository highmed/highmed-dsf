package org.highmed.openehr.model.datatypes;

import org.highmed.openehr.model.structure.RowElement;

public class StringRowElement implements RowElement
{
	private final String value;

	public StringRowElement(String value)
	{
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	@Override
	public String getValueAsString()
	{
		return getValue();
	}

	public static StringRowElement fromString(String value)
	{
		return new StringRowElement(value);
	}
}
