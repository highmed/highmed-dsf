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

		PseudonymGenerator<TestPerson> generator = new PseudonymGeneratorImpl<TestPerson>(researchStudyIdentifier,
				researchStudyTtpKey, psnObjectMapper, (person, pseudonym) -> new TestPseudonymizedPerson(pseudonym));

		List<MatchedPerson<TestPerson>> pseudonyms = Arrays.asList(
				new TestMatchedPerson(new TestPerson(new TestMedicId("org1", "value11"), null),
						new TestPerson(new TestMedicId("org2", "value21"), null)),
				new TestMatchedPerson(new TestPerson(new TestMedicId("org1", "value12"), null)));

		List<PseudonymizedPerson> encodedPseudonyms = generator.createPseudonyms(pseudonyms);

		encodedPseudonyms.forEach(s -> logger.debug("Encoded Pseudonym: {}", s));

		PseudonymDecoder<TestPerson> decoder = new PseudonymDecoderImpl<TestPerson>(researchStudyIdentifier,
				researchStudyTtpKey, psnObjectMapper, (person, medicIds) -> new TestMatchedPerson(
						medicIds.stream().map(i -> new TestPerson(i, null)).toArray(TestPerson[]::new)));

		List<MatchedPerson<TestPerson>> decodePseudonyms = decoder.decodePseudonyms(encodedPseudonyms);

		assertNotNull(decodePseudonyms);
		assertEquals(2, decodePseudonyms.size());

		assertNotNull(decodePseudonyms.get(0));
		assertNotNull(decodePseudonyms.get(0).getMatches());
		assertEquals(pseudonyms.get(0).getMatches().size(), decodePseudonyms.get(0).getMatches().size());
		assertNotNull(decodePseudonyms.get(0).getMatches().get(0));
		assertNotNull(decodePseudonyms.get(0).getMatches().get(0).getMedicId());
		assertEquals(pseudonyms.get(0).getMatches().get(0).getMedicId().getOrganization(),
				decodePseudonyms.get(0).getMatches().get(0).getMedicId().getOrganization());
		assertEquals(pseudonyms.get(0).getMatches().get(0).getMedicId().getValue(),
				decodePseudonyms.get(0).getMatches().get(0).getMedicId().getValue());

		assertNotNull(decodePseudonyms.get(0).getMatches().get(1));
		assertNotNull(decodePseudonyms.get(0).getMatches().get(1).getMedicId());
		assertEquals(pseudonyms.get(0).getMatches().get(1).getMedicId().getOrganization(),
				decodePseudonyms.get(0).getMatches().get(1).getMedicId().getOrganization());
		assertEquals(pseudonyms.get(0).getMatches().get(1).getMedicId().getValue(),
				decodePseudonyms.get(0).getMatches().get(1).getMedicId().getValue());

		assertNotNull(decodePseudonyms.get(1));
		assertNotNull(decodePseudonyms.get(1).getMatches());
		assertNotNull(decodePseudonyms.get(1).getMatches().get(0).getMedicId());
		assertEquals(pseudonyms.get(1).getMatches().size(), decodePseudonyms.get(1).getMatches().size());
		assertNotNull(decodePseudonyms.get(1).getMatches().get(0));
		assertEquals(pseudonyms.get(1).getMatches().get(0).getMedicId().getOrganization(),
				decodePseudonyms.get(1).getMatches().get(0).getMedicId().getOrganization());
		assertEquals(pseudonyms.get(1).getMatches().get(0).getMedicId().getValue(),
				decodePseudonyms.get(1).getMatches().get(0).getMedicId().getValue());
	}
}
