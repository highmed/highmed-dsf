package org.highmed.pseudonymization.translation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.crypto.SecretKey;

import org.highmed.openehr.json.OpenEhrObjectMapperFactory;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultSetTranslatorResearchResultToTtpTest
{
	private static final Logger logger = LoggerFactory.getLogger(ResultSetTranslatorResearchResultToTtpTest.class);

	@Test
	public void testTranslateResearchResultForTtp() throws Exception
	{
		String researchStudyIdentifier = "researchStudy1";
		SecretKey researchStudyKey = AesGcmUtil.generateAES256Key();

		ResultSetTranslatorResearchResultToTtpImpl translator = new ResultSetTranslatorResearchResultToTtpImpl(
				researchStudyIdentifier, researchStudyKey);

		ObjectMapper openEhrObjectMapper = OpenEhrObjectMapperFactory.createObjectMapper();
		ResultSet resultSet = openEhrObjectMapper
				.readValue(Files.readAllBytes(Paths.get("src/test/resources/result_5_PSN.json")), ResultSet.class);
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
