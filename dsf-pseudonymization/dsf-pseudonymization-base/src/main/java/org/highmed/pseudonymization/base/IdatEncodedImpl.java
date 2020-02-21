package org.highmed.pseudonymization.base;

import org.highmed.pseudonymization.bloomfilter.RecordBloomFilter;

public class IdatEncodedImpl implements IdatEncoded
{

	private TtpId localPsn;
	private RecordBloomFilter rbf;

	public IdatEncodedImpl(TtpId localPsn, RecordBloomFilter rbf) {
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
