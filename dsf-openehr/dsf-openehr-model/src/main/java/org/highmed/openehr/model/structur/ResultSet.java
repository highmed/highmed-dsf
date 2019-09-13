package org.highmed.openehr.model.structur;

import java.util.List;

public class ResultSet
{
	private Meta meta;

	private String name;
	private String q;

	private List<Column> columns;
	private List<List<RowElement>> rows;

	public ResultSet()
	{
	}

	public Meta getMeta()
	{
		return meta;
	}

	public void setMeta(Meta meta)
	{
		this.meta = meta;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getQ()
	{
		return q;
	}

	public void setQ(String q)
	{
		this.q = q;
	}

	public List<Column> getColumns()
	{
		return columns;
	}

	public void setColumns(List<Column> columns)
	{
		this.columns = columns;
	}

	public List<List<RowElement>> getRows()
	{
		return rows;
	}

	public List<RowElement> getRow(int index) {
		return rows.get(index);
	}

	public void addRow(List<RowElement> row)
	{
		rows.add(row);
	}
}
