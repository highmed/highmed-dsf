package org.highmed.pseudonymization.translation;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.highmed.mpi.client.Idat;
import org.highmed.mpi.client.IdatNotFoundException;
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

public class ResultSetTranslatorToTtpCreateRbfImpl extends AbstractResultSetTranslator
		implements ResultSetTranslatorToTtpCreateRbf
{
	private static final Logger logger = LoggerFactory.getLogger(ResultSetTranslatorToTtpCreateRbfImpl.class);

	public static final Function<Supplier<Idat>, Idat> FILTER_ON_IDAT_NOT_FOUND_EXCEPTION = supplier ->
	{
		try
		{
			return supplier.get();
		}
		catch (IdatNotFoundException e)
		{
			logger.warn("Error while retrieving IDAT, filtering entry: {}", e.getMessage());
			return null;
		}
	};

	public static final Function<Supplier<Idat>, Idat> THROW_ON_IDAT_NOT_FOUND_EXCEPTION = supplier ->
	{
		try
		{
			return supplier.get();
		}
		catch (IdatNotFoundException e)
		{
			logger.warn("Error while retrieving IDAT, throwing exception: {}", e.getMessage());
			throw e;
		}
	};

	private final String ehrIdColumnPath;

	private final RecordBloomFilterGenerator recordBloomFilterGenerator;
	private final MasterPatientIndexClient masterPatientIndexClient;

	private final Function<Supplier<Idat>, Idat> retrieveIdatErrorHandler;

	public ResultSetTranslatorToTtpCreateRbfImpl(String ehrIdColumnPath,
			RecordBloomFilterGenerator recordBloomFilterGenerator, MasterPatientIndexClient masterPatientIndexClient)
	{
		this(ehrIdColumnPath, recordBloomFilterGenerator, masterPatientIndexClient, THROW_ON_IDAT_NOT_FOUND_EXCEPTION);
	}

	public ResultSetTranslatorToTtpCreateRbfImpl(String ehrIdColumnPath,
			RecordBloomFilterGenerator recordBloomFilterGenerator, MasterPatientIndexClient masterPatientIndexClient,
			Function<Supplier<Idat>, Idat> retrieveIdatErrorHandler)
	{
		this.ehrIdColumnPath = Objects.requireNonNull(ehrIdColumnPath, "ehrIdColumnPath");

		this.recordBloomFilterGenerator = Objects.requireNonNull(recordBloomFilterGenerator,
				"recordBloomFilterGenerator");
		this.masterPatientIndexClient = Objects.requireNonNull(masterPatientIndexClient, "masterPatientIndexClient");
		this.retrieveIdatErrorHandler = retrieveIdatErrorHandler;
	}

	@Override
	public ResultSet translate(ResultSet resultSet)
	{
		int ehrIdColumnIndex = getEhrColumnIndex(resultSet.getColumns());

		if (ehrIdColumnIndex < 0)
			throw new IllegalArgumentException("Missing ehr id column with name '" + Constants.EHRID_COLUMN_NAME
					+ "' and path '" + ehrIdColumnPath + "'");

		Meta meta = copyMeta(resultSet.getMeta());
		List<Column> columns = addRbfColumn(resultSet.getColumns());
		List<List<RowElement>> rows = translateEhrIdsToRbfs(ehrIdColumnIndex, resultSet.getRows());

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

	private List<Column> addRbfColumn(List<Column> columns)
	{
		Stream<Column> s1 = columns.stream().map(copyColumn());
		Stream<Column> s2 = newRbfColumn();
		return Stream.concat(s1, s2).collect(Collectors.toList());
	}

	private Stream<Column> newRbfColumn()
	{
		return Stream.of(new Column(Constants.RBF_COLUMN_NAME, Constants.RBF_COLUMN_PATH));
	}

	private List<List<RowElement>> translateEhrIdsToRbfs(int ehrIdColumnIndex, List<List<RowElement>> rows)
	{
		return rows.parallelStream().map(translateEhrIdToRbf(ehrIdColumnIndex)).filter(e -> e != null)
				.collect(Collectors.toList());
	}

	private Function<List<RowElement>, List<RowElement>> translateEhrIdToRbf(int ehrIdColumnIndex)
	{
		return rowElements ->
		{
			RowElement ehrId = rowElements.get(ehrIdColumnIndex);
			Idat idat = retrieveIdatErrorHandler.apply(() -> retrieveIdat(ehrId));

			if (idat == null)
				return null;

			List<RowElement> newRowElements = new ArrayList<>(rowElements);
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

	private RowElement encodeAsRbf(Idat idat)
	{
		RecordBloomFilter recordBloomFilter = recordBloomFilterGenerator.generate(idat);
		String rbfEncodedToString = Base64.getEncoder().encodeToString(recordBloomFilter.getBitSet().toByteArray());
		return new StringRowElement(rbfEncodedToString);
	}
}
