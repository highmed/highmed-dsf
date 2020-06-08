package org.highmed.pseudonymization.psn;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.highmed.pseudonymization.domain.PseudonymizedPerson;
import org.highmed.pseudonymization.recordlinkage.MatchedPerson;
import org.highmed.pseudonymization.recordlinkage.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PseudonymDecoderImpl<P extends Person> implements PseudonymDecoder<P>
{
	private static final Logger logger = LoggerFactory.getLogger(PseudonymDecoderImpl.class);

	private final String researchStudyIdentifier;
	private final byte[] researchStudyIdentifierAadTag;

	private final SecretKey researchStudyTtpKey;
	private final ObjectMapper psnObjectMapper;

	private final MatchedPersonFactory<P> matchedPersonFactory;

	public PseudonymDecoderImpl(String researchStudyIdentifier, SecretKey researchStudyTtpKey,
			ObjectMapper psnObjectMapper, MatchedPersonFactory<P> matchedPersonFactory)
	{
		this.researchStudyIdentifier = Objects.requireNonNull(researchStudyIdentifier, "researchStudyIdentifier");
		researchStudyIdentifierAadTag = researchStudyIdentifier.getBytes(StandardCharsets.UTF_8);

		this.researchStudyTtpKey = Objects.requireNonNull(researchStudyTtpKey, "researchStudyTtpKey");
		this.psnObjectMapper = Objects.requireNonNull(psnObjectMapper, "psnObjectMapper");

		this.matchedPersonFactory = Objects.requireNonNull(matchedPersonFactory, "matchedPersonFactory");
	}

	@Override
	public MatchedPerson<P> decodePseudonym(PseudonymizedPerson person)
	{
		try
		{
			byte[] encryptedPwp = Base64.getDecoder().decode(person.getPseudonym());
			byte[] decryptPwp = AesGcmUtil.decrypt(encryptedPwp, researchStudyIdentifierAadTag, researchStudyTtpKey);
			PseudonymWithPadding pwp = psnObjectMapper.readValue(decryptPwp, PseudonymWithPadding.class);

			return matchedPersonFactory.create(person, pwp.medicIds);
		}
		catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | IOException e)
		{
			logger.error("Error while decrypting with aadTag " + researchStudyIdentifier, e);
			throw new RuntimeException(e);
		}
	}
}
