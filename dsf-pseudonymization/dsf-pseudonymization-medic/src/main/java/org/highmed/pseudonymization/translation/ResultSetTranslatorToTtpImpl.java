package org.highmed.pseudonymization.translation;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.SecretKey;

import org.highmed.mpi.client.Idat;
import org.highmed.mpi.client.MasterPatientIndexClient;
import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.Meta;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.openehr.model.structure.RowElement;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilter;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGenerator;
import org.highmed.pseudonymization.openehr.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultSetTranslatorToTtpImpl extends AbstractResultSetTranslator implements ResultSetTranslatorToTtp
{
	private static final Logger logger = LoggerFactory.getLogger(ResultSetTranslatorToTtpImpl.class);

	private final String organizationIdentifier;
	private final SecretKey organizationKey;
	private final String researchStudyIdentifier;
	private final SecretKey researchStudyKey;
	private final String ehrIdColumnPath;

	private final RecordBloomFilterGenerator recordBloomFilterGenerator;
	private final MasterPatientIndexClient masterPatientIndexClient;

	public ResultSetTranslatorToTtpImpl(String organizationIdentifier, SecretKey organizationKey,
			String researchStudyIdentifier, SecretKey researchStudyKey, String ehrIdColumnPath,
			RecordBloomFilterGenerator recordBloomFilterGenerator, MasterPatientIndexClient masterPatientIndexClient)
	{
		this.organizationIdentifier = Objects.requireNonNull(organizationIdentifier, "organizationIdentifier");
		this.organizationKey = Objects.requireNonNull(organizationKey, "organizationKey");
		this.researchStudyIdentifier = Objects.requireNonNull(researchStudyIdentifier, "researchStudyIdentifier");
		this.researchStudyKey = Objects.requireNonNull(researchStudyKey, "researchStudyKey");
		this.ehrIdColumnPath = Objects.requireNonNull(ehrIdColumnPath, "ehrIdColumnPath");

		this.masterPatientIndexClient = Objects.requireNonNull(masterPatientIndexClient, "masterPatientIndexClient");
		this.recordBloomFilterGenerator = Objects.requireNonNull(recordBloomFilterGenerator,
				"recordBloomFilterGenerator");
	}

	@Override
	public ResultSet translate(ResultSet resultSet)
	{
		int ehrIdColumnIndex = getEhrColumnIndex(resultSet.getColumns());

		if (ehrIdColumnIndex < 0)
			throw new IllegalArgumentException("Missing ehr id column with name '" + Constants.EHRID_COLUMN_NAME
					+ "' and path '" + ehrIdColumnPath + "'");

		Meta meta = copyMeta(resultSet.getMeta());
		List<Column> columns = encodeColumnsWithEhrId(resultSet.getColumns());
		List<List<RowElement>> rows = encodeRowsWithEhrId(ehrIdColumnIndex, resultSet.getRows());

		return new ResultSet(meta, resultSet.getName(), resultSet.getQuery(), columns, rows);
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
		return column -> Constants.EHRID_COLUMN_NAME.equals(column.getName())
				&& ehrIdColumnPath.equals(column.getPath());
	}

	private List<Column> encodeColumnsWithEhrId(List<Column> columns)
	{
		Stream<Column> s1 = columns.stream().filter(isEhrIdColumn().negate()).map(copyColumn());
		Stream<Column> s2 = newMedicIdAndRbfColumn();
		return Stream.concat(s1, s2).collect(Collectors.toList());
	}

	private Stream<Column> newMedicIdAndRbfColumn()
	{
		return Stream.of(new Column(Constants.MEDICID_COLUMN_NAME, Constants.MEDICID_COLUMN_PATH),
				new Column(Constants.RBF_COLUMN_NAME, Constants.RBF_COLUMN_PATH));
	}

	private List<List<RowElement>> encodeRowsWithEhrId(int ehrIdColumnIndex, List<List<RowElement>> rows)
	{
		return rows.parallelStream().map(encodeRowWithEhrId(ehrIdColumnIndex)).collect(Collectors.toList());
	}

	private Function<List<RowElement>, List<RowElement>> encodeRowWithEhrId(int ehrIdColumnIndex)
	{
		return rowElements ->
		{
			RowElement ehrId = rowElements.get(ehrIdColumnIndex);

			List<RowElement> newRowElements = new ArrayList<>();
			for (int i = 0; i < rowElements.size(); i++)
				if (i != ehrIdColumnIndex)
					newRowElements.add(
							toEncryptedMdatRowElement(rowElements.get(i), researchStudyKey, researchStudyIdentifier));

			Idat idat = retrieveIdat(ehrId);

			newRowElements.add(encodeAsEncrypedMedicId(idat));
			newRowElements.add(encodeAsRbf(idat));

			return newRowElements;
		};
	}

	private Idat retrieveIdat(RowElement ehrId)
	{
		if (!(ehrId instanceof StringRowElement))
			throw new IllegalStateException("EhrId " + RowElement.class.getSimpleName() + " of type "
					+ StringRowElement.class.getName() + " expected, but got " + ehrId.getClass().getName());

		String id = ((StringRowElement) ehrId).getValue();
		try
		{
			Idat fetchedIdat = masterPatientIndexClient.fetchIdat(id);

			if (fetchedIdat == null)
				throw new RuntimeException("IDAT could not be fetched, client returned null for ID " + id);

			return fetchedIdat;
		}
		catch (Exception e)
		{
			logger.warn("Error while fetching IDAT from MPI for ID " + id, e);
			throw e;
		}
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
}
