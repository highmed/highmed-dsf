package org.highmed.pseudonymization.base;

import org.highmed.pseudonymization.bloomfilter.RecordBloomFilter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bloom-Filter encoded version of a subject's IDAT
 */
public class IdatEncodedImpl implements IdatEncoded
{

	private TtpId localPsn;
	private RecordBloomFilter rbf;

	@JsonCreator
	public IdatEncodedImpl(
			@JsonProperty("localPsn")
					TtpId localPsn,
			@JsonProperty("rbf")
					RecordBloomFilter rbf)
	{
		this.localPsn = localPsn;
		this.rbf = rbf;
	}

	@Override
	public TtpId getEncodedID()
	{
		return localPsn;
	}

	public RecordBloomFilter getRBF()
	{
		return rbf;
	}
}
