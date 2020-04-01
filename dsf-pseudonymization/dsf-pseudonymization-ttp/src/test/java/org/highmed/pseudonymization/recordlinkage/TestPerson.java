package org.highmed.pseudonymization.recordlinkage;

import java.util.BitSet;

public class TestPerson implements Person
{
	private final String organization;
	private final String id;
	private final BitSet recordBloomFilter;

	public TestPerson(String organization, String id, BitSet recordBloomFilter)
	{
		this.organization = organization;
		this.id = id;
		this.recordBloomFilter = recordBloomFilter;
	}

	@Override
	public String getOrganization()
	{
		return organization;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public BitSet getRecordBloomFilter()
	{
		return recordBloomFilter;
	}

	@Override
	public MatchedPerson toMatchedPerson()
	{
		return new TestMatchedPerson(this);
	}

	@Override
	public String toString()
	{
		return "org: " + getOrganization() + ", id: " + getId();
	}
}
