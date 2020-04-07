package org.highmed.pseudonymization.psn;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;

import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PseudonymEncoderImpl implements PseudonymEncoder
{
	private static final Logger logger = LoggerFactory.getLogger(PseudonymEncoderImpl.class);

	private static final class PseudonymWithJsonLength
	{
		final int jsonLength;
		final List<MedicId> medicIds = new ArrayList<>();

		PseudonymWithJsonLength(int jsonLength, Collection<? extends MedicId> medicIds)
		{
			this.jsonLength = jsonLength;
			if (medicIds != null)
				this.medicIds.addAll(medicIds);
		}

		int getJsonLength()
		{
			return jsonLength;
		}
	}

	private static final class PseudonymWithPadding
	{
		@JsonProperty
		final List<MedicId> medicIds = new ArrayList<>();

		@JsonProperty
		final String padding;

		PseudonymWithPadding(int paddingLength, Collection<? extends MedicId> medicIds)
		{
			this(IntStream.range(0, paddingLength).mapToObj(i -> " ").collect(Collectors.joining()), medicIds);
		}

		PseudonymWithPadding(@JsonProperty("padding") String padding,
				@JsonProperty("medicIds") Collection<? extends MedicId> medicIds)
		{
			this.padding = padding;
			if (medicIds != null)
				this.medicIds.addAll(medicIds);
		}
	}

	private final String researchStudyIdentifier;
	private final byte[] researchStudyIdentifierAadTag;

	private final SecretKey researchStudyTtpKey;
	private final ObjectMapper psnObjectMapper;

	public PseudonymEncoderImpl(String researchStudyIdentifier, SecretKey researchStudyTtpKey,
			ObjectMapper psnObjectMapper)
	{
		this.researchStudyIdentifier = Objects.requireNonNull(researchStudyIdentifier, "researchStudyIdentifier");
		researchStudyIdentifierAadTag = researchStudyIdentifier.getBytes(StandardCharsets.UTF_8);

		this.researchStudyTtpKey = Objects.requireNonNull(researchStudyTtpKey, "researchStudyTtpKey");
		this.psnObjectMapper = Objects.requireNonNull(psnObjectMapper, "psnObjectMapper");
	}

	@Override
	public List<String> encodePseudonyms(List<Pseudonym> pseudonyms)
	{
		Objects.requireNonNull(pseudonyms, "pseudonyms");
		if (pseudonyms.isEmpty())
			return Collections.emptyList();

		List<PseudonymWithJsonLength> pseudonymsWithJsonLength = pseudonyms.parallelStream()
				.map(toPseudonymWithJsonLength()).collect(Collectors.toList());

		int maxLength = pseudonymsWithJsonLength.parallelStream()
				.max(Comparator.comparingInt(PseudonymWithJsonLength::getJsonLength))
				.map(PseudonymWithJsonLength::getJsonLength).orElse(0);

		return pseudonymsWithJsonLength.parallelStream().map(encrypt(maxLength)).collect(Collectors.toList());
	}

	private Function<Pseudonym, PseudonymWithJsonLength> toPseudonymWithJsonLength()
	{
		return pseudonym ->
		{
			try
			{
				byte[] bytes = psnObjectMapper.writeValueAsBytes(pseudonym);
				return new PseudonymWithJsonLength(bytes.length, pseudonym.getMedicIds());
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		};
	}

	private Function<PseudonymWithJsonLength, String> encrypt(int maxLength)
	{
		return pseudonym ->
		{
			try
			{
				PseudonymWithPadding pwp = new PseudonymWithPadding(maxLength - pseudonym.jsonLength,
						pseudonym.medicIds);

				byte[] plainPwp = psnObjectMapper.writeValueAsBytes(pwp);
				byte[] encrypted = AesGcmUtil.encrypt(plainPwp, researchStudyIdentifierAadTag, researchStudyTtpKey);
				return Base64.getEncoder().encodeToString(encrypted);
			}
			catch (JsonProcessingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
					| ShortBufferException e)
			{
				logger.error("Error while encrypting with aadTag " + researchStudyIdentifier, e);
				throw new RuntimeException(e);
			}
		};
	}

	@Override
	public List<Pseudonym> decodePseudonyms(List<String> pseudonyms)
	{
		return pseudonyms.parallelStream().map(decrypt()).collect(Collectors.toList());
	}

	private Function<String, Pseudonym> decrypt()
	{
		return encryptedPwpBase64 ->
		{
			try
			{
				byte[] encryptedPwp = Base64.getDecoder().decode(encryptedPwpBase64);
				byte[] decryptPwp = AesGcmUtil.decrypt(encryptedPwp, researchStudyIdentifierAadTag,
						researchStudyTtpKey);
				PseudonymWithPadding pwp = psnObjectMapper.readValue(decryptPwp, PseudonymWithPadding.class);

				return new Pseudonym(pwp.medicIds);
			}
			catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
					| IOException e)
			{
				logger.error("Error while decrypting with aadTag " + researchStudyIdentifier, e);
				throw new RuntimeException(e);
			}
		};
	}
}
