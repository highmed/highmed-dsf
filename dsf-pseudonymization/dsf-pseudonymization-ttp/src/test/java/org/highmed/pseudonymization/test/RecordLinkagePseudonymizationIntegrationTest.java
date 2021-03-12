package org.highmed.pseudonymization.test;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.highmed.openehr.json.OpenEhrObjectMapperFactory;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.highmed.pseudonymization.domain.PersonWithMdat;
import org.highmed.pseudonymization.domain.PseudonymizedPersonWithMdat;
import org.highmed.pseudonymization.domain.impl.MatchedPersonImpl;
import org.highmed.pseudonymization.domain.impl.PseudonymizedPersonImpl;
import org.highmed.pseudonymization.psn.PseudonymGenerator;
import org.highmed.pseudonymization.psn.PseudonymGeneratorImpl;
import org.highmed.pseudonymization.recordlinkage.FederatedMatcher;
import org.highmed.pseudonymization.recordlinkage.FederatedMatcherImpl;
import org.highmed.pseudonymization.recordlinkage.MatchedPerson;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedic;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedicWithRbfImpl;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToMedic;
import org.highmed.pseudonymization.translation.ResultSetTranslatorToMedicImpl;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RecordLinkagePseudonymizationIntegrationTest
{
	private static final Logger logger = LoggerFactory.getLogger(RecordLinkagePseudonymizationIntegrationTest.class);

	private ResultSetTranslatorFromMedic fromMedic;
	private FederatedMatcher<PersonWithMdat> matcher;
	private PseudonymGenerator<PersonWithMdat, PseudonymizedPersonWithMdat> generator;
	private ResultSetTranslatorToMedic toMedic;
	private ObjectMapper openEhrObjectMapper;

	@Before
	public void before() throws Exception
	{
		fromMedic = new ResultSetTranslatorFromMedicWithRbfImpl();
		matcher = new FederatedMatcherImpl<>(MatchedPersonImpl::new);
		generator = new PseudonymGeneratorImpl<>("researchStudyIdentifier", AesGcmUtil.generateAES256Key(),
				new ObjectMapper(), PseudonymizedPersonImpl::new);
		toMedic = new ResultSetTranslatorToMedicImpl();
		openEhrObjectMapper = OpenEhrObjectMapperFactory.createObjectMapper();
	}

	@Test
	public void testMatch() throws Exception
	{
		ResultSet resultSet1 = readResultSet();
		ResultSet resultSet2 = readResultSet();

		List<PersonWithMdat> fromOrg1 = fromMedic.translate("org1", resultSet1);
		List<PersonWithMdat> fromOrg2 = fromMedic.translate("org2", resultSet2);
		List<List<PersonWithMdat>> personLists = Arrays.asList(fromOrg1, fromOrg2);

		Set<MatchedPerson<PersonWithMdat>> matchedPersons = matcher.matchPersons(personLists);
		assertNotNull(matchedPersons);

		List<PseudonymizedPersonWithMdat> pseudonymizedPersons = generator.createPseudonymsAndShuffle(matchedPersons);
		assertNotNull(pseudonymizedPersons);

		ResultSet pseudonymizedResultSet = toMedic.translate(resultSet1.getMeta(), resultSet1.getColumns(),
				pseudonymizedPersons);
		assertNotNull(pseudonymizedResultSet);

		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

		logger.debug("Pseudonymized ResultSet for Researcher {}",
				openEhrObjectMapper.writer(prettyPrinter).writeValueAsString(pseudonymizedResultSet));
	}

	private ResultSet readResultSet() throws IOException
	{
		try (InputStream in = Files.newInputStream(Paths.get("src/test/resources/rbf_resultset.json")))
		{
			return openEhrObjectMapper.readValue(in, ResultSet.class);
		}
	}
}
