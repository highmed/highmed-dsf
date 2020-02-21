package org.highmed.pseudonymization.base;

import org.highmed.pseudonymization.bloomfilter.RecordBloomFilter;

public interface IdatEncoded
{

	TtpId getEncodedID();

	RecordBloomFilter getRBF();
}
