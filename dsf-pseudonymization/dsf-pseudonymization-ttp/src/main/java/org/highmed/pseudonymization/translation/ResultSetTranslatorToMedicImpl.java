package org.highmed.pseudonymization.translation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.Meta;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.openehr.model.structure.RowElement;
import org.highmed.pseudonymization.domain.MdatContainer;
import org.highmed.pseudonymization.domain.PseudonymizedPersonWithMdat;
import org.highmed.pseudonymization.domain.impl.OpenEhrMdatContainer;
import org.highmed.pseudonymization.openehr.Constants;

public class ResultSetTranslatorToMedicImpl implements ResultSetTranslatorToMedic
{
	@Override
	public ResultSet translate(List<Column> columns, List<PseudonymizedPersonWithMdat> pseudonymsWithMdat)
	{
		Meta newMeta = createMeta();
		List<Column> newColumns = createColumns(columns);
		return new ResultSet(newMeta, "", "", newColumns,
				pseudonymsWithMdat.parallelStream().flatMap(toRows()).collect(Collectors.toList()));
	}

	private Meta createMeta()
	{
		return new Meta("", "", "", LocalDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), "", "");
	}

	private List<Column> createColumns(List<Column> columns)
	{
		return Stream
				.concat(columns.stream().map(toNewColumn()),
						Stream.of(new Column(Constants.PSN_COLUMN_NAME, Constants.PSN_COLUMN_PATH)))
				.collect(Collectors.toList());
	}

	private Function<Column, Column> toNewColumn()
	{
		return c -> new Column(c.getName(), c.getPath());
	}

	private Function<PseudonymizedPersonWithMdat, Stream<List<RowElement>>> toRows()
	{
		return person -> person.getMdatContainers().stream().map(toRows(person.getPseudonym()));
	}

	private Function<MdatContainer, List<RowElement>> toRows(String pseudonym)
	{
		return mdatContainer ->
		{
			if (mdatContainer instanceof OpenEhrMdatContainer)
			{
				return Stream.concat(((OpenEhrMdatContainer) mdatContainer).getElements().stream(),
						Stream.of(new StringRowElement(pseudonym))).collect(Collectors.toList());
			}
			else
				throw new IllegalArgumentException("MdatContainer of type " + OpenEhrMdatContainer.class.getName()
						+ " expected, but got " + mdatContainer.getClass().getName());
		};
	}
}
