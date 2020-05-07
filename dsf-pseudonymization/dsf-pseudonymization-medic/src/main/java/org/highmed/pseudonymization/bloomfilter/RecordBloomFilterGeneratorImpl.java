package org.highmed.pseudonymization.bloomfilter;

import java.util.Objects;
import java.util.function.Supplier;

import org.highmed.pseudonymization.bloomfilter.BloomFilterGenerator.BiGramHasher;
import org.highmed.pseudonymization.mpi.Idat;

public class RecordBloomFilterGeneratorImpl implements RecordBloomFilterGenerator
{
	public static class FieldWeights
	{
		final double firstNameWeight;
		final double lastNameWeight;
		final double birthdayWeight;
		final double sexWeight;
		final double streetWeight;
		final double zipCodeWeight;
		final double cityWeight;
		final double countryWeight;
		final double insuranceNumberWeight;

		public FieldWeights(double firstNameWeight, double lastNameWeight, double birthdayWeight, double sexWeight,
				double streetWeight, double zipCodeWeight, double cityWeight, double countryWeight,
				double insuranceNumberWeight)
		{
			this.firstNameWeight = firstNameWeight;
			this.lastNameWeight = lastNameWeight;
			this.birthdayWeight = birthdayWeight;
			this.sexWeight = sexWeight;
			this.streetWeight = streetWeight;
			this.zipCodeWeight = zipCodeWeight;
			this.cityWeight = cityWeight;
			this.countryWeight = countryWeight;
			this.insuranceNumberWeight = insuranceNumberWeight;
		}
	}

	public static class FieldBloomFilterLengths
	{
		final int firstNameLength;
		final int lastNameLength;
		final int birthdayLength;
		final int sexLength;
		final int streetLength;
		final int zipCodeLength;
		final int cityLength;
		final int countryLength;
		final int insuranceNumberLength;

		public FieldBloomFilterLengths(int firstNameLength, int lastNameLength, int birthdayLength, int sexLength,
				int streetLength, int zipCodeLength, int cityLength, int countryLength, int insuranceNumberLength)
		{
			this.firstNameLength = firstNameLength;
			this.lastNameLength = lastNameLength;
			this.birthdayLength = birthdayLength;
			this.sexLength = sexLength;
			this.streetLength = streetLength;
			this.zipCodeLength = zipCodeLength;
			this.cityLength = cityLength;
			this.countryLength = countryLength;
			this.insuranceNumberLength = insuranceNumberLength;
		}
	}

	private final int length;
	private final long permutationSeed;
	private final FieldWeights weights;
	private final BloomFilterGenerator firstNameGenerator;
	private final BloomFilterGenerator lastNameGenerator;
	private final BloomFilterGenerator birthdayGenerator;
	private final BloomFilterGenerator sexGenerator;
	private final BloomFilterGenerator streetGenerator;
	private final BloomFilterGenerator zipCodeGenerator;
	private final BloomFilterGenerator cityGenerator;
	private final BloomFilterGenerator countryGenerator;
	private final BloomFilterGenerator insuranceNumberGenerator;

	public RecordBloomFilterGeneratorImpl(int length, long permutationSeed, FieldWeights weights,
			FieldBloomFilterLengths lengths, Supplier<BiGramHasher> biGramHasherSupplier)
	{
		this.length = length;
		this.permutationSeed = Objects.requireNonNull(permutationSeed, "permutationSeed");
		this.weights = Objects.requireNonNull(weights, "weights");
		Objects.requireNonNull(lengths, "lengths");
		Objects.requireNonNull(biGramHasherSupplier, "biGramHasherSupplier");

		firstNameGenerator = new BloomFilterGenerator(lengths.firstNameLength, biGramHasherSupplier);
		lastNameGenerator = new BloomFilterGenerator(lengths.lastNameLength, biGramHasherSupplier);
		birthdayGenerator = new BloomFilterGenerator(lengths.birthdayLength, biGramHasherSupplier);
		sexGenerator = new BloomFilterGenerator(lengths.sexLength, biGramHasherSupplier);
		streetGenerator = new BloomFilterGenerator(lengths.streetLength, biGramHasherSupplier);
		zipCodeGenerator = new BloomFilterGenerator(lengths.zipCodeLength, biGramHasherSupplier);
		cityGenerator = new BloomFilterGenerator(lengths.cityLength, biGramHasherSupplier);
		countryGenerator = new BloomFilterGenerator(lengths.countryLength, biGramHasherSupplier);
		insuranceNumberGenerator = new BloomFilterGenerator(lengths.insuranceNumberLength, biGramHasherSupplier);
	}

	@Override
	public RecordBloomFilter generate(Idat idat)
	{
		FieldBloomFilter f1 = new FieldBloomFilter(firstNameGenerator.generateBitSet(idat.getFirstName()),
				weights.firstNameWeight);
		FieldBloomFilter f2 = new FieldBloomFilter(lastNameGenerator.generateBitSet(idat.getLastName()),
				weights.firstNameWeight);
		FieldBloomFilter f3 = new FieldBloomFilter(birthdayGenerator.generateBitSet(idat.getBirthday()),
				weights.firstNameWeight);
		FieldBloomFilter f4 = new FieldBloomFilter(sexGenerator.generateBitSet(idat.getSex()), weights.firstNameWeight);
		FieldBloomFilter f5 = new FieldBloomFilter(streetGenerator.generateBitSet(idat.getStreet()),
				weights.firstNameWeight);
		FieldBloomFilter f6 = new FieldBloomFilter(zipCodeGenerator.generateBitSet(idat.getZipCode()),
				weights.firstNameWeight);
		FieldBloomFilter f7 = new FieldBloomFilter(cityGenerator.generateBitSet(idat.getCity()),
				weights.firstNameWeight);
		FieldBloomFilter f8 = new FieldBloomFilter(countryGenerator.generateBitSet(idat.getCountry()),
				weights.firstNameWeight);
		FieldBloomFilter f9 = new FieldBloomFilter(insuranceNumberGenerator.generateBitSet(idat.getInsuranceNumber()),
				weights.firstNameWeight);

		if (length <= 0)
			return new RecordBloomFilter(permutationSeed, f1, f2, f3, f4, f5, f6, f7, f8, f9);
		else
			return new RecordBloomFilter(length, permutationSeed, f1, f2, f3, f4, f5, f6, f7, f8, f9);
	}
}
