package org.highmed.openehr.model.structur;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultSet
{
	private final Meta meta;

	private final String name;
	private final String query;

	private final List<Column> columns;
	private final List<List<RowElement>> rows;

	public ResultSet(
			@JsonProperty("meta")
					Meta meta,
			@JsonProperty("name")
					String name,
			@JsonProperty("q")
					String query,
			@JsonProperty("columns")
					List<Column> columns,
			@JsonProperty("rows")
					List<List<RowElement>> rows)
	{
		this.meta = meta;
		this.name = name;
		this.query = query;
		this.columns = columns;
		this.rows = rows;
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
		return columns;
	}

	public List<List<RowElement>> getRows()
	{
		return rows;
	}

	public List<RowElement> getRow(int index)
	{
		return rows.get(index);
	}
}
