package org.highmed.pseudonymization.recordlinkage;

import java.util.BitSet;

public class TestPerson implements Person
{
	private final MedicId medicId;
	private final BitSet recordBloomFilter;

	public TestPerson(MedicId medicId, BitSet recordBloomFilter)
	{
		this.medicId = medicId;
		this.recordBloomFilter = recordBloomFilter;
	}

	@Override
	public MedicId getMedicId()
	{
		return medicId;
	}

	@Override
	public BitSet getRecordBloomFilter()
	{
		return recordBloomFilter;
	}

	@Override
	public String toString()
	{
		return "org: " + medicId.getOrganization() + ", id: " + medicId.getValue();
	}
}
