package org.highmed.pseudonymization.translation;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import org.highmed.pseudonymization.mpi.Idat;
import org.highmed.pseudonymization.mpi.MasterPatientIndexClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultSetTranslatorToTtpTestRbfOnly
{
	private static final Logger logger = LoggerFactory.getLogger(ResultSetTranslatorToTtpTestRbfOnly.class);

	@Test
	public void testIdsOnly() throws Exception
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

		ResultSetTranslatorToTtpRbfOnlyImpl translator = new ResultSetTranslatorToTtpRbfOnlyImpl(recordBloomFilterGenerator,
				masterPatientIndexClient);

		ObjectMapper openEhrObjectMapper = OpenEhrObjectMapperFactory.createObjectMapper();
		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

		List<List<RowElement>> rows = IntStream.range(0, 15)
				.mapToObj(id -> Collections.<RowElement> singletonList(new StringRowElement(String.valueOf(id))))
				.collect(Collectors.toList());
		ResultSet resultSet = new ResultSet(null, null, "SELECT e/ehr_id/value as EHRID FROM EHR e",
				Collections.singleton(new Column("EHRID", "/ehr_id/value")), rows);

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
}
