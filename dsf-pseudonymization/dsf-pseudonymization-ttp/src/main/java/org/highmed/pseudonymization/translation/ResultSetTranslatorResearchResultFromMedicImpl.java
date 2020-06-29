package org.highmed.pseudonymization.translation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.openehr.model.structure.RowElement;
import org.highmed.pseudonymization.domain.PseudonymizedPersonWithMdat;
import org.highmed.pseudonymization.domain.impl.OpenEhrMdatContainer;
import org.highmed.pseudonymization.domain.impl.PseudonymizedPersonImpl;
import org.highmed.pseudonymization.openehr.Constants;

public class ResultSetTranslatorResearchResultFromMedicImpl implements ResultSetTranslatorResearchResultFromMedic
{
	@Override
	public List<PseudonymizedPersonWithMdat> translate(ResultSet resultSet)
	{
		int psnColumnIndex = getPsnColumnIndex(resultSet.getColumns());

		if (psnColumnIndex < 0)
			throw new IllegalArgumentException("Missing PSN column with name '" + Constants.PSN_COLUMN_NAME
					+ "' and path '" + Constants.PSN_COLUMN_PATH + "'");

		return resultSet.getRows().stream().map(toPseudonymizedPersonWithMdat(psnColumnIndex))
				.collect(Collectors.toList());
	}

	private int getPsnColumnIndex(List<Column> columns)
	{
		for (int i = 0; i < columns.size(); i++)
			if (isPsnColumn().test(columns.get(i)))
				return i;

		return -1;
	}

	private Predicate<? super Column> isPsnColumn()
	{
		return column -> Constants.PSN_COLUMN_NAME.equals(column.getName())
				&& Constants.PSN_COLUMN_PATH.equals(column.getPath());
	}

	private Function<List<RowElement>, PseudonymizedPersonWithMdat> toPseudonymizedPersonWithMdat(int psnColumnIndex)
	{
		return rowElements ->
		{
			String pseudonym = getPseudonym(rowElements.get(psnColumnIndex));
			List<RowElement> newRowElements = new ArrayList<>();
			for (int i = 0; i < rowElements.size(); i++)
				if (i != psnColumnIndex)
					newRowElements.add(rowElements.get(i));

			return new PseudonymizedPersonImpl(pseudonym,
					Collections.singleton(new OpenEhrMdatContainer(newRowElements)));
		};
	}

	private String getPseudonym(RowElement rowElement)
	{
		if (rowElement instanceof StringRowElement)
			return rowElement.getValueAsString();
		else
			throw new IllegalArgumentException("RowElement of type " + StringRowElement.class.getName()
					+ " expected, but got " + rowElement.getClass().getName());
	}
}
