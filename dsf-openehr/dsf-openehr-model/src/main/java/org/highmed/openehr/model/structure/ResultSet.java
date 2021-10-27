package org.highmed.openehr.model.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "meta", "name", "q", "columns", "rows" })
public class ResultSet
{
	@JsonProperty("meta")
	private final Meta meta;

	@JsonProperty("name")
	private final String name;
	@JsonProperty("q")
	private final String query;

	@JsonProperty("columns")
	private final List<Column> columns = new ArrayList<>();
	@JsonProperty("rows")
	private final List<List<RowElement>> rows = new ArrayList<>();

	@JsonCreator
	public ResultSet(@JsonProperty("meta") Meta meta, @JsonProperty("name") String name,
			@JsonProperty("q") String query, @JsonProperty("columns") Collection<? extends Column> columns,
			@JsonProperty("rows") Collection<? extends List<RowElement>> rows)
	{
		this.meta = meta;
		this.name = name;
		this.query = query;

		if (columns != null)
			this.columns.addAll(columns);

		if (rows != null)
			for (List<RowElement> r : rows)
				this.rows.add(new ArrayList<RowElement>(r));
	}

	public Meta getMeta()
	{
		return meta;
	}

	public String getName()
	{
		return name;
	}

	public String getQuery()
	{
		return query;
	}

	public List<Column> getColumns()
	{
		return Collections.unmodifiableList(columns);
	}

	public List<List<RowElement>> getRows()
	{
		List<List<RowElement>> rows = new ArrayList<List<RowElement>>();
		for (List<RowElement> r : this.rows)
			rows.add(Collections.unmodifiableList(r));

		return Collections.unmodifiableList(rows);
	}

	public List<RowElement> getRow(int index)
	{
		return Collections.unmodifiableList(rows.get(index));
	}
}
