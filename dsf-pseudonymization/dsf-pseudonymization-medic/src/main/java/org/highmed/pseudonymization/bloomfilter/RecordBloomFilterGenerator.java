package org.highmed.pseudonymization.bloomfilter;

import org.highmed.mpi.client.Idat;

public interface RecordBloomFilterGenerator
{
	RecordBloomFilter generate(Idat idat);
}
