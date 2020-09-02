package org.highmed.pseudonymization.translation;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

public class ResultSetTranslatorToTtpRbfOnlyImpl extends AbstractResultSetTranslator
		implements ResultSetTranslatorToTtpRbfOnly
{
	private static final Logger logger = LoggerFactory.getLogger(ResultSetTranslatorToTtpRbfOnlyImpl.class);

	private final String ehrIdColumnPath;

	private final RecordBloomFilterGenerator recordBloomFilterGenerator;
	private final MasterPatientIndexClient masterPatientIndexClient;

	public ResultSetTranslatorToTtpRbfOnlyImpl(String ehrIdColumnPath,
			RecordBloomFilterGenerator recordBloomFilterGenerator,
			MasterPatientIndexClient masterPatientIndexClient)
	{
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
		List<Column> columns = translageColumns(resultSet.getColumns());
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

	private List<Column> translageColumns(List<Column> columns)
	{
		return Collections.singletonList(new Column(Constants.RBF_COLUMN_NAME, Constants.RBF_COLUMN_PATH));
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
			Idat idat = retrieveIdat(ehrId);

			RowElement rbf = encodeAsRbf(idat);
			return Collections.singletonList(rbf);
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

	private RowElement encodeAsRbf(Idat idat)
	{
		RecordBloomFilter recordBloomFilter = recordBloomFilterGenerator.generate(idat);
		String rbfEncodedToString = Base64.getEncoder().encodeToString(recordBloomFilter.getBitSet().toByteArray());
		return new StringRowElement(rbfEncodedToString);
	}
}
