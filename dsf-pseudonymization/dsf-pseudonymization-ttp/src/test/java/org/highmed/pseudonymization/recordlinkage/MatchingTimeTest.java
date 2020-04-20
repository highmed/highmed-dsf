package org.highmed.pseudonymization.recordlinkage;

import static org.junit.Assert.assertNotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.highmed.pseudonymization.bloomfilter.BloomFilterGenerator;
import org.highmed.pseudonymization.bloomfilter.FieldBloomFilter;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilter;
import org.highmed.pseudonymization.domain.IdatImpl;
import org.highmed.pseudonymization.domain.PersonWithMdat;
import org.highmed.pseudonymization.domain.impl.MatchedPersonImpl;
import org.highmed.pseudonymization.domain.impl.MedicIdImpl;
import org.highmed.pseudonymization.domain.impl.OpenEhrMdatContainer;
import org.highmed.pseudonymization.domain.impl.PersonImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MatchingTimeTest
{

	private static final int BIT_SET_LENGTH = 500;
	private static final int BIT_SET_LENGTH_MEDIUM = 250;
	private static final int BIT_SET_LENGTH_SHORT = 50;
	private static List<IdatImpl> org1, org2 = new ArrayList<>();
	private static ConcurrentHashMap<List<Double>, List<Double>> results = new ConcurrentHashMap<>();
	//	private static LocalDateTime starttime, rbftime, endtime;
	private static List<Duration> rbftimes = new ArrayList<>();
	private static List<Duration> rltimes = new ArrayList<>();

	@BeforeClass
	public static void readFromTwoOrgs()
	{
		//org1 = read2(Paths.get("src/test/resources/dfD_org.csv"));
		//org1 = read2(Paths.get("src/test/resources/singlept.csv"));
		//org1 = read2(Paths.get("src/test/resources/df5k_org.csv"));
		org1 = readWithTrueNeg(Paths.get("src/test/resources/df5k_org.csv"));

		//org2 = read2(Paths.get("src/test/resources/dfD_dup.csv"));
		//org2 = read2(Paths.get("src/test/resources/singlept_dup.csv"));
		org2 = read2(Paths.get("src/test/resources/df5k_dup.csv"));
	}

	@AfterClass
	public static void writeResults() {
		String[] header = { "RBFTime", "LinkageTime" };
		FileWriter out = null;
		try
		{
			out = new FileWriter("src/test/resources/results_5k_sha1_sha2_32bHmacKey_TrueNeg_testtime.csv");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(header)))
		{
//			results.entrySet().forEach(result -> {
//				try
//				{
//					List<Double> resultList = new ArrayList<>();
//					resultList.addAll(result.getKey());
//					resultList.addAll(result.getValue());
//					printer.printRecord(resultList);
//				}
//				catch (IOException e)
//				{
//					e.printStackTrace();
//				}
//			});
			printer.print("AVG Duration BFGen: " + calculateAvgDuration(rbftimes));
			printer.print("Duration RL: " + calculateAvgDuration(rltimes));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Test
	public void testTwoOrgsOneWeightParallel() //To get more realistic times than with the parametrized Test
	{
		LocalDateTime starttime = LocalDateTime.now();

		List<TestPerson> org1Pts = new ArrayList<>();
		List<TestPerson> org2Pts = new ArrayList<>();
		List<Double> weightList = Arrays.asList(0.1,0.1,0.2,0.1,0.1,0.05,0.2,0.1,0.05); // Best weights for SHA23Hmac, MD5SHA1

		BouncyCastleProvider bcProvider = new BouncyCastleProvider();

		Random rand = new Random(42L);
		byte[] key1 = new byte[32];
		byte[] key2 = new byte[32];
		rand.nextBytes(key1);
		rand.nextBytes(key2);

//						BloomFilterGenerator generatorLong = BloomFilterGenerator.withMd5Sha1BiGramHasher(BIT_SET_LENGTH);
//						BloomFilterGenerator generatorMedium = BloomFilterGenerator.withMd5Sha1BiGramHasher(BIT_SET_LENGTH_MEDIUM);
//						BloomFilterGenerator generatorShort = BloomFilterGenerator.withMd5Sha1BiGramHasher(BIT_SET_LENGTH_SHORT);

//		BloomFilterGenerator generatorLong = BloomFilterGenerator.withHmacMd5HmacSha1BiGramHasher(BIT_SET_LENGTH, key1, key2);
//		BloomFilterGenerator generatorMedium = BloomFilterGenerator.withHmacMd5HmacSha1BiGramHasher(BIT_SET_LENGTH_MEDIUM, key1, key2);
//		BloomFilterGenerator generatorShort = BloomFilterGenerator.withHmacMd5HmacSha1BiGramHasher(BIT_SET_LENGTH_SHORT, key1, key2);

//		BloomFilterGenerator generatorLong = BloomFilterGenerator.withSha1Sha2BiGramHasher(BIT_SET_LENGTH);
//		BloomFilterGenerator generatorMedium = BloomFilterGenerator.withSha1Sha2BiGramHasher(BIT_SET_LENGTH_MEDIUM);
//		BloomFilterGenerator generatorShort = BloomFilterGenerator.withSha1Sha2BiGramHasher(BIT_SET_LENGTH_SHORT);

		BloomFilterGenerator generatorLong = BloomFilterGenerator
				.withHmacSha1HmacSha2BiGramHasher(BIT_SET_LENGTH, key1, key2, bcProvider);
		BloomFilterGenerator generatorMedium = BloomFilterGenerator
				.withHmacSha1HmacSha2BiGramHasher(BIT_SET_LENGTH_MEDIUM, key1, key2, bcProvider);
		BloomFilterGenerator generatorShort = BloomFilterGenerator
				.withHmacSha1HmacSha2BiGramHasher(BIT_SET_LENGTH_SHORT, key1, key2, bcProvider);

//						BloomFilterGenerator generatorLong = BloomFilterGenerator.withSha2Sha3BiGramHasher(BIT_SET_LENGTH);
//						BloomFilterGenerator generatorMedium = BloomFilterGenerator.withSha2Sha3BiGramHasher(BIT_SET_LENGTH_MEDIUM);
//						BloomFilterGenerator generatorShort = BloomFilterGenerator.withSha2Sha3BiGramHasher(BIT_SET_LENGTH_SHORT);

//				BloomFilterGenerator generatorLong = BloomFilterGenerator
//						.withHmacSha2HmacSha3BiGramHasher(BIT_SET_LENGTH, key1, key2,
//								bcProvider);
//				BloomFilterGenerator generatorMedium = BloomFilterGenerator
//						.withHmacSha2HmacSha3BiGramHasher(BIT_SET_LENGTH_MEDIUM, key1, key2,
//								bcProvider);
//				BloomFilterGenerator generatorShort = BloomFilterGenerator
//						.withHmacSha2HmacSha3BiGramHasher(BIT_SET_LENGTH_SHORT, key1, key2,
//								bcProvider);

		for (IdatImpl r : org1)
		{
			String recId = r.getMedicId();
			List<FieldBloomFilter> inputList = createFbfList(r, weightList, generatorLong, generatorMedium,
					generatorShort);
			org1Pts.add(createPatientFromRecord(recId, inputList, "org1"));
			inputList = null;
		}

		for (IdatImpl r : org2)
		{
			String recId = r.getMedicId();
			List<FieldBloomFilter> inputList = createFbfList(r, weightList, generatorLong, generatorMedium,
					generatorShort);
			org2Pts.add(createPatientFromRecord(recId, inputList, "org2"));
			inputList = null;
		}

		LocalDateTime rbftime = LocalDateTime.now();
		Duration rbfduration = Duration.between(starttime, rbftime);
		rbftimes.add(rbfduration);

		FederatedMatcher<TestPerson> matcher = new FederatedMatcherImpl<>(TestMatchedPerson::new);
		LocalDateTime t0 = LocalDateTime.now();
		Set<MatchedPerson<TestPerson>> matchPatients = matcher.matchPersons(org1Pts, org2Pts);
		LocalDateTime t1 = LocalDateTime.now();
		assertNotNull(matchPatients);

		LocalDateTime endtime = LocalDateTime.now();
		Duration rlduration = Duration.between(rbftime, endtime);
		rltimes.add(rlduration);

		//		//		System.out.println(
		//		//				"matched [" + org1Pts.size() + "] patients in " + Duration.between(t0, t1).toMillis() + "ms into "
		//		//						+ matchPatients.size() + " patients");
		//		List<Integer> confusionMt = calculateConfusionMatrixWithTrueNeg(matchPatients);
		//		//		System.out.println(
		//		//				"Confusion Matrix: \nTP: " + confusionMt.get(0) + "\nFP: " + confusionMt.get(1) + "\nFN: " + confusionMt
		//		//						.get(2) + "\nTN: " + confusionMt.get(3));
		//		List<Double> measures = calculateMeasures(confusionMt);
		//		//		System.out.println(
		//		//				"Measures: \nPrecision: " + measures.get(0) + "\nRecall: " + measures.get(1) + "\nAccuracy: " + measures
		//		//						.get(2) + "\nF1: " + measures.get(3));
		//
		//		double tp = confusionMt.get(0);
		//		double fp = confusionMt.get(1);
		//		double fn = confusionMt.get(2);
		//		double tn = confusionMt.get(3);
		//		List<Double> resultsList = Arrays.asList(tp,fp,fn,tn,
		//				measures.get(0),measures.get(1),measures.get(2),measures.get(3));
		//		results.put(weightList, resultsList);

		org1Pts = null;
		org2Pts = null;
		matchPatients = null;
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
		List<FieldBloomFilter> fbfList = Arrays
				.asList(firstNameInput, lastNameInput, sexInput, birthdayInput, zipCodeInput, cityInput, countryInput,
						insuranceInput, streetInput);
		return fbfList;
	}

	private TestPerson createPatientFromRecord(String id, List<FieldBloomFilter> inputList, String organization)
	{
		RecordBloomFilter rbf = new RecordBloomFilter(3000, "42L".getBytes(StandardCharsets.UTF_8), inputList);
		return new TestPerson(new MedicIdImpl(organization, id), rbf.getBitSet());
	}

	public static long calculateAvgDuration(List<Duration> durations)
	{
		if (durations.size() == 70)
		{
			for (int i = 0; i < 20; i++) {
				durations.remove(0); //Remove first 20 runs to disregard "warmup" times
			}
		}
		long sum = durations.stream().mapToLong(Duration::toMillis).sum();
		return sum / durations.size();
	}

	private static List<IdatImpl> read2(Path path)
	{
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8))
		{
			Iterable<CSVRecord> records = CSVFormat.RFC4180
					.withHeader("rec_id", "given_name", "surname", "street_number", "address_1", "address_2", "suburb",
							"postcode", "state", "date_of_birth", "soc_sec_id").withSkipHeaderRecord().parse(reader);
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
			Iterable<CSVRecord> records = CSVFormat.RFC4180
					.withHeader("rec_id", "given_name", "surname", "street_number", "address_1", "address_2", "suburb",
							"postcode", "state", "date_of_birth", "soc_sec_id").withSkipHeaderRecord().parse(reader);
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
					idats.add(new IdatImpl(r.get("rec_id"), r.get("given_name"), r.get("surname"), ptSex, bday, r.get("postcode"), r.get("address_2"), r.get("address_1"), r.get("state"),
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

	private FieldBloomFilter toField(double weight, String input, BloomFilterGenerator generator)
	{
		return new FieldBloomFilter(generator.generateBitSet(input), weight);
	}
}
