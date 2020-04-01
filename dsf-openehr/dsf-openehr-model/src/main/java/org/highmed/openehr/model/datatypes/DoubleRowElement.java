package org.highmed.openehr.model.datatypes;

import org.highmed.openehr.model.structure.RowElement;

public class DoubleRowElement implements RowElement
{
	private final double value;

	public DoubleRowElement(double value)
	{
		this.value = value;
	}

	public double getValue()
	{
		return value;
	}

	@Override
	public String getValueAsString()
	{
		return String.valueOf(getValue());
	}

	public static DoubleRowElement fromString(String value)
	{
		try
		{
			return new DoubleRowElement(Double.valueOf(value));
		}
		catch (NumberFormatException e)
		{
			throw e;
		}
	}
}
