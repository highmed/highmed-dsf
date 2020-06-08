package org.highmed.pseudonymization.bloomfilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RecordBloomFilter
{
	private final int length;
	private final long permutationSeed;

	private final List<FieldBloomFilter> fieldBloomFilters = new ArrayList<>();

	private BitSet bitSet;

	public RecordBloomFilter(long permutationSeed, FieldBloomFilter... fieldBloomFilters)
	{
		this(permutationSeed, Arrays.asList(fieldBloomFilters));
	}

	public RecordBloomFilter(long permutationSeed, List<? extends FieldBloomFilter> fieldBloomFilters)
	{
		this(calculateLength(fieldBloomFilters), permutationSeed, fieldBloomFilters);
	}

	private static int calculateLength(List<? extends FieldBloomFilter> fieldBloomFilters)
	{
		return fieldBloomFilters == null ? 0
				: fieldBloomFilters.stream().mapToInt(f -> (int) (f.length() / f.weight())).max().orElse(0);
	}

	public RecordBloomFilter(int length, long permutationSeed, FieldBloomFilter... fieldBloomFilters)
	{
		this(length, permutationSeed, Arrays.asList(fieldBloomFilters));
	}

	public RecordBloomFilter(int length, long permutationSeed, List<? extends FieldBloomFilter> fieldBloomFilters)
	{
		if (fieldBloomFilters != null && fieldBloomFilters.stream().mapToDouble(f -> (double) f.weight()).sum() > 1)
			throw new IllegalArgumentException(FieldBloomFilter.class.getName() + " weight sum <= 1 expected");

		this.length = length;
		this.permutationSeed = Objects.requireNonNull(permutationSeed, "permutationSeed");

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
		Collections.shuffle(bits, new Random(permutationSeed));

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
