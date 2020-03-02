package org.highmed.pseudonymization.bloomfilter;

import java.util.ArrayList;
import java.util.BitSet;

import org.highmed.pseudonymization.base.Idat;

public class RBFGenerator
{
	private BloomFilterGenerator bfg;

	//todo generalize
	public RBFGenerator()
	{
		this.bfg = BloomFilterGenerator.withMd5Sha1BiGramHasher(500);
	}

	public RecordBloomFilter generateRbfFromIdat(Idat idat, byte[] seed)
	{
		double weight = 0.111; // 1/9 for each of the 9 fields, experimental/testing

		BitSet firstName = bfg.generateBitSet(idat.getFirstName());
		BitSet lastName = bfg.generateBitSet(idat.getLastName());
		BitSet sex = bfg.generateBitSet(idat.getSex());
		BitSet birthday = bfg.generateBitSet(idat.getBirthday());
		BitSet zipCode = bfg.generateBitSet(idat.getZipCode());
		BitSet city = bfg.generateBitSet(idat.getCity());
		BitSet country = bfg.generateBitSet(idat.getCountry());
		BitSet insuranceNr = bfg.generateBitSet(idat.getInsuranceNr());
		BitSet street = bfg.generateBitSet(idat.getStreet());

		ArrayList<FieldBloomFilter> fbfList = new ArrayList<>();
		fbfList.add(new FieldBloomFilter(firstName, weight));
		fbfList.add(new FieldBloomFilter(lastName, weight));
		fbfList.add(new FieldBloomFilter(sex, weight));
		fbfList.add(new FieldBloomFilter(birthday, weight));
		fbfList.add(new FieldBloomFilter(zipCode, weight));
		fbfList.add(new FieldBloomFilter(city, weight));
		fbfList.add(new FieldBloomFilter(country, weight));
		fbfList.add(new FieldBloomFilter(insuranceNr, weight));
		fbfList.add(new FieldBloomFilter(street, weight));

		RecordBloomFilter rbf = new RecordBloomFilter(seed, fbfList);

		return rbf;

	}

}
