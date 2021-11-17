package org.highmed.pseudonymization.translation;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.highmed.mpi.client.Idat;
import org.highmed.mpi.client.IdatNotFoundException;
import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.openehr.json.OpenEhrObjectMapperFactory;
import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.openehr.model.structure.RowElement;
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

public class ResultSetTranslatorToTtpTestCreateRbfOnlyDropOthers
{
	private static final Logger logger = LoggerFactory
			.getLogger(ResultSetTranslatorToTtpTestCreateRbfOnlyDropOthers.class);

	@Test
	public void testTranslateToTtp() throws Exception
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

		Map<String, Idat> idats = new HashMap<>();
		IntStream.range(0, 15).mapToObj(String::valueOf)
				.forEach(id -> idats.put(id,
						new IdatTestImpl("medicId" + id, "firstName" + id, "lastName" + id, "birthday" + id, "sex" + id,
								"street" + id, "zipCode" + id, "city" + id, "country" + id, "insuranceNumber" + id)));

		MasterPatientIndexClient masterPatientIndexClient = new MasterPatientIndexClientTestImpl(idats);

		ResultSetTranslatorToTtpCreateRbfOnlyDropOtherColumnsImpl translator = new ResultSetTranslatorToTtpCreateRbfOnlyDropOtherColumnsImpl(
				"/ehr_status/subject/external_ref/id/value", recordBloomFilterGenerator, masterPatientIndexClient);

		ObjectMapper openEhrObjectMapper = OpenEhrObjectMapperFactory.createObjectMapper();
		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

		List<List<RowElement>> rows = IntStream.range(0, 15)
				.mapToObj(id -> Collections.<RowElement> singletonList(new StringRowElement(String.valueOf(id))))
				.collect(Collectors.toList());
		ResultSet resultSet = new ResultSet(null, null,
				"SELECT e/ehr_status/subject/external_ref/id/value as EHRID FROM EHR e",
				Collections.singleton(new Column("EHRID", "/ehr_status/subject/external_ref/id/value")), rows);

		logger.debug("ResultSet {}", openEhrObjectMapper.writer(prettyPrinter).writeValueAsString(resultSet));
		ResultSet translated = translator.translate(resultSet);
		assertNotNull(translated);
		logger.debug("Encoded ResultSet {}", openEhrObjectMapper.writer(prettyPrinter).writeValueAsString(translated));

		assertNotNull(translated.getColumns());
		assertEquals(1, translated.getColumns().size());
		assertNotNull(translated.getRows());
		assertEquals(15, translated.getRows().size());
		assertEquals(15, translated.getRows().stream().mapToInt(List::size).sum());
	}

	@Test(expected = IdatNotFoundException.class)
	public void testIdsOnlyFailingMpiClientFails() throws Exception
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

		ResultSetTranslatorToTtpCreateRbfOnlyDropOtherColumnsImpl translator = new ResultSetTranslatorToTtpCreateRbfOnlyDropOtherColumnsImpl(
				"/ehr_status/subject/external_ref/id/value", recordBloomFilterGenerator, masterPatientIndexClient);

		List<List<RowElement>> rows = IntStream.range(0, 15)
				.mapToObj(id -> Collections.<RowElement> singletonList(new StringRowElement(String.valueOf(id))))
				.collect(Collectors.toList());
		ResultSet resultSet = new ResultSet(null, null,
				"SELECT e/ehr_status/subject/external_ref/id/value as EHRID FROM EHR e",
				Collections.singleton(new Column("EHRID", "/ehr_status/subject/external_ref/id/value")), rows);

		translator.translate(resultSet);
	}

	@Test
	public void testIdsOnlyFailingMpiClientFilters() throws Exception
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

		ResultSetTranslatorToTtpCreateRbfOnlyDropOtherColumnsImpl translator = new ResultSetTranslatorToTtpCreateRbfOnlyDropOtherColumnsImpl(
				"/ehr_status/subject/external_ref/id/value", recordBloomFilterGenerator, masterPatientIndexClient,
				ResultSetTranslatorToTtpCreateRbfOnlyDropOtherColumnsImpl.FILTER_ON_IDAT_NOT_FOUND_EXCEPTION);

		List<List<RowElement>> rows = IntStream.range(0, 15)
				.mapToObj(id -> Collections.<RowElement> singletonList(new StringRowElement(String.valueOf(id))))
				.collect(Collectors.toList());
		ResultSet resultSet = new ResultSet(null, null,
				"SELECT e/ehr_status/subject/external_ref/id/value as EHRID FROM EHR e",
				Collections.singleton(new Column("EHRID", "/ehr_status/subject/external_ref/id/value")), rows);

		ResultSet translate = translator.translate(resultSet);
		assertNotNull(translate);
		assertNotNull(translate.getRows());
		assertEquals(0, translate.getRows().size());
	}
}
