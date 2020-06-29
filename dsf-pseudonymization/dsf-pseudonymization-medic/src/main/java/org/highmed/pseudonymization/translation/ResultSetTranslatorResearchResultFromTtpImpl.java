package org.highmed.pseudonymization.translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.Meta;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.openehr.model.structure.RowElement;
import org.highmed.pseudonymization.openehr.Constants;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultSetTranslatorResearchResultFromTtpImpl extends AbstractResultSetTranslator
		implements ResultSetTranslatorResearchResultFromTtp
{
	private final SecretKey organizationKey;
	private final String organizationIdentifier;
	private final String researchStudyIdentifier;
	private final SecretKey researchStudyKey;

	private final ObjectMapper openEhrObjectMapper;

	public ResultSetTranslatorResearchResultFromTtpImpl(SecretKey organizationKey, String organizationIdentifier,
			String researchStudyIdentifier, SecretKey researchStudyKey, ObjectMapper openEhrObjectMapper)
	{
		this.organizationKey = Objects.requireNonNull(organizationKey, "organizationKey");
		this.organizationIdentifier = Objects.requireNonNull(organizationIdentifier, "organizationIdentifier");
		this.researchStudyIdentifier = Objects.requireNonNull(researchStudyIdentifier, "researchStudyIdentifier");
		this.researchStudyKey = Objects.requireNonNull(researchStudyKey, "researchStudyKey");
		this.openEhrObjectMapper = Objects.requireNonNull(openEhrObjectMapper, "openEhrObjectMapper");
	}

	@Override
	public ResultSet translate(ResultSet resultSet)
	{
		int medicIdColumnIndex = getMedicIdColumnIndex(resultSet.getColumns());

		if (medicIdColumnIndex < 0)
			throw new IllegalArgumentException("Missing MeDIC id column with name '" + Constants.MEDICID_COLUMN_NAME
					+ "' and path '" + Constants.MEDICID_COLUMN_PATH + "'");

		Meta meta = copyMeta(resultSet.getMeta());
		List<Column> columns = copyColumns(resultSet.getColumns());
		List<List<RowElement>> newRows = decodeRowsWithMedicIdColumn(medicIdColumnIndex, resultSet.getRows());

		return new ResultSet(meta, resultSet.getName(), resultSet.getQuery(), columns, newRows);
	}

	private int getMedicIdColumnIndex(List<Column> columns)
	{
		for (int i = 0; i < columns.size(); i++)
			if (isMedicIdColumn().test(columns.get(i)))
				return i;

		return -1;
	}

	private Predicate<? super Column> isMedicIdColumn()
	{
		return column -> Constants.MEDICID_COLUMN_NAME.equals(column.getName())
				&& Constants.MEDICID_COLUMN_PATH.equals(column.getPath());
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
					newRowElements.add(toDecryptedMdatRowElement(rowElements.get(i), researchStudyKey,
							researchStudyIdentifier, openEhrObjectMapper));

			newRowElements.add(decryptMedicId(medicId.getValueAsString()));

			return newRowElements;
		};
	}

	private RowElement decryptMedicId(String encryptedMedicId)
	{
		return new StringRowElement(decrypt(organizationKey, organizationIdentifier, encryptedMedicId));
	}
}
