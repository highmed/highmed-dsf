package org.highmed.pseudonymization.base;

import java.util.BitSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bloom-Filter encoded version of a subject's IDAT
 */
public class IdatEncoded
{

	private TtpId localPsn;
	private BitSet rbf;

	@JsonCreator
	public IdatEncoded(
			@JsonProperty("localPsn")
					TtpId localPsn,
			@JsonProperty("rbf")
					BitSet rbf)
	{
		this.localPsn = localPsn;
		this.rbf = rbf;
	}

	public TtpId getEncodedID()
	{
		return localPsn;
	}

	public BitSet getRBF()
	{
		return rbf;
	}
}
