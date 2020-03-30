package org.highmed.pseudonymization.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;

import org.highmed.pseudonymization.base.Pseudonym;
import org.highmed.pseudonymization.base.TtpId;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PsnGenerator
{

	private SecretKey projectKey;
	private byte[] aadTag;
	private static final Logger logger = LoggerFactory.getLogger(PsnGenerator.class);
	private ObjectMapper mapper = new ObjectMapper();

	public PsnGenerator(SecretKey projectKey, String aadTag)
	{
		setProjectKey(projectKey);
		setAadTag(aadTag);
	}

	//Constructor for Testing
	public PsnGenerator() throws NoSuchAlgorithmException
	{
		this.projectKey = generateProjectKey();
		this.aadTag = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * Encodes one subject's linked IDs into one project specific pseudonym
	 *
	 * @param ttpIDs List of all of this subject's Local IDs
	 * @return Project pseudonym string
	 */
	public String encodePseudonym(List<TtpId> ttpIDs, int maxLength)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, ShortBufferException,
			JsonProcessingException
	{

		String idConcat = ttpIDs.stream().map(TtpId::toString).collect(Collectors.joining(","));

		Pseudonym psn = new Pseudonym(ttpIDs, maxLength - idConcat.length());
		String plainPsn = mapper.writeValueAsString(psn);
		System.out.println("Plain Psn: " + plainPsn);

		byte[] input = plainPsn.getBytes(StandardCharsets.UTF_8);
		byte[] encryptedPsn = AesGcmUtil.encrypt(input, this.aadTag, this.projectKey);
		return Base64.getEncoder().encodeToString(encryptedPsn);
	}

	/**
	 * Applies {@link PsnGenerator#encodePseudonym(List, int)} on a collection of linked subject IDs
	 */
	public List<String> encodeMultiplePsns(List<List<TtpId>> linkedIDs)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, ShortBufferException,
			JsonProcessingException
	{

		int maxLength = 0;
		for (List<TtpId> idList : linkedIDs)
		{
			String idConcat = idList.stream().map(TtpId::toString).collect(Collectors.joining(","));
			if (idConcat.length() > maxLength)
			{
				maxLength = idConcat.length();
			}
		}

		List<String> pseudonyms = new ArrayList<>();
		for (List<TtpId> idList : linkedIDs)
		{
			pseudonyms.add(encodePseudonym(idList, maxLength));
		}
		return pseudonyms;
	}

	/**
	 * Decodes a subject's ready-made, encrypted project pseudonym back into
	 * the initial localIDs it was made up from.
	 *
	 * @param input Base64 encoded, encrypted project pseudonym String
	 * @return List<TtpId> of the various local IDs.
	 */
	public List<TtpId> decodePseudonym(String input)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException
	{

		byte[] encoded = Base64.getDecoder().decode(input);
		byte[] decoded = AesGcmUtil.decrypt(encoded, this.aadTag, this.projectKey);

		String plainPsn = new String(decoded, StandardCharsets.UTF_8);
		Pseudonym psn = mapper.readValue(plainPsn, Pseudonym.class);

		return psn.getIds();

	}

	/**
	 * Applies {@link PsnGenerator#decodePseudonym(String)} on a collection of pseudonyms
	 */
	public List<List<TtpId>> decodeMultiplePsns(List<String> psns)
			throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
			IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException
	{

		List<List<TtpId>> linkedIDs = new ArrayList<>();
		for (String input : psns)
		{
			linkedIDs.add(decodePseudonym(input));
		}
		return linkedIDs;
	}

	// Init functions
	private void setProjectKey(SecretKey key)
	{
		int keySize = 32; // 32 byte key in AES256-G/CM
		if (key == null)
		{
			throw new IllegalArgumentException("The provided key must not be null.");
		}
		if (key.getEncoded().length == keySize)
		{
			this.projectKey = key;
		}
		else
		{
			throw new IllegalArgumentException(
					"The provided key does not meet " + "the 256 bit length standard for AES-GCM Keys.");
		}
	}

	private SecretKey generateProjectKey() throws NoSuchAlgorithmException
	{
		return AesGcmUtil.generateAES256Key();
	}

	private void setAadTag(String aadTag)
	{
		if (aadTag == null)
		{
			throw new IllegalArgumentException("Provided study ID must not be null.");
		}
		this.aadTag = aadTag.getBytes(StandardCharsets.UTF_8);
	}

}