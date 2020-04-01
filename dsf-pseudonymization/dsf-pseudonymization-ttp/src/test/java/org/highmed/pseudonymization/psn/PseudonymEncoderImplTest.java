package org.highmed.pseudonymization.psn;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PseudonymEncoderImplTest
{
	private static final Logger logger = LoggerFactory.getLogger(PseudonymEncoderImplTest.class);

	@Test
	public void testEncodeDecode() throws Exception
	{
		PseudonymEncoder encoder = new PseudonymEncoderImpl("researchStudyIdentifier", AesGcmUtil.generateAES256Key(),
				new ObjectMapper());

		List<Pseudonym> pseudonyms = Arrays.asList(
				new Pseudonym(Arrays.asList(new MedicId("org1", "value11"), new MedicId("org2", "value21"))),
				new Pseudonym(Arrays.asList(new MedicId("org1", "value12"))));
		List<String> encodedPseudonyms = encoder.encodePseudonyms(pseudonyms);

		encodedPseudonyms.forEach(s -> logger.debug("Encoded Pseudonym: {}", s));

		List<Pseudonym> decodePseudonyms = encoder.decodePseudonyms(encodedPseudonyms);
		assertNotNull(decodePseudonyms);
		assertEquals(2, decodePseudonyms.size());

		assertNotNull(decodePseudonyms.get(0));
		assertNotNull(decodePseudonyms.get(0).getMedicIds());
		assertEquals(pseudonyms.get(0).getMedicIds().size(), decodePseudonyms.get(0).getMedicIds().size());
		assertNotNull(decodePseudonyms.get(0).getMedicIds().get(0));
		assertEquals(pseudonyms.get(0).getMedicIds().get(0).getOrganization(),
				decodePseudonyms.get(0).getMedicIds().get(0).getOrganization());
		assertEquals(pseudonyms.get(0).getMedicIds().get(0).getValue(),
				decodePseudonyms.get(0).getMedicIds().get(0).getValue());
		assertNotNull(decodePseudonyms.get(0).getMedicIds().get(1));
		assertEquals(pseudonyms.get(0).getMedicIds().get(1).getOrganization(),
				decodePseudonyms.get(0).getMedicIds().get(1).getOrganization());
		assertEquals(pseudonyms.get(0).getMedicIds().get(1).getValue(),
				decodePseudonyms.get(0).getMedicIds().get(1).getValue());

		assertNotNull(decodePseudonyms.get(1));
		assertNotNull(decodePseudonyms.get(1).getMedicIds());
		assertEquals(pseudonyms.get(1).getMedicIds().size(), decodePseudonyms.get(1).getMedicIds().size());
		assertNotNull(decodePseudonyms.get(1).getMedicIds().get(0));
		assertEquals(pseudonyms.get(1).getMedicIds().get(0).getOrganization(),
				decodePseudonyms.get(1).getMedicIds().get(0).getOrganization());
		assertEquals(pseudonyms.get(1).getMedicIds().get(0).getValue(),
				decodePseudonyms.get(1).getMedicIds().get(0).getValue());
	}
}
