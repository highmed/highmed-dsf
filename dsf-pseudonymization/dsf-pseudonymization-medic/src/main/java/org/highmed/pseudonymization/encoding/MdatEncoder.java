package org.highmed.pseudonymization.encoding;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;

import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.structure.RowElement;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MdatEncoder
{

	private final SecretKey mdatKey;
	private final byte[] aadTag;
	private static final Logger logger = LoggerFactory.getLogger(MdatEncoder.class);

	/**
	 * @param mdatKey 32 byte key for AES Encryption
	 * @param aadTag  Study ID
	 */
	public MdatEncoder(SecretKey mdatKey, String aadTag)
	{
		this.mdatKey = Objects.requireNonNull(mdatKey, "mdatKey");
		String tag = Objects.requireNonNull(aadTag, "aadTag");
		this.aadTag = tag.getBytes(StandardCharsets.UTF_8);
	}

	public List<RowElement> encryptRow(List<RowElement> inputMDAT, int idColIdx)
	{
		List<RowElement> encryptedRow = new ArrayList<>();

		for (RowElement elem : inputMDAT)
		{
			if (!(inputMDAT.indexOf(elem) == idColIdx))
			{ // No point encrypting ehrID column, idatEncoder handles that
				if (elem == null)
				{
					encryptedRow.add(null);
				}
				else
				{
					encryptedRow.add(encrypt(elem));
				}
			}
		}

		return encryptedRow;
	}

	public List<RowElement> decryptRow(List<RowElement> encMDAT, int idColIdx)
	{
		List<RowElement> decryptedRow = new ArrayList<>();

		for (RowElement elem : encMDAT)
		{
			if (!(encMDAT.indexOf(elem) == idColIdx))
			{ // Encoded Pseudonym doesn't need to be decrypted
				if (elem == null)
				{
					decryptedRow.add(null);
				}
				else
				{
					decryptedRow.add(decrypt(elem));
				}
			}
			else
			{
				decryptedRow.add(elem);
			}
		}

		return decryptedRow;
	}

	/**
	 * AES-GCM encrypts a given Row out of a ResultSet's MDAT
	 *
	 * @param plainRow Plaintext Row of MDAT to be encrypted
	 * @return StringRowElement containing the encrypted MDAT String
	 */
	private StringRowElement encrypt(RowElement plainRow)
	{
		byte[] plain = plainRow.getValueAsString().getBytes(StandardCharsets.UTF_8);
		byte[] encrypted;

		try
		{
			encrypted = AesGcmUtil.encrypt(plain, this.aadTag, this.mdatKey);
			return new StringRowElement(Base64.getEncoder().encodeToString(encrypted));
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | ShortBufferException e)
		{
			logger.error("Error while encrypting Mdat: ", e);
			throw new InternalError(e);
		}
	}

	/**
	 * @param encrypted {@link StringRowElement} containing the encrypted MDAT
	 * @return Decrypted MDAT {@link StringRowElement}
	 */
	private StringRowElement decrypt(RowElement encrypted)
	{
		byte[] encryptedText = Base64.getDecoder().decode(encrypted.getValueAsString());
		byte[] decrypted;

		try
		{
			decrypted = AesGcmUtil.decrypt(encryptedText, this.aadTag, this.mdatKey);
			return new StringRowElement(new String(decrypted, StandardCharsets.UTF_8));
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e)
		{
			logger.error("Error while decrypting Mdat: ", e);
			throw new InternalError(e);
		}
	}
}