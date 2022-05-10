package org.highmed.pseudonymization.translation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;

import org.highmed.mpi.client.Idat;
import org.highmed.mpi.client.IdatNotFoundException;
import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.openehr.json.OpenEhrObjectMapperFactory;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.bloomfilter.BloomFilterGenerator;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGenerator;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGeneratorImpl;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGeneratorImpl.FieldBloomFilterLengths;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGeneratorImpl.FieldWeights;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultSetTranslatorToTtpCreateRbfTest
{
	private static final Logger logger = LoggerFactory.getLogger(ResultSetTranslatorToTtpCreateRbfTest.class);

	@Test
	public void testTranslateForTtp() throws Exception
	{
		Random random = new Random();

		int recordBloomFilterLength = 2000;
		long permutationSeed = 42L;

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
				"sex1", "street1", "zipCode1", "city1", "country1", "insuranceNumber1"));
		MasterPatientIndexClient masterPatientIndexClient = new MasterPatientIndexClientTestImpl(idats);

		ResultSetTranslatorToTtpCreateRbfImpl translator = new ResultSetTranslatorToTtpCreateRbfImpl(
				"/ehr_status/subject/external_ref/id/value", recordBloomFilterGenerator, masterPatientIndexClient);

		ObjectMapper openEhrObjectMapper = OpenEhrObjectMapperFactory.createObjectMapper();
		ResultSet resultSet = openEhrObjectMapper
				.readValue(Files.readAllBytes(Paths.get("src/test/resources/result_5.json")), ResultSet.class);
		assertNotNull(resultSet);
		assertNotNull(resultSet.getColumns());
		assertEquals(4, resultSet.getColumns().size());
		assertNotNull(resultSet.getRows());
		assertEquals(1, resultSet.getRows().size());
		assertNotNull(resultSet.getRow(0));
		assertEquals(4, resultSet.getRow(0).size());

		ResultSet translatedResultSet = translator.translate(resultSet);
		assertNotNull(translatedResultSet);
		assertNotNull(translatedResultSet.getColumns());
		assertEquals(5, translatedResultSet.getColumns().size());
		assertNotNull(translatedResultSet.getRows());
		assertEquals(1, translatedResultSet.getRows().size());
		assertNotNull(translatedResultSet.getRow(0));
		assertEquals(5, translatedResultSet.getRow(0).size());

		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

		logger.debug("Encoded ResultSet {}",
				openEhrObjectMapper.writer(prettyPrinter).writeValueAsString(translatedResultSet));
	}

	@Test(expected = IdatNotFoundException.class)
	public void testTranslateForTtpFailingMpiClientFails() throws Exception
	{
		Random random = new Random();

		int recordBloomFilterLength = 2000;
		long permutationSeed = 42L;

		FieldWeights weights = new FieldWeights(0.1, 0.1, 0.1, 0.2, 0.05, 0.1, 0.05, 0.2, 0.1);
		FieldBloomFilterLengths lengths = new FieldBloomFilterLengths(500, 500, 250, 50, 500, 250, 500, 500, 500);

		byte[] hashKey1 = new byte[64];
		random.nextBytes(hashKey1);
		byte[] hashKey2 = new byte[64];
		random.nextBytes(hashKey2);

		RecordBloomFilterGenerator recordBloomFilterGenerator = new RecordBloomFilterGeneratorImpl(
				recordBloomFilterLength, permutationSeed, weights, lengths,
				() -> new BloomFilterGenerator.HmacMd5HmacSha1BiGramHasher(hashKey1, hashKey2));

		MasterPatientIndexClient masterPatientIndexClient = id ->
		{
			throw new IdatNotFoundException(id);
		};

		ResultSetTranslatorToTtpCreateRbfImpl translator = new ResultSetTranslatorToTtpCreateRbfImpl(
				"/ehr_status/subject/external_ref/id/value", recordBloomFilterGenerator, masterPatientIndexClient);

		ObjectMapper openEhrObjectMapper = OpenEhrObjectMapperFactory.createObjectMapper();
		ResultSet resultSet = openEhrObjectMapper
				.readValue(Files.readAllBytes(Paths.get("src/test/resources/result_5.json")), ResultSet.class);
		assertNotNull(resultSet);
		assertNotNull(resultSet.getColumns());
		assertEquals(4, resultSet.getColumns().size());
		assertNotNull(resultSet.getRows());
		assertEquals(1, resultSet.getRows().size());
		assertNotNull(resultSet.getRow(0));
		assertEquals(4, resultSet.getRow(0).size());

		translator.translate(resultSet);
	}

	@Test
	public void testTranslateForTtpFailingMpiClientFilters() throws Exception
	{
		Random random = new Random();

		int recordBloomFilterLength = 2000;
		long permutationSeed = 42L;

		FieldWeights weights = new FieldWeights(0.1, 0.1, 0.1, 0.2, 0.05, 0.1, 0.05, 0.2, 0.1);
		FieldBloomFilterLengths lengths = new FieldBloomFilterLengths(500, 500, 250, 50, 500, 250, 500, 500, 500);

		byte[] hashKey1 = new byte[64];
		random.nextBytes(hashKey1);
		byte[] hashKey2 = new byte[64];
		random.nextBytes(hashKey2);

		RecordBloomFilterGenerator recordBloomFilterGenerator = new RecordBloomFilterGeneratorImpl(
				recordBloomFilterLength, permutationSeed, weights, lengths,
				() -> new BloomFilterGenerator.HmacMd5HmacSha1BiGramHasher(hashKey1, hashKey2));

		MasterPatientIndexClient masterPatientIndexClient = id ->
		{
			throw new IdatNotFoundException(id);
		};

		ResultSetTranslatorToTtpCreateRbfImpl translator = new ResultSetTranslatorToTtpCreateRbfImpl(
				"/ehr_status/subject/external_ref/id/value", recordBloomFilterGenerator, masterPatientIndexClient,
				ResultSetTranslatorToTtpCreateRbfImpl.FILTER_ON_IDAT_NOT_FOUND_EXCEPTION);

		ObjectMapper openEhrObjectMapper = OpenEhrObjectMapperFactory.createObjectMapper();
		ResultSet resultSet = openEhrObjectMapper
				.readValue(Files.readAllBytes(Paths.get("src/test/resources/result_5.json")), ResultSet.class);
		assertNotNull(resultSet);
		assertNotNull(resultSet.getColumns());
		assertEquals(4, resultSet.getColumns().size());
		assertNotNull(resultSet.getRows());
		assertEquals(1, resultSet.getRows().size());
		assertNotNull(resultSet.getRow(0));
		assertEquals(4, resultSet.getRow(0).size());

		ResultSet translate = translator.translate(resultSet);
		assertNotNull(translate);
		assertNotNull(translate.getRows());
		assertEquals(0, translate.getRows().size());
	}
}
