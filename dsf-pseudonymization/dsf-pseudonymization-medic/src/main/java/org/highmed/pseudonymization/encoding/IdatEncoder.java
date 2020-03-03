package org.highmed.pseudonymization.encoding;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

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
	private String studyID;
	private byte[] seed;
	private String origin;
	private RBFGenerator rbfGen;

	/**
	 * @param projectKey 32 byte key for AES Encryption
	 * @param studyID     Study ID
	 * @param seed       Seed byte array for RBF Permutation
	 * @param origin     Institution where the encoded IDAT hail from
	 */
	public IdatEncoder(SecretKey projectKey, String studyID, byte[] seed, String origin)
	{
		setProjectKey(projectKey);
		setStudyID(studyID);
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
			byte[] encrypted = AesGcmUtil.encrypt(plain, this.studyID.getBytes(StandardCharsets.UTF_8), this.projectKey);
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
			byte[] decrypted = AesGcmUtil.decrypt(encryptedText, this.studyID.getBytes(StandardCharsets.UTF_8), this.projectKey);
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

	private void setStudyID(String studyID)
	{
		if (studyID == null || studyID.equals(""))
		{
			throw new IllegalArgumentException("Provided study ID must not be null or empty.");
		}
		this.studyID = studyID;
	}

	private void setSeed(byte[] seed)
	{
		if (seed.length == 0)
		{
			throw new IllegalArgumentException("Provided permutation seed must not be empty.");
		}
		this.seed = seed;
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
