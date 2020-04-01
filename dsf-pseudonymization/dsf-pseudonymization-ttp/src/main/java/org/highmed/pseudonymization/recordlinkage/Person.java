package org.highmed.pseudonymization.recordlinkage;

import java.util.BitSet;

public interface Person
{
	String getOrganization();

	String getId();

	BitSet getRecordBloomFilter();

	default int getBloomFilterCardinality()
	{
		return getRecordBloomFilter().cardinality();
	}

	default double compareTo(Person other)
	{
		// TODO look for faster combined bits calculation that doesn't require cloning
		BitSet combinedBits = ((BitSet) getRecordBloomFilter().clone());
		combinedBits.and(other.getRecordBloomFilter());

		return (2.0 * combinedBits.cardinality() / (getBloomFilterCardinality() + other.getBloomFilterCardinality()));
	}

	MatchedPerson toMatchedPerson();
}
