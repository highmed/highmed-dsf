package org.highmed.pseudonymization.base;

import java.util.BitSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bloom-Filter encoded version of a subject's IDAT
 */
public class IdatEncodedImpl implements IdatEncoded
{

	private TtpId localPsn;
	private BitSet rbf;

	@JsonCreator
	public IdatEncodedImpl(
			@JsonProperty("localPsn")
					TtpId localPsn,
			@JsonProperty("rbf")
					BitSet rbf)
	{
		this.localPsn = localPsn;
		this.rbf = rbf;
	}

	@Override
	public TtpId getEncodedID()
	{
		return localPsn;
	}

	public BitSet getRBF()
	{
		return rbf;
	}
}
