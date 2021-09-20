package org.highmed.consent.client.stub;

import java.util.List;
import java.util.function.Predicate;

import org.highmed.consent.client.ConsentClient;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.ResultSet;

public class ConsentClientStub implements ConsentClient
{
	private final String ehrIdColumnName;
	private final String ehrIdColumnPath;

	protected ConsentClientStub(String ehrIdColumnName, String ehrIdColumnPath)
	{
		this.ehrIdColumnName = ehrIdColumnName;
		this.ehrIdColumnPath = ehrIdColumnPath;
	}

	@Override
	public ResultSet check(ResultSet resultSet)
	{
		if (getEhrColumnIndex(resultSet.getColumns()) < 0)
			throw new IllegalArgumentException("ResultSet does not contain an openEHR-EHR-ID");

		return resultSet;
	}

	private int getEhrColumnIndex(List<Column> columns)
	{
		for (int i = 0; i < columns.size(); i++)
			if (isEhrIdColumn().test(columns.get(i)))
				return i;

		return -1;
	}

	private Predicate<? super Column> isEhrIdColumn()
	{
		return column -> ehrIdColumnName.equals(column.getName()) && ehrIdColumnPath.equals(column.getPath());
	}
}
