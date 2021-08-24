package org.highmed.pseudonymization.translation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.highmed.openehr.json.OpenEhrObjectMapperFactory;
import org.highmed.openehr.model.structure.ResultSet;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultSetTranslatorResearchResultFromTtpTest
{
	private static final Logger logger = LoggerFactory.getLogger(ResultSetTranslatorResearchResultFromTtpTest.class);

	@Test
	public void testTranslateResearchResultFromTtp() throws Exception
	{
		String organizationIdentifier = "org1";
		byte[] decodedOrganizationKey = Base64.getDecoder().decode("KollbDbopQK1t6hZncLicT7X64H1TfrzjpYdF4jehHo=");
		SecretKey organizationKey = new SecretKeySpec(decodedOrganizationKey, 0, decodedOrganizationKey.length, "AES");

		String researchStudyIdentifier = "researchStudy1";
		byte[] decodedKey = Base64.getDecoder().decode("Loy75q55b/L3yxk3BoRgNUSAJJLan643alkrWHathBk=");
		SecretKey researchStudyKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

		ObjectMapper openEhrObjectMapper = OpenEhrObjectMapperFactory.createObjectMapper();

		ResultSetTranslatorResearchResultFromTtpImpl translator = new ResultSetTranslatorResearchResultFromTtpImpl(
				organizationKey, organizationIdentifier, researchStudyIdentifier, researchStudyKey,
				openEhrObjectMapper);

		ResultSet resultSet = openEhrObjectMapper.readValue(
				Files.readAllBytes(Paths.get("src/test/resources/researchresult_from_ttp.json")), ResultSet.class);
		assertNotNull(resultSet);
		assertNotNull(resultSet.getColumns());
		assertEquals(3, resultSet.getColumns().size());
		assertNotNull(resultSet.getRows());
		assertEquals(1, resultSet.getRows().size());
		assertNotNull(resultSet.getRow(0));
		assertEquals(3, resultSet.getRow(0).size());

		ResultSet translatedResultSet = translator.translate(resultSet);
		assertNotNull(translatedResultSet);
		assertNotNull(translatedResultSet.getColumns());
		assertEquals(3, translatedResultSet.getColumns().size());
		assertNotNull(translatedResultSet.getRows());
		assertEquals(1, translatedResultSet.getRows().size());
		assertNotNull(translatedResultSet.getRow(0));
		assertEquals(3, translatedResultSet.getRow(0).size());

		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

		logger.debug("Encoded ResultSet {}",
				openEhrObjectMapper.writer(prettyPrinter).writeValueAsString(translatedResultSet));

	}
}
