package org.highmed.pseudonymization.psn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKey;

import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.highmed.pseudonymization.domain.PseudonymizedPerson;
import org.highmed.pseudonymization.recordlinkage.MatchedPerson;
import org.highmed.pseudonymization.recordlinkage.TestMatchedPerson;
import org.highmed.pseudonymization.recordlinkage.TestMedicId;
import org.highmed.pseudonymization.recordlinkage.TestPerson;
import org.highmed.pseudonymization.recordlinkage.TestPseudonymizedPerson;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;

public class PseudonymGeneratorImplTest
{
	private static final Logger logger = LoggerFactory.getLogger(PseudonymGeneratorImplTest.class);

	@Test
	public void testEncodeDecode() throws Exception
	{
		String researchStudyIdentifier = "researchStudyIdentifier";
		SecretKey researchStudyTtpKey = AesGcmUtil.generateAES256Key();
		ObjectMapper psnObjectMapper = new ObjectMapper();
		psnObjectMapper.registerSubtypes(new NamedType(TestMedicId.class, "TestMedicId"));

		PseudonymGenerator<TestPerson, PseudonymizedPerson> generator = new PseudonymGeneratorImpl<>(
				researchStudyIdentifier, researchStudyTtpKey, psnObjectMapper,
				(person, pseudonym) -> new TestPseudonymizedPerson(pseudonym));

		List<MatchedPerson<TestPerson>> ids = Arrays.asList(
				new TestMatchedPerson(new TestPerson(new TestMedicId("org1", "value11"), null),
						new TestPerson(new TestMedicId("org2", "value21"), null)),
				new TestMatchedPerson(new TestPerson(new TestMedicId("org1", "value12"), null)));

		List<PseudonymizedPerson> encodedPseudonyms = generator.createPseudonymsAndShuffle(ids);

		encodedPseudonyms.forEach(s -> logger.debug("Encoded Pseudonym: {}", s));

		PseudonymDecoder<TestPerson> decoder = new PseudonymDecoderImpl<TestPerson>(researchStudyIdentifier,
				researchStudyTtpKey, psnObjectMapper, (person, medicIds) -> new TestMatchedPerson(
						medicIds.stream().map(i -> new TestPerson(i, null)).toArray(TestPerson[]::new)));

		List<MatchedPerson<TestPerson>> pseudonyms = decoder.decodePseudonyms(encodedPseudonyms);

		assertNotNull(pseudonyms);
		assertEquals(2, pseudonyms.size());

		int doubleMatch = pseudonyms.get(0).getMatches().size() == 2 ? 0 : 1;
		int singleMatch = pseudonyms.get(0).getMatches().size() == 2 ? 1 : 0;

		assertNotNull(pseudonyms.get(doubleMatch));
		assertNotNull(pseudonyms.get(doubleMatch).getMatches());
		assertEquals(ids.get(0).getMatches().size(), pseudonyms.get(doubleMatch).getMatches().size());
		assertNotNull(pseudonyms.get(doubleMatch).getMatches().get(0));
		assertNotNull(pseudonyms.get(doubleMatch).getMatches().get(0).getMedicId());
		assertEquals(ids.get(0).getMatches().get(0).getMedicId().getOrganization(),
				pseudonyms.get(doubleMatch).getMatches().get(0).getMedicId().getOrganization());
		assertEquals(ids.get(0).getMatches().get(0).getMedicId().getValue(),
				pseudonyms.get(doubleMatch).getMatches().get(0).getMedicId().getValue());

		assertNotNull(pseudonyms.get(doubleMatch).getMatches().get(1));
		assertNotNull(pseudonyms.get(doubleMatch).getMatches().get(1).getMedicId());
		assertEquals(ids.get(0).getMatches().get(1).getMedicId().getOrganization(),
				pseudonyms.get(doubleMatch).getMatches().get(1).getMedicId().getOrganization());
		assertEquals(ids.get(0).getMatches().get(1).getMedicId().getValue(),
				pseudonyms.get(doubleMatch).getMatches().get(1).getMedicId().getValue());

		assertNotNull(pseudonyms.get(singleMatch));
		assertNotNull(pseudonyms.get(singleMatch).getMatches());
		assertNotNull(pseudonyms.get(singleMatch).getMatches().get(0).getMedicId());
		assertEquals(ids.get(1).getMatches().size(), pseudonyms.get(singleMatch).getMatches().size());
		assertNotNull(pseudonyms.get(singleMatch).getMatches().get(0));
		assertEquals(ids.get(1).getMatches().get(0).getMedicId().getOrganization(),
				pseudonyms.get(singleMatch).getMatches().get(0).getMedicId().getOrganization());
		assertEquals(ids.get(1).getMatches().get(0).getMedicId().getValue(),
				pseudonyms.get(singleMatch).getMatches().get(0).getMedicId().getValue());
	}
}
