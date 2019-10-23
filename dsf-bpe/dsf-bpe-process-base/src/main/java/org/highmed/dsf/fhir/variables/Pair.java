package org.highmed.dsf.fhir.variables;

import java.io.Serializable;

public class Pair<K, V> implements Serializable
{
	private final K key;
	private final V value;

	public Pair(K key, V value)
	{
		this.key = key;
		this.value = value;
	}

	public K getKey()
	{
		return key;
	}

	public V getValue()
	{
		return value;
	}
}