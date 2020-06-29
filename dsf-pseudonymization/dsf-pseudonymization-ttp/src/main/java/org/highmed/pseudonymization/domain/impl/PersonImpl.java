package org.highmed.pseudonymization.domain.impl;

import java.util.BitSet;

import org.highmed.pseudonymization.domain.MdatContainer;
import org.highmed.pseudonymization.domain.PersonWithMdat;
import org.highmed.pseudonymization.recordlinkage.MedicId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PersonImpl implements PersonWithMdat
{
	private final MedicId medicId;
	private final BitSet recordBloomFilter;
	private final MdatContainer mdatContainer;

	@JsonCreator
	public PersonImpl(@JsonProperty("medicId") MedicId medicId,
			@JsonProperty("recordBloomFilter") BitSet recordBloomFilter,
			@JsonProperty("mdatContainer") MdatContainer mdatContainer)
	{
		this.medicId = medicId;
		this.recordBloomFilter = recordBloomFilter;
		this.mdatContainer = mdatContainer;
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
	public MdatContainer getMdatContainer()
	{
		return mdatContainer;
	}

	@JsonIgnore
	@Override
	// overriding default implementation to add JsonIgnore annotation
	public int getBloomFilterCardinality()
	{
		return PersonWithMdat.super.getBloomFilterCardinality();
	}
}
