package org.highmed.pseudonymization.bloomfilter;

import java.util.BitSet;

public class FieldBloomFilter
{
	private final BitSet bitSet;
	private final double weight;

	public FieldBloomFilter(BitSet bitSet, double weight)
	{
		this.bitSet = bitSet;

		if (weight < 0 || weight > 1)
			throw new IllegalArgumentException("weight between 0 and 1 expected");

		this.weight = weight;
	}

	public int length()
	{
		return bitSet.size();
	}

	public double weight()
	{
		return weight;
	}

	public boolean sample(int position)
	{
		return bitSet.get(position % length());
	}
}
