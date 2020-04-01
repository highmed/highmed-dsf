package org.highmed.pseudonymization.encoding;

import static org.junit.Assert.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.SecretKey;

import org.highmed.openehr.OpenEhrObjectMapperFactory;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.bloomfilter.BloomFilterGenerator;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGenerator;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGeneratorImpl;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGeneratorImpl.FieldBloomFilterLengths;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGeneratorImpl.FieldWeights;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.highmed.pseudonymization.mpi.Idat;
import org.highmed.pseudonymization.mpi.MasterPatientIndexClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultSetEncoderImplTest
{
	private static class IdatTestImpl implements Idat
	{
		final String medicId;
		final String firstName;
		final String lastName;
		final String birthday;
		final String sex;
		final String street;
		final String zipCode;
		final String city;
		final String country;
		final String insuranceNumber;

		IdatTestImpl(String medicId, String firstName, String lastName, String birthday, String sex, String street,
				String zipCode, String city, String country, String insuranceNumber)
		{
			this.medicId = medicId;
			this.firstName = firstName;
			this.lastName = lastName;
			this.birthday = birthday;
			this.sex = sex;
			this.street = street;
			this.zipCode = zipCode;
			this.city = city;
			this.country = country;
			this.insuranceNumber = insuranceNumber;
		}

		@Override
		public String getMedicId()
		{
			return medicId;
		}

		@Override
		public String getFirstName()
		{
			return firstName;
		}

		@Override
		public String getLastName()
		{
			return lastName;
		}

		@Override
		public String getBirthday()
		{
			return birthday;
		}

		@Override
		public String getSex()
		{
			return sex;
		}

		@Override
		public String getStreet()
		{
			return street;
		}

		@Override
		public String getZipCode()
		{
			return zipCode;
		}

		@Override
		public String getCity()
		{
			return city;
		}

		@Override
		public String getCountry()
		{
			return country;
		}

		@Override
		public String getInsuranceNumber()
		{
			return insuranceNumber;
		}
	}

	private static class MasterPatientIndexClientTestImpl implements MasterPatientIndexClient
	{
		final Map<String, Idat> idats = new HashMap<>();

		MasterPatientIndexClientTestImpl(Map<String, Idat> idats)
		{
			if (idats != null)
				this.idats.putAll(idats);
		}

		@Override
		public Idat fetchIdat(String ehrID)
		{
			return idats.get(ehrID);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(ResultSetEncoderImplTest.class);

	@Test
	public void testEncodeForTtp() throws Exception
	{
		Random random = new Random();

		int recordBloomFilterLength = 2000;
		byte[] permutationSeed = new byte[64];
		random.nextBytes(permutationSeed);

		FieldWeights weights = new FieldWeights(0.1, 0.1, 0.1, 0.2, 0.05, 0.1, 0.05, 0.2, 0.1);
		FieldBloomFilterLengths lengths = new FieldBloomFilterLengths(500, 500, 250, 50, 500, 250, 500, 500, 500);

		byte[] hashKey1 = new byte[64];
		random.nextBytes(hashKey1);
		byte[] hashKey2 = new byte[64];
		random.nextBytes(hashKey2);

		RecordBloomFilterGenerator recordBloomFilterGenerator = new RecordBloomFilterGeneratorImpl(
				recordBloomFilterLength, permutationSeed, weights, lengths,
				() -> new BloomFilterGenerator.HmacMd5HmacSha1BiGramHasher(hashKey1, hashKey2));

		Map<String, Idat> idats = Map.of("ehrId1", new IdatTestImpl("medicId1", "firstName1", "lastName1", "birthday1",
				"sex1", "street1", "zipCode1", "city1", "country1", "insuranceNumber"));
		MasterPatientIndexClient masterPatientIndexClient = new MasterPatientIndexClientTestImpl(idats);
		String organizationIdentifier = "org1";
		SecretKey organizationKey = AesGcmUtil.generateAES256Key();
		String researchStudyIdentifier = "researchStudy1";
		SecretKey researchStudyKey = AesGcmUtil.generateAES256Key();
		ObjectMapper openEhrObjectMapper = OpenEhrObjectMapperFactory.createObjectMapper();

		ResultSetEncoder encoder = new ResultSetEncoderImpl(recordBloomFilterGenerator, masterPatientIndexClient,
				organizationIdentifier, organizationKey, researchStudyIdentifier, researchStudyKey,
				openEhrObjectMapper);

		ResultSet resultSet = openEhrObjectMapper
				.readValue(Files.readAllBytes(Paths.get("src/test/resources/result_5.json")), ResultSet.class);
		assertNotNull(resultSet);

		ResultSet encodedResultSet = encoder.encode(resultSet);
		assertNotNull(encodedResultSet);

		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

		logger.debug("Encoded ResultSet {}",
				openEhrObjectMapper.writer(prettyPrinter).writeValueAsString(encodedResultSet));
	}
}
