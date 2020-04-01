package org.highmed.openehr.model.datatypes;

import org.highmed.openehr.model.structure.RowElement;

public class IntegerRowElement implements RowElement
{
	private final int value;

	public IntegerRowElement(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}

	@Override
	public String getValueAsString()
	{
		return String.valueOf(getValue());
	}

	public static IntegerRowElement fromString(String value)
	{
		try
		{
			return new IntegerRowElement(Integer.valueOf(value));
		}
		catch (NumberFormatException e)
		{
			throw e;
		}
	}
}
