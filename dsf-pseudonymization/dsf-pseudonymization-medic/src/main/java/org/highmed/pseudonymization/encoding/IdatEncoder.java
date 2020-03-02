package org.highmed.pseudonymization.encoding;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;

import org.highmed.pseudonymization.base.Idat;
import org.highmed.pseudonymization.base.IdatEncoded;
import org.highmed.pseudonymization.base.TtpId;
import org.highmed.pseudonymization.bloomfilter.RBFGenerator;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilter;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for converting plaintext {@link Idat} to
 * {@link IdatEncoded} by encrypting the local pseudonym
 * as well as MDAT and bloom-filter hashing all other IDAT fields
 */
public class IdatEncoder
{

	private static final Logger logger = LoggerFactory.getLogger(IdatEncoder.class);
	private SecretKey projectKey;
	private byte[] aadTag;
	private byte[] seed;
	private String origin;
	private RBFGenerator rbfGen;

	/**
	 * @param projectKey 32 byte key for AES Encryption
	 * @param aadTag     Study ID
	 * @param seed       Seed for RBF Permutation
	 * @param origin     Institution where the encoded IDAT hail from
	 */
	public IdatEncoder(SecretKey projectKey, String aadTag, String seed, String origin)
	{
		setProjectKey(projectKey);
		setAadTag(aadTag);
		setSeed(seed);
		setOrigin(origin);
		this.rbfGen = new RBFGenerator();
	}

	/**
	 * @param ct {@link Idat} to be encoded for transport in Data Sharing Framework
	 * @return {@link IdatEncoded} Encoded container containing an encrypted local Psn
	 * and bloom filter hashes of IDAT
	 */
	public IdatEncoded encodeContainer(Idat ct)
	{

		TtpId tid = encrypt(ct.getLocalPsn());
		RecordBloomFilter rbf = rbfGen.generateRbfFromIdat(ct, seed);
		IdatEncoded enc = new IdatEncoded(tid, rbf.getBitSet());
		return enc;
	}

	/**
	 * @param cte Fully encoded {@link IdatEncoded} whose local ID shall be decrypted
	 * @return An String containing the subject's decrypted local MPI ID.
	 */
	public String decodeContainer(IdatEncoded cte)
	{
		String localID = decrypt(cte.getEncodedID());
		return localID;
	}

	/**
	 * AES-GCM-encrypts a given String, e.g. a local Identifier of an IdatContainer
	 * or an MDAT field
	 *
	 * @param plainText Plaintext String to be encrypted
	 * @return TtpId containing the origin and encrypted local PSN
	 */
	private TtpId encrypt(String plainText)
	{
		byte[] plain = plainText.getBytes(StandardCharsets.UTF_8);
		try
		{
			byte[] encrypted = AesGcmUtil.encrypt(plain, this.aadTag, this.projectKey);
			return new TtpId(this.origin, Base64.getEncoder().encodeToString(encrypted));
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | ShortBufferException e)
		{
			logger.error("Error while encrypting TtpId", e);
			throw new InternalError(e);
		}
	}

	/**
	 * @param encrypted TtpId containing the encrypted local ID
	 * @return Decrypted Local ID String
	 */
	private String decrypt(TtpId encrypted)
	{
		byte[] encryptedText = Base64.getDecoder().decode(encrypted.getIdString());
		try
		{
			byte[] decrypted = AesGcmUtil.decrypt(encryptedText, this.aadTag, this.projectKey);
			return new String(decrypted, StandardCharsets.UTF_8);
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e)
		{
			logger.error("Error while decrypting TtpId", e);
			throw new InternalError(e);
		}
	}

	// Init methods
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

	private void setAadTag(String aadTag)
	{
		if (aadTag == null)
		{
			throw new IllegalArgumentException("Provided study ID must not be null.");
		}
		this.aadTag = aadTag.getBytes(StandardCharsets.UTF_8);
	}

	private void setSeed(String seed)
	{
		if (seed == null)
		{
			throw new IllegalArgumentException("Provided permutation seed must not be null.");
		}
		this.seed = seed.getBytes(StandardCharsets.UTF_8);
	}

	private void setOrigin(String origin)
	{
		if (origin == null || origin.length() == 0)
		{
			throw new IllegalArgumentException("Provided origin may neither be empty nor null.");
		}
		this.origin = origin;
	}
}
