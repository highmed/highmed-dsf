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
}
