package org.highmed.consent.client.stub;

import java.util.List;
import java.util.function.Predicate;

import org.highmed.consent.client.ConsentClient;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsentClientStub implements ConsentClient
{
	private static final Logger logger = LoggerFactory.getLogger(ConsentClientStub.class);

	private final String ehrIdColumnName;
	private final String ehrIdColumnPath;

	protected ConsentClientStub(String ehrIdColumnName, String ehrIdColumnPath)
	{
		this.ehrIdColumnName = ehrIdColumnName;
		this.ehrIdColumnPath = ehrIdColumnPath;
	}

	@Override
	public ResultSet removeRowsWithoutConsent(ResultSet resultSet)
	{
		if (getEhrColumnIndex(resultSet.getColumns()) < 0)
			throw new IllegalArgumentException("ResultSet does not contain an openEHR-EHR-ID");

		logger.warn("Assuming that consent is given for all rows, ResultSet will be returned as provided");

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
