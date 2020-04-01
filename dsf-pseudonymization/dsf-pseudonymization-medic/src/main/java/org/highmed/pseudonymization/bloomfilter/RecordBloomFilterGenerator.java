package org.highmed.pseudonymization.bloomfilter;

import org.highmed.pseudonymization.mpi.Idat;

public interface RecordBloomFilterGenerator
{
	RecordBloomFilter generate(Idat idat);
}
