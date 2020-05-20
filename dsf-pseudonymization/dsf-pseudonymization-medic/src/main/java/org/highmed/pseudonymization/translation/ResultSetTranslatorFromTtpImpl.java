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

import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultSetTranslatorFromTtpImpl extends AbstractResultSetTranslator implements ResultSetTranslatorFromTtp
{
	private final String researchStudyIdentifier;
	private final SecretKey researchStudyKey;

	private final ObjectMapper openEhrObjectMapper;

	public ResultSetTranslatorFromTtpImpl(String researchStudyIdentifier, SecretKey researchStudyKey,
			ObjectMapper openEhrObjectMapper)
	{
		this.researchStudyIdentifier = Objects.requireNonNull(researchStudyIdentifier, "researchStudyIdentifier");
		this.researchStudyKey = Objects.requireNonNull(researchStudyKey, "researchStudyKey");
		this.openEhrObjectMapper = Objects.requireNonNull(openEhrObjectMapper, "openEhrObjectMapper");
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
		List<List<RowElement>> rows = decodeRowsWithPsnColumn(psnColumnIndex, resultSet.getRows());

		return new ResultSet(meta, resultSet.getName(), resultSet.getQuery(), columns, rows);
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
					newRowElements.add(toDecryptedMdatRowElement(rowElements.get(i), researchStudyKey,
							researchStudyIdentifier, openEhrObjectMapper));

			newRowElements.add(psn);

			return newRowElements;
		};
	}
}
