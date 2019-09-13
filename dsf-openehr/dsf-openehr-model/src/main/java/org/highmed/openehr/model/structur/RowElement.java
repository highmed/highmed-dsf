package org.highmed.openehr.model.structur;

public abstract class RowElement<T>
{
	private T value;

	public RowElement(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}
}
