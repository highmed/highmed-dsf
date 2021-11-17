package org.highmed.pseudonymization.translation;

import static org.highmed.pseudonymization.openehr.Constants.RBF_COLUMN_PATH;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.SecretKey;

import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.Meta;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.openehr.model.structure.RowElement;
import org.highmed.pseudonymization.openehr.Constants;

public class ResultSetTranslatorToTtpEncryptImpl extends AbstractResultSetTranslator
		implements ResultSetTranslatorToTtpEncrypt
{
	private final String organizationIdentifier;
	private final SecretKey organizationKey;
	private final String researchStudyIdentifier;
	private final SecretKey researchStudyKey;
	private final String ehrIdColumnPath;

	public ResultSetTranslatorToTtpEncryptImpl(String organizationIdentifier, SecretKey organizationKey,
			String researchStudyIdentifier, SecretKey researchStudyKey, String ehrIdColumnPath)
	{
		this.organizationIdentifier = Objects.requireNonNull(organizationIdentifier, "organizationIdentifier");
		this.organizationKey = Objects.requireNonNull(organizationKey, "organizationKey");
		this.researchStudyIdentifier = Objects.requireNonNull(researchStudyIdentifier, "researchStudyIdentifier");
		this.researchStudyKey = Objects.requireNonNull(researchStudyKey, "researchStudyKey");
		this.ehrIdColumnPath = Objects.requireNonNull(ehrIdColumnPath, "ehrIdColumnPath");
	}

	@Override
	public ResultSet translate(ResultSet resultSet)
	{
		int ehrIdColumnIndex = getEhrColumnIndex(resultSet.getColumns());
		int rbfColumnIndex = getRbfColumnIndex(resultSet.getColumns());

		if (ehrIdColumnIndex < 0)
			throw new IllegalArgumentException("Missing ehr id column with name '" + Constants.EHRID_COLUMN_NAME
					+ "' and path '" + ehrIdColumnPath + "'");

		if (ehrIdColumnIndex == rbfColumnIndex)
			throw new IllegalArgumentException("Ehr id column index should not be equal to rbf column index");

		Meta meta = copyMeta(resultSet.getMeta());
		List<Column> columns = replaceEhrIdWithMedicIdColumn(resultSet.getColumns());
		List<List<RowElement>> rows = encryptNonRbfValues(ehrIdColumnIndex, rbfColumnIndex, resultSet.getRows());

		return new ResultSet(meta, resultSet.getName(), resultSet.getQuery(), columns, rows);
	}

	private int getEhrColumnIndex(List<Column> columns)
	{
		for (int i = 0; i < columns.size(); i++)
			if (isEhrIdColumn().test(columns.get(i)))
				return i;

		return -1;
	}

	private int getRbfColumnIndex(List<Column> columns)
	{
		for (int i = 0; i < columns.size(); i++)
			if (isRbfColumn().test(columns.get(i)))
				return i;

		return -1;
	}

	private Predicate<? super Column> isEhrIdColumn()
	{
		return column -> Constants.EHRID_COLUMN_NAME.equals(column.getName())
				&& ehrIdColumnPath.equals(column.getPath());
	}

	private Predicate<? super Column> isRbfColumn()
	{
		return column -> Constants.RBF_COLUMN_NAME.equals(column.getName()) && RBF_COLUMN_PATH.equals(column.getPath());
	}

	private List<Column> replaceEhrIdWithMedicIdColumn(List<Column> columns)
	{
		Stream<Column> s1 = columns.stream().filter(isEhrIdColumn().negate()).map(copyColumn());
		Stream<Column> s2 = newMedicIdColumn();
		return Stream.concat(s1, s2).collect(Collectors.toList());
	}

	private Stream<Column> newMedicIdColumn()
	{
		return Stream.of(new Column(Constants.MEDICID_COLUMN_NAME, Constants.MEDICID_COLUMN_PATH));
	}

	private List<List<RowElement>> encryptNonRbfValues(int ehrIdColumnIndex, int rbfColumnIndex,
			List<List<RowElement>> rows)
	{
		return rows.parallelStream().map(encryptNonRbfValue(ehrIdColumnIndex, rbfColumnIndex)).filter(e -> e != null)
				.collect(Collectors.toList());
	}

	private Function<List<RowElement>, List<RowElement>> encryptNonRbfValue(int ehrIdColumnIndex, int rbfColumnIndex)
	{
		return rowElements ->
		{
			List<RowElement> newRowElements = new ArrayList<>();
			for (int i = 0; i < rowElements.size(); i++)
				if (i != ehrIdColumnIndex && i != rbfColumnIndex)
					newRowElements.add(
							toEncryptedMdatRowElement(rowElements.get(i), researchStudyKey, researchStudyIdentifier));
				else if (i == rbfColumnIndex)
					newRowElements.add(rowElements.get(i));

			RowElement ehrId = rowElements.get(ehrIdColumnIndex);
			newRowElements.add(encodeAsEncrypedMedicId(ehrId));

			return newRowElements;
		};
	}

	private RowElement encodeAsEncrypedMedicId(RowElement ehrId)
	{
		return new StringRowElement(encrypt(organizationKey, organizationIdentifier, ehrId.getValueAsString()));
	}
}
