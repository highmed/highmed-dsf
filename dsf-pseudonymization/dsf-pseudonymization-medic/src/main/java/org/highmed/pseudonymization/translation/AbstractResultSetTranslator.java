package org.highmed.pseudonymization.translation;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;

import org.highmed.openehr.model.datatypes.DoubleRowElement;
import org.highmed.openehr.model.datatypes.IntegerRowElement;
import org.highmed.openehr.model.datatypes.JsonNodeRowElement;
import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.datatypes.ZonedDateTimeRowElement;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.Meta;
import org.highmed.openehr.model.structure.RowElement;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.highmed.pseudonymization.openehr.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractResultSetTranslator implements ResultSetTranslator
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractResultSetTranslator.class);

	protected final Meta copyMeta(Meta meta)
	{
		if (meta == null)
			return null;

		return new Meta(meta.getHref(), meta.getType(), meta.getSchemaVersion(), meta.getCreated(), meta.getGenerator(),
				meta.getExecutedAql());
	}

	protected final List<Column> copyColumns(List<Column> columns)
	{
		return columns.stream().map(copyColumn()).collect(Collectors.toList());
	}

	protected final Function<Column, Column> copyColumn()
	{
		return c -> new Column(c.getName(), c.getPath());
	}

	protected final RowElement copyRowElement(RowElement rowElement)
	{
		if (rowElement instanceof DoubleRowElement)
			return new DoubleRowElement(((DoubleRowElement) rowElement).getValue());
		else if (rowElement instanceof IntegerRowElement)
			return new IntegerRowElement(((IntegerRowElement) rowElement).getValue());
		else if (rowElement instanceof JsonNodeRowElement)
			return new JsonNodeRowElement(((JsonNodeRowElement) rowElement).getValue().deepCopy());
		else if (rowElement instanceof StringRowElement)
			return new StringRowElement(((StringRowElement) rowElement).getValue());
		else if (rowElement instanceof ZonedDateTimeRowElement)
			return new ZonedDateTimeRowElement(((ZonedDateTimeRowElement) rowElement).getValue());
		else
			throw new IllegalStateException("rowElement of type " + rowElement.getClass().getName() + " not supported");
	}

	protected final RowElement toEncryptedMdatRowElement(RowElement rowElement, SecretKey researchStudyKey,
			String researchStudyIdentifier)
	{
		if (researchStudyKey == null || researchStudyIdentifier == null)
			throw new IllegalStateException(
					"researchStudyKey or researchStudyIdentifier null, unable to encrypted MDAT row element");

		return new StringRowElement(toTypeTag(rowElement)
				+ encrypt(researchStudyKey, researchStudyIdentifier, rowElement.getValueAsString()));
	}

	private String toTypeTag(RowElement rowElement)
	{
		if (rowElement instanceof DoubleRowElement)
			return "double:";
		else if (rowElement instanceof IntegerRowElement)
			return "integer:";
		else if (rowElement instanceof JsonNodeRowElement)
			return "json:";
		else if (rowElement instanceof StringRowElement)
			return "string:";
		else if (rowElement instanceof ZonedDateTimeRowElement)
			return "timestamp:";
		else
			throw new IllegalStateException("rowElement of type " + rowElement.getClass().getName() + " not supported");
	}

	protected final String encrypt(SecretKey key, String aadTagValue, String plainText)
	{
		byte[] plain = plainText.getBytes(StandardCharsets.UTF_8);
		try
		{
			byte[] encrypted = AesGcmUtil.encrypt(plain, aadTagValue.getBytes(StandardCharsets.UTF_8), key);
			return Base64.getEncoder().encodeToString(encrypted);
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
				| ShortBufferException e)
		{
			logger.error("Error while encrypting with aadTag " + aadTagValue, e);
			throw new RuntimeException(e);
		}
	}

	protected final String decrypt(SecretKey key, String aadTagValue, String encryptedText)
	{
		byte[] encrypted = Base64.getDecoder().decode(encryptedText);
		try
		{
			byte[] decrypted = AesGcmUtil.decrypt(encrypted, aadTagValue.getBytes(StandardCharsets.UTF_8), key);
			return new String(decrypted, StandardCharsets.UTF_8);
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e)
		{
			logger.error("Error while decrypting with aadTag " + aadTagValue, e);
			throw new RuntimeException(e);
		}
	}

	protected final int getPsnColumnIndex(List<Column> columns)
	{
		for (int i = 0; i < columns.size(); i++)
			if (isPsnColumn().test(columns.get(i)))
				return i;

		return -1;
	}

	protected final Predicate<? super Column> isPsnColumn()
	{
		return column -> Constants.PSN_COLUMN_NAME.equals(column.getName())
				&& Constants.PSN_COLUMN_PATH.equals(column.getPath());
	}

	protected final RowElement toDecryptedMdatRowElement(RowElement rowElement, SecretKey researchStudyKey,
			String researchStudyIdentifier, ObjectMapper openEhrObjectMapper)
	{
		String tagAndEncrypted = rowElement.getValueAsString();

		if (tagAndEncrypted.startsWith("double:"))
			return DoubleRowElement
					.fromString(decrypt(researchStudyKey, researchStudyIdentifier, tagAndEncrypted.substring(7)));
		else if (tagAndEncrypted.startsWith("integer:"))
			return IntegerRowElement
					.fromString(decrypt(researchStudyKey, researchStudyIdentifier, tagAndEncrypted.substring(8)));
		else if (tagAndEncrypted.startsWith("json:"))
			return JsonNodeRowElement.fromString(
					decrypt(researchStudyKey, researchStudyIdentifier, tagAndEncrypted.substring(5)),
					openEhrObjectMapper);
		else if (tagAndEncrypted.startsWith("string:"))
			return StringRowElement
					.fromString(decrypt(researchStudyKey, researchStudyIdentifier, tagAndEncrypted.substring(7)));
		else if (tagAndEncrypted.startsWith("timestamp:"))
			return ZonedDateTimeRowElement
					.fromString(decrypt(researchStudyKey, researchStudyIdentifier, tagAndEncrypted.substring(10)));
		else
			throw new IllegalStateException("rowElement with unknown type tag");
	}
}
