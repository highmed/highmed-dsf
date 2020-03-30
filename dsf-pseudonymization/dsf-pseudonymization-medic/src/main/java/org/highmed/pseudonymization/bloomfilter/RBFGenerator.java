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
		fbfList.add(new FieldBloomFilter(firstName, 0.1));
		fbfList.add(new FieldBloomFilter(lastName, 0.1));
		fbfList.add(new FieldBloomFilter(sex, 0.2));
		fbfList.add(new FieldBloomFilter(birthday, 0.1));
		fbfList.add(new FieldBloomFilter(zipCode, 0.1));
		fbfList.add(new FieldBloomFilter(city, 0.05));
		fbfList.add(new FieldBloomFilter(country, 0.2));
		fbfList.add(new FieldBloomFilter(insuranceNr, 0.1));
		fbfList.add(new FieldBloomFilter(street, 0.05));

		RecordBloomFilter rbf = new RecordBloomFilter(seed, fbfList);

		return rbf;

	}

}
