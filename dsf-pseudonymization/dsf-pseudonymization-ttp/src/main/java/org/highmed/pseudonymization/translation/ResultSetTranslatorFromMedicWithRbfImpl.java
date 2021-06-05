package org.highmed.pseudonymization.translation;

import java.util.ArrayList;
import java.util.Base64;
import java.util.BitSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.openehr.model.structure.RowElement;
import org.highmed.pseudonymization.domain.PersonWithMdat;
import org.highmed.pseudonymization.domain.impl.MedicIdImpl;
import org.highmed.pseudonymization.domain.impl.OpenEhrMdatContainer;
import org.highmed.pseudonymization.domain.impl.PersonImpl;
import org.highmed.pseudonymization.openehr.Constants;
import org.highmed.pseudonymization.recordlinkage.MedicId;

public class ResultSetTranslatorFromMedicWithRbfImpl implements ResultSetTranslatorFromMedicWithRbf
{
	@Override
	public List<PersonWithMdat> translate(String organization, ResultSet resultSet)
	{
		int medicIdColumnIndex = getMedicIdColumnIndex(resultSet.getColumns());
		int rbfColumnIndex = getRbfColumnIndex(resultSet.getColumns());

		if (medicIdColumnIndex < 0)
			throw new IllegalArgumentException("Missing MeDIC id column with name '" + Constants.MEDICID_COLUMN_NAME
					+ "' and path '" + Constants.MEDICID_COLUMN_PATH + "'");
		if (rbfColumnIndex < 0)
			throw new IllegalArgumentException("Missing RBF column with name '" + Constants.RBF_COLUMN_NAME
					+ "' and path '" + Constants.RBF_COLUMN_PATH + "'");

		return resultSet.getRows().parallelStream()
				.map(toPersonWithMdat(organization, medicIdColumnIndex, rbfColumnIndex)).collect(Collectors.toList());
	}

	private Function<List<RowElement>, PersonWithMdat> toPersonWithMdat(String organization, int medicIdColumnIndex,
			int rbfColumnIndex)
	{
		return rowElements ->
		{
			MedicId medicId = getMedicId(organization, rowElements.get(medicIdColumnIndex));
			BitSet recordBloomFilter = getRecordBloomFilter(rowElements.get(rbfColumnIndex));
			List<RowElement> elements = new ArrayList<>();
			for (int i = 0; i < rowElements.size(); i++)
				if (i != medicIdColumnIndex && i != rbfColumnIndex)
					elements.add(rowElements.get(i));

			return new PersonImpl(medicId, recordBloomFilter, new OpenEhrMdatContainer(elements));
		};
	}

	private MedicId getMedicId(String organization, RowElement rowElement)
	{
		if (rowElement instanceof StringRowElement)
			return new MedicIdImpl(organization, ((StringRowElement) rowElement).getValue());
		else
			throw new IllegalArgumentException("RowElement of type " + StringRowElement.class.getName()
					+ " expected but got " + rowElement.getClass().getName());
	}

	private BitSet getRecordBloomFilter(RowElement rowElement)
	{
		if (rowElement instanceof StringRowElement)
		{
			String rbfString = ((StringRowElement) rowElement).getValue();
			byte[] rbfBytes = Base64.getDecoder().decode(rbfString);
			return BitSet.valueOf(rbfBytes);
		}
		else
			throw new IllegalArgumentException("RowElement of type " + StringRowElement.class.getName()
					+ " expected, but got " + rowElement.getClass().getName());
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

	private int getRbfColumnIndex(List<Column> columns)
	{
		for (int i = 0; i < columns.size(); i++)
			if (isRbfColumn().test(columns.get(i)))
				return i;

		return -1;
	}

	private Predicate<? super Column> isRbfColumn()
	{
		return column -> Constants.RBF_COLUMN_NAME.equals(column.getName())
				&& Constants.RBF_COLUMN_PATH.equals(column.getPath());
	}
}
