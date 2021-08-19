package org.highmed.pseudonymization.recordlinkage;

import static org.junit.Assert.assertNotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.highmed.pseudonymization.bloomfilter.BloomFilterGenerator;
import org.highmed.pseudonymization.bloomfilter.FieldBloomFilter;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilter;
import org.highmed.pseudonymization.domain.IdatImpl;
import org.highmed.pseudonymization.domain.impl.MedicIdImpl;
import org.junit.BeforeClass;
import org.junit.Test;

public class WeightDistributionTest
{
	private static final int BIT_SET_LENGTH = 500;
	private static final int BIT_SET_LENGTH_MEDIUM = 250;
	private static final int BIT_SET_LENGTH_SHORT = 50;

	private static ConcurrentHashMap<List<Double>, List<Double>> results = new ConcurrentHashMap<>();

	private static List<IdatImpl> org1, org2 = new ArrayList<>();

	@BeforeClass
	public static void readFromTwoOrgs()
	{
		org1 = readWithTrueNeg(Paths.get("src/test/resources/df5k_org.csv"));

		org2 = read2(Paths.get("src/test/resources/df5k_dup.csv"));
	}

	@Test
	public void testWeightsWithMd5Sha1()
	{
		BloomFilterGenerator generatorLong = BloomFilterGenerator.withMd5Sha1BiGramHasher(BIT_SET_LENGTH);
		BloomFilterGenerator generatorMedium = BloomFilterGenerator.withMd5Sha1BiGramHasher(BIT_SET_LENGTH_MEDIUM);
		BloomFilterGenerator generatorShort = BloomFilterGenerator.withMd5Sha1BiGramHasher(BIT_SET_LENGTH_SHORT);

		List<List<Double>> weightLists = getWeightLists();

		// Todo: Run tests in parallel with different weightlists once ConcModException is fixed
		testWeights(weightLists.get(0), generatorLong, generatorMedium, generatorShort);

		writeResultsAsCsv("Md5_SHA1.csv");
	}

	@Test
	public void testWeightsWithMd5Sha1Hmac()
	{
		Random rand = new Random(42L);
		byte[] key1 = new byte[32];
		byte[] key2 = new byte[32];
		rand.nextBytes(key1);
		rand.nextBytes(key2);

		BloomFilterGenerator generatorLong = BloomFilterGenerator.withHmacMd5HmacSha1BiGramHasher(BIT_SET_LENGTH, key1,
				key2);
		BloomFilterGenerator generatorMedium = BloomFilterGenerator
				.withHmacMd5HmacSha1BiGramHasher(BIT_SET_LENGTH_MEDIUM, key1, key2);
		BloomFilterGenerator generatorShort = BloomFilterGenerator.withHmacMd5HmacSha1BiGramHasher(BIT_SET_LENGTH_SHORT,
				key1, key2);

		List<List<Double>> weightLists = getWeightLists();

		testWeights(weightLists.get(0), generatorLong, generatorMedium, generatorShort);

		writeResultsAsCsv("Md5_SHA1_HMAC.csv");
	}

	@Test
	public void testWeightsWithSha1Sha2()
	{
		BloomFilterGenerator generatorLong = BloomFilterGenerator.withSha1Sha2BiGramHasher(BIT_SET_LENGTH);
		BloomFilterGenerator generatorMedium = BloomFilterGenerator.withSha1Sha2BiGramHasher(BIT_SET_LENGTH_MEDIUM);
		BloomFilterGenerator generatorShort = BloomFilterGenerator.withSha1Sha2BiGramHasher(BIT_SET_LENGTH_SHORT);

		List<List<Double>> weightLists = getWeightLists();

		testWeights(weightLists.get(0), generatorLong, generatorMedium, generatorShort);

		writeResultsAsCsv("SHA1_SHA2.csv");
	}

	@Test
	public void testWeightsWithSha1Sha2Hmac()
	{
		BouncyCastleProvider bcProvider = new BouncyCastleProvider();

		Random rand = new Random(42L);
		byte[] key1 = new byte[32];
		byte[] key2 = new byte[32];
		rand.nextBytes(key1);
		rand.nextBytes(key2);

		BloomFilterGenerator generatorLong = BloomFilterGenerator.withHmacSha1HmacSha2BiGramHasher(BIT_SET_LENGTH, key1,
				key2, bcProvider);
		BloomFilterGenerator generatorMedium = BloomFilterGenerator
				.withHmacSha1HmacSha2BiGramHasher(BIT_SET_LENGTH_MEDIUM, key1, key2, bcProvider);
		BloomFilterGenerator generatorShort = BloomFilterGenerator
				.withHmacSha1HmacSha2BiGramHasher(BIT_SET_LENGTH_SHORT, key1, key2, bcProvider);

		List<List<Double>> weightLists = getWeightLists();

		testWeights(weightLists.get(0), generatorLong, generatorMedium, generatorShort);

		writeResultsAsCsv("SHA1_HMAC_SHA2_HMAC.csv");
	}

	@Test
	public void testWeightsWithSha2Sha3()
	{
		BloomFilterGenerator generatorLong = BloomFilterGenerator.withSha2Sha3BiGramHasher(BIT_SET_LENGTH);
		BloomFilterGenerator generatorMedium = BloomFilterGenerator.withSha2Sha3BiGramHasher(BIT_SET_LENGTH_MEDIUM);
		BloomFilterGenerator generatorShort = BloomFilterGenerator.withSha2Sha3BiGramHasher(BIT_SET_LENGTH_SHORT);

		List<List<Double>> weightLists = getWeightLists();

		testWeights(weightLists.get(0), generatorLong, generatorMedium, generatorShort);

		writeResultsAsCsv("SHA2_SHA3.csv");
	}

	@Test
	public void testWeightsWithSha2Sha3Hmac()
	{
		BouncyCastleProvider bcProvider = new BouncyCastleProvider();

		Random rand = new Random(42L);
		byte[] key1 = new byte[32];
		byte[] key2 = new byte[32];
		rand.nextBytes(key1);
		rand.nextBytes(key2);

		BloomFilterGenerator generatorLong = BloomFilterGenerator.withHmacSha2HmacSha3BiGramHasher(BIT_SET_LENGTH, key1,
				key2, bcProvider);
		BloomFilterGenerator generatorMedium = BloomFilterGenerator
				.withHmacSha2HmacSha3BiGramHasher(BIT_SET_LENGTH_MEDIUM, key1, key2, bcProvider);
		BloomFilterGenerator generatorShort = BloomFilterGenerator
				.withHmacSha2HmacSha3BiGramHasher(BIT_SET_LENGTH_SHORT, key1, key2, bcProvider);

		List<List<Double>> weightLists = getWeightLists();

		testWeights(weightLists.get(0), generatorLong, generatorMedium, generatorShort);

		writeResultsAsCsv("SHA2_HMAC_SHA3_HMAC.csv");
	}

	private void testWeights(List<Double> weightList, BloomFilterGenerator generatorLong,
			BloomFilterGenerator generatorMedium, BloomFilterGenerator generatorShort)
	{
		List<List<TestPerson>> combinedPtRbfs = createPatientsFromRecord(org1, org2, weightList, generatorLong,
				generatorMedium, generatorShort);
		List<TestPerson> org1Pts = combinedPtRbfs.get(0);
		List<TestPerson> org2Pts = combinedPtRbfs.get(1);

		FederatedMatcher<TestPerson> matcher = new FederatedMatcherImpl<>(TestMatchedPerson::new);
		Set<MatchedPerson<TestPerson>> matchPatients = matcher.matchPersons(Arrays.asList(org1Pts, org2Pts));
		assertNotNull(matchPatients);

		List<Integer> confusionMt = calculateConfusionMatrixWithTrueNeg(matchPatients);
		List<Double> measures = calculateMeasures(confusionMt);

		double tp = confusionMt.get(0);
		double fp = confusionMt.get(1);
		double fn = confusionMt.get(2);
		double tn = confusionMt.get(3);

		List<Double> resultsList = Arrays.asList(tp, fp, fn, tn, measures.get(0), measures.get(1), measures.get(2),
				measures.get(3));
		results.put(weightList, resultsList);
	}

	private List<Double> calculateMeasures(List<Integer> confMt)
	{
		double tp = confMt.get(0);
		double fp = confMt.get(1);
		double fn = confMt.get(2);
		double tn = confMt.get(3);

		double precision = tp / (tp + fp);
		double recall = tp / (tp + fn);
		double accuracy = (tp + tn) / (tp + fp + fn + tn);
		double f1 = 2 * ((precision * recall) / (precision + recall));

		return Arrays.asList(precision, recall, accuracy, f1);
	}

	private List<Integer> calculateConfusionMatrixWithTrueNeg(Set<MatchedPerson<TestPerson>> matchPatients)
	{
		int truePos = 0;
		int falsePos = 0;
		int trueNeg = 0;
		int falseNeg = 0;

		for (MatchedPerson<TestPerson> mP : matchPatients)
		{
			if (mP.getMatches().size() == 1)
			{
				String mPId = mP.getFirstMatch().getMedicId().getValue().substring(0, 8).replaceAll("\\D+", "");
				boolean isInOrg1 = false;
				boolean isInOrg2 = false;

				for (IdatImpl pt : org1)
				{
					String ptId1 = pt.getMedicId().substring(0, 8).replaceAll("\\D+", "");
					if (ptId1.equals(mPId))
					{
						isInOrg1 = true;
						break;
					}
				}
				for (IdatImpl pt : org2)
				{
					String ptId2 = pt.getMedicId().substring(0, 8).replaceAll("\\D+", "");
					if (ptId2.equals(mPId))
					{
						isInOrg2 = true;
						break;
					}
				}

				if (isInOrg1 && isInOrg2)
				{
					falseNeg++;
				}
				else
				{
					trueNeg++;
				}
			}
			else if (mP.getMatches().size() == 2)
			{
				String firstID = mP.getFirstMatch().getMedicId().getValue().substring(0, 8).replaceAll("\\D+", "");
				String lastID = mP.getLastMatch().getMedicId().getValue().substring(0, 8).replaceAll("\\D+", "");
				if (firstID.equals(lastID))
				{
					truePos++;
				}
				else
				{
					falsePos++;
				}
			}
			else
			{
				String firstID = mP.getFirstMatch().getMedicId().getValue().substring(0, 8).replaceAll("\\D+", "");
				for (int i = 1; i < mP.getMatches().size(); i++)
				{
					Person match = (Person) mP.getMatches().get(i);
					String lastID = match.getMedicId().getValue().substring(0, 8).replaceAll("\\D+", "");
					if (firstID.equals(lastID))
					{
						truePos++;
					}
					else
					{
						falsePos++;
					}
				}
			}
		}

		falseNeg /= 2; // Since theyre unmatched, false negatives get counted twice
		return Arrays.asList(truePos, falsePos, falseNeg, trueNeg);
	}

	private List<List<TestPerson>> createPatientsFromRecord(List<IdatImpl> org1Idat, List<IdatImpl> org2Idat,
			List<Double> weightList, BloomFilterGenerator generatorLong, BloomFilterGenerator generatorMedium,
			BloomFilterGenerator generatorShort)
	{
		List<TestPerson> org1Pts = new ArrayList<>();
		List<TestPerson> org2Pts = new ArrayList<>();

		List<IdatImpl> allPts = new ArrayList<>();
		allPts.addAll(org1Idat);
		allPts.addAll(org2Idat);

		List<RecordBloomFilter> rbfList = allPts.parallelStream().map(r ->
		{
			return new RecordBloomFilter(3000, 42L,
					createFbfList(r, weightList, generatorLong, generatorMedium, generatorShort));
		}).collect(Collectors.toList());

		for (IdatImpl r : allPts)
		{
			int idx = allPts.indexOf(r);
			if (r.getMedicId().contains("org")) // Org 1 IDAT have org (original) id, org 2 idat have dup
			{
				org1Pts.add(new TestPerson(new MedicIdImpl("org1", r.getMedicId()), rbfList.get(idx).getBitSet()));
			}
			else
			{
				org2Pts.add(new TestPerson(new MedicIdImpl("org2", r.getMedicId()), rbfList.get(idx).getBitSet()));
			}
		}

		return Arrays.asList(org1Pts, org2Pts);
	}

	private List<FieldBloomFilter> createFbfList(IdatImpl r, List<Double> weightList,
			BloomFilterGenerator generatorLong, BloomFilterGenerator generatorMedium,
			BloomFilterGenerator generatorShort)
	{
		double firstname = weightList.get(0);
		double lastname = weightList.get(1);
		double sex = weightList.get(2);
		double birthday = weightList.get(3);
		double zipcode = weightList.get(4);
		double city = weightList.get(5);
		double country = weightList.get(6);
		double insurancenr = weightList.get(7);
		double street = weightList.get(8);

		String recId = r.getMedicId();
		FieldBloomFilter firstNameInput = toField(firstname, r.getFirstName(), generatorLong);
		FieldBloomFilter lastNameInput = toField(lastname, r.getLastName(), generatorLong);
		String ptSex = "f";
		String idNr = recId.substring(0, 8).replaceAll("\\D+", "");
		if (Integer.parseInt(idNr) % 2 == 0)
			ptSex = "m";
		FieldBloomFilter sexInput = toField(sex, ptSex, generatorShort);
		FieldBloomFilter birthdayInput = toField(birthday, r.getBirthday(), generatorMedium);
		FieldBloomFilter zipCodeInput = toField(zipcode, r.getZipCode(), generatorMedium);
		FieldBloomFilter cityInput = toField(city, r.getCity(), generatorLong);
		FieldBloomFilter countryInput = toField(country, r.getCountry(), generatorLong);
		FieldBloomFilter insuranceInput = toField(insurancenr, r.getInsuranceNumber(), generatorLong);
		FieldBloomFilter streetInput = toField(street, r.getStreet(), generatorLong);
		List<FieldBloomFilter> fbfList = Arrays.asList(firstNameInput, lastNameInput, sexInput, birthdayInput,
				zipCodeInput, cityInput, countryInput, insuranceInput, streetInput);
		return fbfList;
	}

	private FieldBloomFilter toField(double weight, String input, BloomFilterGenerator generator)
	{
		return new FieldBloomFilter(generator.generateBitSet(input), weight);
	}

	private static List<IdatImpl> read2(Path path)
	{
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8))
		{
			Iterable<CSVRecord> records = CSVFormat.RFC4180.builder()
					.setHeader("rec_id", "given_name", "surname", "street_number", "address_1", "address_2", "suburb",
							"postcode", "state", "date_of_birth", "soc_sec_id")
					.setSkipHeaderRecord(true).build().parse(reader);
			ArrayList<IdatImpl> idats = new ArrayList<>();
			for (CSVRecord r : records)
			{
				String ptSex = "f";
				String idNr = r.get("rec_id").substring(0, 8).replaceAll("\\D+", "");
				if (Integer.parseInt(idNr) % 2 == 0)
					ptSex = "m";
				String bday;
				if (r.get("date_of_birth").length() == 8)
				{
					String bdayYear = r.get("date_of_birth").substring(0, 4);
					String bdayMonth = r.get("date_of_birth").substring(4, 6);
					String bdayDay = r.get("date_of_birth").substring(6);
					bday = bdayDay + "." + bdayMonth + "." + bdayYear;
				}
				else
				{
					bday = "NaN";
				}
				idats.add(new IdatImpl(r.get("rec_id"), r.get("given_name"), r.get("surname"), ptSex, bday,
						r.get("postcode"), r.get("address_2"), r.get("address_1"), r.get("state"),
						r.get("soc_sec_id")));
			}
			return idats;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static List<IdatImpl> readWithTrueNeg(Path path)
	{
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8))
		{
			Iterable<CSVRecord> records = CSVFormat.RFC4180.builder()
					.setHeader("rec_id", "given_name", "surname", "street_number", "address_1", "address_2", "suburb",
							"postcode", "state", "date_of_birth", "soc_sec_id")
					.setSkipHeaderRecord(true).build().parse(reader);
			ArrayList<IdatImpl> idats = new ArrayList<>();
			for (CSVRecord r : records)
			{
				String ptSex = "f";
				String idNr = r.get("rec_id").substring(0, 8).replaceAll("\\D+", "");

				if (Integer.parseInt(idNr) % 2 == 0)
					ptSex = "m";
				String bday;
				if (r.get("date_of_birth").length() == 8)
				{
					String bdayYear = r.get("date_of_birth").substring(0, 4);
					String bdayMonth = r.get("date_of_birth").substring(4, 6);
					String bdayDay = r.get("date_of_birth").substring(6);
					bday = bdayDay + "." + bdayMonth + "." + bdayYear;
				}
				else
				{
					bday = "NaN";
				}
				if (Integer.parseInt(idNr) < 4000)
				{
					idats.add(new IdatImpl(r.get("rec_id"), r.get("given_name"), r.get("surname"), ptSex, bday,
							r.get("postcode"), r.get("address_2"), r.get("address_1"), r.get("state"),
							r.get("soc_sec_id")));
				}
			}
			return idats;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static List<List<Double>> getWeightLists()
	{
		List<List<Double>> top10 = Arrays.asList(Arrays.asList(0.1, 0.1, 0.2, 0.05, 0.15, 0.1, 0.15, 0.1, 0.05),
				Arrays.asList(0.1, 0.1, 0.2, 0.1, 0.1, 0.1, 0.2, 0.05, 0.05),
				Arrays.asList(0.1, 0.1, 0.2, 0.05, 0.15, 0.05, 0.2, 0.1, 0.05),
				Arrays.asList(0.1, 0.1, 0.2, 0.1, 0.1, 0.05, 0.2, 0.1, 0.05),
				Arrays.asList(0.1, 0.1, 0.2, 0.05, 0.15, 0.05, 0.15, 0.1, 0.1),
				Arrays.asList(0.1, 0.1, 0.2, 0.1, 0.05, 0.05, 0.2, 0.1, 0.1),
				Arrays.asList(0.1, 0.1, 0.2, 0.1, 0.05, 0.1, 0.2, 0.1, 0.05),
				Arrays.asList(0.1, 0.1, 0.2, 0.1, 0.1, 0.05, 0.15, 0.1, 0.1),
				Arrays.asList(0.1, 0.1, 0.2, 0.05, 0.1, 0.05, 0.15, 0.15, 0.1),
				Arrays.asList(0.1, 0.1, 0.2, 0.1, 0.1, 0.05, 0.2, 0.05, 0.1));

		return top10;
	}

	public static void writeResultsAsCsv(String filename)
	{
		String[] header = { "firstname", "lastname", "sex", "birthday", "zipcode", "city", "country", "insurancenr",
				"street", "tp", "fp", "fn", "tn", "Prec", "Rec", "Acc", "F1" };
		FileWriter out = null;
		try
		{
			out = new FileWriter("src/test/resources/" + filename);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.builder().setHeader(header).build()))
		{
			results.entrySet().forEach(result ->
			{
				try
				{
					List<Double> resultList = new ArrayList<>();
					resultList.addAll(result.getKey());
					resultList.addAll(result.getValue());
					printer.printRecord(resultList);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			});
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
