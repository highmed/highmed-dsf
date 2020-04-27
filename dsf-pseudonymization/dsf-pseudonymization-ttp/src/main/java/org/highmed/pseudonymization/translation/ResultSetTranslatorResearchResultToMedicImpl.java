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
import org.highmed.pseudonymization.domain.PersonWithMdat;
import org.highmed.pseudonymization.domain.impl.OpenEhrMdatContainer;
import org.highmed.pseudonymization.openehr.Constants;

public class ResultSetTranslatorResearchResultToMedicImpl implements ResultSetTranslatorResearchResultToMedic
{
	@Override
	public ResultSet translate(List<Column> columns, List<PersonWithMdat> personsWithMdat)
	{
		Meta newMeta = createMeta();
		List<Column> newColumns = createColumns(columns);
		return new ResultSet(newMeta, "", "", newColumns,
				personsWithMdat.parallelStream().map(toRows()).collect(Collectors.toList()));
	}

	private Meta createMeta()
	{
		return new Meta("", "", "", LocalDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), "", "");
	}

	private List<Column> createColumns(List<Column> columns)
	{
		return Stream
				.concat(columns.stream().map(toNewColumn()),
						Stream.of(new Column(Constants.MEDICID_COLUMN_NAME, Constants.MEDICID_COLUMN_PATH)))
				.collect(Collectors.toList());
	}

	private Function<Column, Column> toNewColumn()
	{
		return c -> new Column(c.getName(), c.getPath());
	}

	private Function<PersonWithMdat, List<RowElement>> toRows()
	{
		return person ->
		{
			MdatContainer mdatContainer = person.getMdatContainer();
			if (mdatContainer instanceof OpenEhrMdatContainer)
			{
				String medicId = person.getMedicId().getValue();
				return Stream.concat(((OpenEhrMdatContainer) mdatContainer).getElements().stream(),
						Stream.of(new StringRowElement(medicId))).collect(Collectors.toList());
			}
			else
				throw new IllegalArgumentException("MdatContainer of type " + OpenEhrMdatContainer.class.getName()
						+ " expected, but got " + mdatContainer.getClass().getName());
		};
	}
}
