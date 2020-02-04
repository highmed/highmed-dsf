package org.highmed.pseudonymization.bloomfilter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

public class RecordBloomFilter
{
	private final int length;
	private final byte[] seed;

	private final List<FieldBloomFilter> fieldBloomFilters = new ArrayList<>();

	private BitSet bitSet;

	public RecordBloomFilter(byte[] seed, List<? extends FieldBloomFilter> fieldBloomFilters)
	{
		this(calculateLength(fieldBloomFilters), seed, fieldBloomFilters);
	}

	private static int calculateLength(List<? extends FieldBloomFilter> fieldBloomFilters)
	{
		return fieldBloomFilters == null ? 0
				: fieldBloomFilters.stream().mapToInt(f -> (int) (f.length() / f.weight())).max().orElse(0);
	}

	public RecordBloomFilter(int length, byte[] seed, List<? extends FieldBloomFilter> fieldBloomFilters)
	{
		if (fieldBloomFilters != null && fieldBloomFilters.stream().mapToDouble(f -> (double) f.weight()).sum() > 1)
			throw new IllegalArgumentException(FieldBloomFilter.class.getName() + " weight sum <= 1 expected");

		this.length = length;
		this.seed = seed;

		if (fieldBloomFilters != null)
			this.fieldBloomFilters.addAll(fieldBloomFilters);
	}

	public BitSet getBitSet()
	{
		if (bitSet == null)
			bitSet = createBitSet();

		return bitSet;
	}

	private BitSet createBitSet()
	{
		List<Boolean> bits = createSorted();
		Collections.shuffle(bits, new SecureRandom(seed));

		return toBitSet(bits);
	}

	private List<Boolean> createSorted()
	{
		List<Boolean> bits = new ArrayList<Boolean>(length);

		for (FieldBloomFilter fbf : fieldBloomFilters)
		{
			for (int b = 0; b < sampleLength(fbf.weight()); b++)
				bits.add(fbf.sample(b));
		}

		return bits;
	}

	private int sampleLength(double weight)
	{
		return (int) Math.round(length * weight);
	}

	private BitSet toBitSet(List<Boolean> bits)
	{
		BitSet bitSet = new BitSet(length);
		for (int b = 0; b < bits.size(); b++)
			bitSet.set(b, bits.get(b));

		return bitSet;
	}

	public int length()
	{
		return length;
	}
}
