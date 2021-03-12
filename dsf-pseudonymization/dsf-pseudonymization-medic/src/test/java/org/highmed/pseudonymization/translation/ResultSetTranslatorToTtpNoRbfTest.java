package org.highmed.pseudonymization.translation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;

import javax.crypto.SecretKey;

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
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultSetTranslatorToTtpNoRbfTest
{
	private static final Logger logger = LoggerFactory.getLogger(ResultSetTranslatorToTtpNoRbfTest.class);

	@Test
	public void testTranslateForTtp() throws Exception
	{
		String organizationIdentifier = "org1";
		SecretKey organizationKey = AesGcmUtil.generateAES256Key();
		String researchStudyIdentifier = "researchStudy1";
		SecretKey researchStudyKey = AesGcmUtil.generateAES256Key();

		ResultSetTranslatorToTtpNoRbfImpl translator = new ResultSetTranslatorToTtpNoRbfImpl(organizationIdentifier,
				organizationKey, researchStudyIdentifier, researchStudyKey,
				"/ehr_status/subject/external_ref/id/value");

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
		assertEquals(4, translatedResultSet.getColumns().size());
		assertNotNull(translatedResultSet.getRows());
		assertEquals(1, translatedResultSet.getRows().size());
		assertNotNull(translatedResultSet.getRow(0));
		assertEquals(4, translatedResultSet.getRow(0).size());

		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

		logger.debug("Encoded ResultSet {}",
				openEhrObjectMapper.writer(prettyPrinter).writeValueAsString(translatedResultSet));
	}
}
