package org.highmed.pseudonymization.translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.Meta;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.openehr.model.structure.RowElement;
import org.highmed.pseudonymization.openehr.Constants;

public class ResultSetTranslatorResearchResultToTtpImpl extends AbstractResultSetTranslator
		implements ResultSetTranslatorResearchResultToTtp
{
	private final String researchStudyIdentifier;
	private final SecretKey researchStudyKey;

	public ResultSetTranslatorResearchResultToTtpImpl(String researchStudyIdentifier, SecretKey researchStudyKey)
	{
		this.researchStudyIdentifier = Objects.requireNonNull(researchStudyIdentifier, "researchStudyIdentifier");
		this.researchStudyKey = Objects.requireNonNull(researchStudyKey, "researchStudyKey");
	}

	@Override
	public ResultSet translate(ResultSet resultSet)
	{
		int psnColumnIndex = getPsnColumnIndex(resultSet.getColumns());

		if (psnColumnIndex < 0)
			throw new IllegalArgumentException("Missing psn column with name '" + Constants.PSN_COLUMN_NAME
					+ "' and path '" + Constants.PSN_COLUMN_PATH + "'");

		Meta meta = copyMeta(resultSet.getMeta());
		List<Column> columns = copyColumns(resultSet.getColumns());
		List<List<RowElement>> rows = encodeRowsWithPsn(psnColumnIndex, resultSet.getRows());

		return new ResultSet(meta, resultSet.getName(), resultSet.getQuery(), columns, rows);
	}

	private List<List<RowElement>> encodeRowsWithPsn(int psnColumnIndex, List<List<RowElement>> rows)
	{
		return rows.parallelStream().map(encodeRowWithPsn(psnColumnIndex)).collect(Collectors.toList());
	}

	private Function<List<RowElement>, List<RowElement>> encodeRowWithPsn(int psnColumnIndex)
	{
		return rowElements ->
		{
			RowElement psn = rowElements.get(psnColumnIndex);

			List<RowElement> newRowElements = new ArrayList<>();
			for (int i = 0; i < rowElements.size(); i++)
				if (i != psnColumnIndex)
					newRowElements.add(
							toEncryptedMdatRowElement(rowElements.get(i), researchStudyKey, researchStudyIdentifier));

			newRowElements.add(copyRowElement(psn));

			return newRowElements;
		};
	}
}
