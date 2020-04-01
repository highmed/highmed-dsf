package org.highmed.pseudonymization.encoding;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.openehr.model.structure.RowElement;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilter;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGenerator;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.highmed.pseudonymization.mpi.Idat;
import org.highmed.pseudonymization.mpi.MasterPatientIndexClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultSetEncoderImpl implements ResultSetEncoder
{
	private static final Logger logger = LoggerFactory.getLogger(ResultSetEncoderImpl.class);

	private final RecordBloomFilterGenerator recordBloomFilterGenerator;
	private final MasterPatientIndexClient masterPatientIndexClient;

	private final String organizationIdentifier;
	private final SecretKey organizationKey;
	private final String researchStudyIdentifier;
	private final SecretKey researchStudyKey;

	private final ObjectMapper openEhrObjectMapper;

	public ResultSetEncoderImpl(RecordBloomFilterGenerator recordBloomFilterGenerator,
			MasterPatientIndexClient masterPatientIndexClient, String organizationIdentifier, SecretKey organizationKey,
			String researchStudyIdentifier, SecretKey researchStudyKey, ObjectMapper openEhrObjectMapper)
	{
		this.organizationIdentifier = Objects.requireNonNull(organizationIdentifier, "organizationIdentifier");
		this.recordBloomFilterGenerator = Objects.requireNonNull(recordBloomFilterGenerator,
				"recordBloomFilterGenerator");
		this.masterPatientIndexClient = Objects.requireNonNull(masterPatientIndexClient, "masterPatientIndexClient");
		this.organizationKey = Objects.requireNonNull(organizationKey, "organizationKey");
		this.researchStudyIdentifier = Objects.requireNonNull(researchStudyIdentifier, "researchStudyIdentifier");
		this.researchStudyKey = Objects.requireNonNull(researchStudyKey, "researchStudyKey");
		this.openEhrObjectMapper = Objects.requireNonNull(openEhrObjectMapper, "openEhrObjectMapper");
	}

	@Override
	public ResultSet encode(ResultSet resultSet)
	{
		int ehrIdColumnIndex = getEhrColumnIndex(resultSet.getColumns());
		int psnColumnIndex = getPsnColumnIndex(resultSet.getColumns());

		Meta newMeta = copyMeta(resultSet.getMeta());
		List<Column> newColumns;
		List<List<RowElement>> newRows;

		if (ehrIdColumnIndex >= 0 && psnColumnIndex < 0)
		{
			newColumns = encodeColumnsWithEhrId(resultSet.getColumns());
			newRows = encodeRowsWithEhrId(ehrIdColumnIndex, resultSet.getRows());
		}
		else if (psnColumnIndex >= 0 && ehrIdColumnIndex < 0)
		{
			newColumns = encodeColumnsWithPsn(resultSet.getColumns());
			newRows = encodeRowsWithPsn(psnColumnIndex, resultSet.getRows());
		}
		else
			throw new IllegalStateException("ResultSet contains EHRID and PSN columns or neither");

		return new ResultSet(newMeta, resultSet.getName(), resultSet.getQuery(), newColumns, newRows);
	}

	private Meta copyMeta(Meta meta)
	{
		return new Meta(meta.getHref(), meta.getType(), meta.getSchemaVersion(), meta.getCreated(), meta.getGenerator(),
				meta.getExecutedAql());
	}

	private List<Column> encodeColumnsWithEhrId(List<Column> columns)
	{
		Stream<Column> s1 = columns.stream().filter(isEhrIdColumn().negate()).map(toNewColumn());
		Stream<Column> s2 = newMedicIdAndRbfColumn();
		return Stream.concat(s1, s2).collect(Collectors.toList());
	}

	private List<Column> encodeColumnsWithPsn(List<Column> columns)
	{
		return columns.stream().map(toNewColumn()).collect(Collectors.toList());
	}

	private int getEhrColumnIndex(List<Column> columns)
	{
		for (int i = 0; i < columns.size(); i++)
			if (isEhrIdColumn().test(columns.get(i)))
				return i;

		return -1;
	}

	private Predicate<? super Column> isEhrIdColumn()
	{
		return column -> EHRID_COLUMN_NAME.equals(column.getName()) && EHRID_COLUMN_PATH.equals(column.getPath());
	}

	private Function<Column, Column> toNewColumn()
	{
		return c -> new Column(c.getName(), c.getPath());
	}

	private Stream<Column> newMedicIdAndRbfColumn()
	{
		return Stream.of(new Column(MEDICID_COLUMN_NAME, MEDICID_COLUMN_PATH),
				new Column(RBF_COLUMN_NAME, RBF_COLUMN_PATH));
	}

	private List<List<RowElement>> encodeRowsWithEhrId(int ehrIdColumnIndex, List<List<RowElement>> rows)
	{
		return rows.parallelStream().map(encodeRowWithEhrId(ehrIdColumnIndex)).collect(Collectors.toList());
	}
	
	private List<List<RowElement>> encodeRowsWithPsn(int psnColumnIndex, List<List<RowElement>> rows)
	{
		return rows.parallelStream().map(encodeRowWithPsn(psnColumnIndex)).collect(Collectors.toList());
	}

	private Function<List<RowElement>, List<RowElement>> encodeRowWithEhrId(int ehrIdColumnIndex)
	{
		return rowElements ->
		{
			RowElement ehrId = rowElements.get(ehrIdColumnIndex);

			List<RowElement> newRowElements = new ArrayList<>();
			for (int i = 0; i < rowElements.size(); i++)
				if (i != ehrIdColumnIndex)
					newRowElements.add(toEncryptedMdatRowElement(rowElements.get(i)));

			Idat idat = retrieveIdat(ehrId);

			newRowElements.add(encodeAsEncrypedMedicId(idat));
			newRowElements.add(encodeAsRbf(idat));

			return newRowElements;
		};
	}


	private Function<List<RowElement>, List<RowElement>> encodeRowWithPsn(int psnColumnIndex)
	{
		return rowElements ->
		{
			RowElement psn = rowElements.get(psnColumnIndex);

			List<RowElement> newRowElements = new ArrayList<>();
			for (int i = 0; i < rowElements.size(); i++)
				if (i != psnColumnIndex)
					newRowElements.add(toEncryptedMdatRowElement(rowElements.get(i)));

			newRowElements.add(psn);

			return newRowElements;
		};
	}

	
	private RowElement toEncryptedMdatRowElement(RowElement rowElement)
	{
		return new StringRowElement(encrypt(researchStudyKey, researchStudyIdentifier,
				toTypeTag(rowElement) + rowElement.getValueAsString()));
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

	private Idat retrieveIdat(RowElement ehrId)
	{
		if (!(ehrId instanceof StringRowElement))
			throw new IllegalStateException("EhrId " + RowElement.class.getSimpleName() + " of type "
					+ StringRowElement.class.getName() + " expected, but got " + ehrId.getClass().getName());

		return masterPatientIndexClient.fetchIdat(((StringRowElement) ehrId).getValue());
	}

	private RowElement encodeAsEncrypedMedicId(Idat idat)
	{
		return new StringRowElement(encrypt(organizationKey, organizationIdentifier, idat.getMedicId()));
	}

	private RowElement encodeAsRbf(Idat idat)
	{
		RecordBloomFilter recordBloomFilter = recordBloomFilterGenerator.generate(idat);
		String rbfEncodedToString = Base64.getEncoder().encodeToString(recordBloomFilter.getBitSet().toByteArray());
		return new StringRowElement(rbfEncodedToString);
	}

	private String encrypt(SecretKey key, String aadTagValue, String plainText)
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

	private String decrypt(SecretKey key, String aadTagValue, String encryptedText)
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

	@Override
	public ResultSet decode(ResultSet resultSet)
	{
		int psnColumnIndex = getPsnColumnIndex(resultSet.getColumns());
		int medicIdColumnIndex = getMedicIdColumnIndex(resultSet.getColumns());

		Meta newMeta = copyMeta(resultSet.getMeta());
		List<Column> newColumns = decodeColumns(resultSet.getColumns());
		List<List<RowElement>> newRows;

		if (psnColumnIndex >= 0 && medicIdColumnIndex < 0)
			newRows = decodeRowsWithPsnColumn(psnColumnIndex, resultSet.getRows());
		else if (medicIdColumnIndex >= 0 && psnColumnIndex < 0)
			newRows = decodeRowsWithMedicIdColumn(medicIdColumnIndex, resultSet.getRows());
		else
			throw new IllegalStateException("ResultSet contains PSN and MEDICID columns or neither");

		return new ResultSet(newMeta, resultSet.getName(), resultSet.getQuery(), newColumns, newRows);
	}

	private List<Column> decodeColumns(List<Column> columns)
	{
		return columns.stream().map(toNewColumn()).collect(Collectors.toList());
	}

	private List<List<RowElement>> decodeRowsWithPsnColumn(int psnColumnIndex, List<List<RowElement>> rows)
	{
		return rows.parallelStream().map(decodeRowWithPsnColumn(psnColumnIndex)).collect(Collectors.toList());
	}

	private Function<List<RowElement>, List<RowElement>> decodeRowWithPsnColumn(int psnColumnIndex)
	{
		return rowElements ->
		{
			RowElement psn = rowElements.get(psnColumnIndex);

			List<RowElement> newRowElements = new ArrayList<>();
			for (int i = 0; i < rowElements.size(); i++)
				if (i != psnColumnIndex)
					newRowElements.add(toDecryptedMdatRowElement(rowElements.get(i)));

			newRowElements.add(psn);

			return newRowElements;
		};
	}

	private List<List<RowElement>> decodeRowsWithMedicIdColumn(int medicIdColumnIndex, List<List<RowElement>> rows)
	{
		return rows.parallelStream().map(decodeRowWithMedicIdColumn(medicIdColumnIndex)).collect(Collectors.toList());
	}

	private Function<List<RowElement>, List<RowElement>> decodeRowWithMedicIdColumn(int medicIdColumnIndex)
	{
		return rowElements ->
		{
			RowElement medicId = rowElements.get(medicIdColumnIndex);

			List<RowElement> newRowElements = new ArrayList<>();
			for (int i = 0; i < rowElements.size(); i++)
				if (i != medicIdColumnIndex)
					newRowElements.add(toDecryptedMdatRowElement(rowElements.get(i)));

			newRowElements.add(decryptMedicId(medicId.getValueAsString()));

			return newRowElements;
		};
	}

	private RowElement toDecryptedMdatRowElement(RowElement rowElement)
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
					.fromString(decrypt(researchStudyKey, researchStudyIdentifier, tagAndEncrypted.substring(9)));
		else
			throw new IllegalStateException("rowElement with unknown type tag");
	}

	private RowElement decryptMedicId(String encryptedMedicId)
	{
		return new StringRowElement(decrypt(organizationKey, organizationIdentifier, encryptedMedicId));
	}

	private int getPsnColumnIndex(List<Column> columns)
	{
		for (int i = 0; i < columns.size(); i++)
			if (isPsnColumn().test(columns.get(i)))
				return i;

		return -1;
	}

	private int getMedicIdColumnIndex(List<Column> columns)
	{
		for (int i = 0; i < columns.size(); i++)
			if (isMedicIdColumn().test(columns.get(i)))
				return i;

		return -1;
	}

	private Predicate<? super Column> isPsnColumn()
	{
		return column -> PSN_COLUMN_NAME.equals(column.getName()) && PSN_COLUMN_PATH.equals(column.getPath());
	}

	private Predicate<? super Column> isMedicIdColumn()
	{
		return column -> MEDICID_COLUMN_NAME.equals(column.getName()) && MEDICID_COLUMN_PATH.equals(column.getPath());
	}
}
