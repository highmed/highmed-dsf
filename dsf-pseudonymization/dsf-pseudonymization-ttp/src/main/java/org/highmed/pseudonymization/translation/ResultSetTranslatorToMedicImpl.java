package org.highmed.pseudonymization.translation;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
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
	public ResultSet translate(Meta meta, List<Column> columns, List<PseudonymizedPersonWithMdat> pseudonymsWithMdat)
	{
		Meta newMeta = createMeta(meta);
		List<Column> newColumns = createColumns(columns);
		return new ResultSet(newMeta, "", "", newColumns,
				pseudonymsWithMdat.parallelStream().flatMap(toRows()).collect(Collectors.toList()));
	}

	private Meta createMeta(Meta meta)
	{
		return new Meta(meta.getHref(), meta.getType(), meta.getSchemaVersion(),
				ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), meta.getGenerator(),
				meta.getExecutedAql());
	}

	private List<Column> createColumns(List<Column> columns)
	{
		return Stream
				.concat(columns.stream().filter(isMedicIdOrRbfColumn().negate()).map(toNewColumn()),
						Stream.of(new Column(Constants.PSN_COLUMN_NAME, Constants.PSN_COLUMN_PATH)))
				.collect(Collectors.toList());
	}

	private Predicate<? super Column> isMedicIdOrRbfColumn()
	{
		return column -> (Constants.MEDICID_COLUMN_NAME.equals(column.getName())
				&& Constants.MEDICID_COLUMN_PATH.equals(column.getPath()))
				|| (Constants.RBF_COLUMN_NAME.equals(column.getName())
						&& Constants.RBF_COLUMN_PATH.equals(column.getPath()));
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
