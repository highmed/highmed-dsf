package org.highmed.pseudonymization.bloomfilter;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.BitSet;

import org.junit.Test;

public class RecordBloomFilterTest
{
	@Test
	public void testRbfGeneration() throws Exception
	{
		BitSet bitSet1 = new BitSet(128);
		bitSet1.set(0, 64);
		FieldBloomFilter fbf1 = new FieldBloomFilter(bitSet1, 0.25);

		BitSet bitSet2 = new BitSet(256);
		bitSet2.set(64, 128);
		FieldBloomFilter fbf2 = new FieldBloomFilter(bitSet2, 0.25);

		BitSet bitSet3 = new BitSet(512);
		bitSet3.set(128, 256);
		FieldBloomFilter fbf3 = new FieldBloomFilter(bitSet3, 0.5);

		RecordBloomFilter rbf = new RecordBloomFilter(42L, Arrays.asList(fbf1, fbf2, fbf3));
		assertEquals(1024, rbf.length());

		BitSet rbfBitSet = rbf.getBitSet();

		assertEquals(320, rbfBitSet.cardinality());
		assertEquals(1024, rbfBitSet.size());
	}

	@Test
	public void testShuffling() throws Exception
	{
		BitSet bitSet1 = new BitSet(128);
		bitSet1.set(0, 64);
		FieldBloomFilter fbf1 = new FieldBloomFilter(bitSet1, 0.25);

		BitSet bitSet2 = new BitSet(256);
		bitSet2.set(64, 128);
		FieldBloomFilter fbf2 = new FieldBloomFilter(bitSet2, 0.25);

		BitSet bitSet3 = new BitSet(512);
		bitSet3.set(128, 256);
		FieldBloomFilter fbf3 = new FieldBloomFilter(bitSet3, 0.5);

		RecordBloomFilter rbf1 = new RecordBloomFilter(42L, Arrays.asList(fbf1, fbf2, fbf3));
		RecordBloomFilter rbf2 = new RecordBloomFilter(27L, Arrays.asList(fbf1, fbf2, fbf3));

		assertFalse(Arrays.equals(rbf1.getBitSet().toByteArray(), rbf2.getBitSet().toByteArray()));
	}
}
