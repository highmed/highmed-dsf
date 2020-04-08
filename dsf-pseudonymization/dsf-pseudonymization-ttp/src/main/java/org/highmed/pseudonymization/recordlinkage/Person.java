package org.highmed.pseudonymization.recordlinkage;

import java.util.BitSet;

public interface Person
{
	MedicId getMedicId();

	BitSet getRecordBloomFilter();

	default int getBloomFilterCardinality()
	{
		return getRecordBloomFilter().cardinality();
	}

	default double compareTo(Person other)
	{
		BitSet combinedBits = ((BitSet) getRecordBloomFilter().clone());
		combinedBits.and(other.getRecordBloomFilter());

		return (2.0 * combinedBits.cardinality() / (getBloomFilterCardinality() + other.getBloomFilterCardinality()));
	}
}
