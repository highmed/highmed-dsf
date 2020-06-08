package org.highmed.pseudonymization.psn;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;

import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.highmed.pseudonymization.domain.PseudonymizedPerson;
import org.highmed.pseudonymization.recordlinkage.MatchedPerson;
import org.highmed.pseudonymization.recordlinkage.MedicId;
import org.highmed.pseudonymization.recordlinkage.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PseudonymGeneratorImpl<P extends Person, PP extends PseudonymizedPerson>
		implements PseudonymGenerator<P, PP>
{
	private static final Logger logger = LoggerFactory.getLogger(PseudonymGeneratorImpl.class);

	private static final class Pseudonym
	{
		@JsonProperty
		final List<MedicId> medicIds = new ArrayList<>();

		@JsonCreator
		public Pseudonym(@JsonProperty("medicIds") Collection<? extends MedicId> medicIds)
		{
			if (medicIds != null)
				this.medicIds.addAll(medicIds);
		}
	}

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

	private final String researchStudyIdentifier;
	private final byte[] researchStudyIdentifierAadTag;

	private final SecretKey researchStudyTtpKey;
	private final ObjectMapper psnObjectMapper;

	private final PseudonymizedPersonFactory<P, PP> pseudonymizedPersonFactory;

	public PseudonymGeneratorImpl(String researchStudyIdentifier, SecretKey researchStudyTtpKey,
			ObjectMapper psnObjectMapper, PseudonymizedPersonFactory<P, PP> pseudonymizedPersonFactory)
	{
		this.researchStudyIdentifier = Objects.requireNonNull(researchStudyIdentifier, "researchStudyIdentifier");
		researchStudyIdentifierAadTag = researchStudyIdentifier.getBytes(StandardCharsets.UTF_8);

		this.researchStudyTtpKey = Objects.requireNonNull(researchStudyTtpKey, "researchStudyTtpKey");
		this.psnObjectMapper = Objects.requireNonNull(psnObjectMapper, "psnObjectMapper");

		this.pseudonymizedPersonFactory = Objects.requireNonNull(pseudonymizedPersonFactory,
				"pseudonymizedPersonFactory");
	}

	@Override
	public List<PP> createPseudonymsAndShuffle(Collection<? extends MatchedPerson<P>> persons)
	{
		Objects.requireNonNull(persons, "persons");
		if (persons.isEmpty())
			return Collections.emptyList();

		Map<MatchedPerson<P>, PseudonymWithJsonLength> pseudonyms = persons.parallelStream()
				.collect(Collectors.toMap(Function.identity(), toPseudonym(), (p1, p2) -> p1, LinkedHashMap::new));

		int maxLength = pseudonyms.values().parallelStream()
				.max(Comparator.comparingInt(PseudonymWithJsonLength::getJsonLength))
				.map(PseudonymWithJsonLength::getJsonLength).orElse(0);

		return pseudonyms.entrySet().parallelStream()
				.map(e -> pseudonymizedPersonFactory.create(e.getKey(), encrypt(e.getValue(), maxLength)))
				.collect(Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), list ->
				{
					Collections.shuffle(list);
					return list;
				}));
	}

	private Function<MatchedPerson<P>, PseudonymWithJsonLength> toPseudonym()
	{
		return matchedPerson ->
		{
			List<MedicId> medicIds = matchedPerson.getMatches().stream().map(Person::getMedicId)
					.collect(Collectors.toList());

			try
			{
				byte[] bytes = psnObjectMapper.writeValueAsBytes(new Pseudonym(medicIds));
				return new PseudonymWithJsonLength(bytes.length, medicIds);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		};
	}

	private String encrypt(PseudonymWithJsonLength pseudonym, int maxLength)
	{
		try
		{
			PseudonymWithPadding pwp = new PseudonymWithPadding(maxLength - pseudonym.jsonLength, pseudonym.medicIds);

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
	}
}
