package org.highmed.openehr.client.stub;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ws.rs.core.MultivaluedMap;

import org.highmed.openehr.client.OpenEhrClient;
import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.openehr.model.structure.RowElement;

public class OpenEhrClientStub implements OpenEhrClient
{
	@Override
	public ResultSet query(String query, MultivaluedMap<String, Object> headers)
	{
		int start = 0;
		int end = 15;

		if(query.startsWith("SELECT COUNT(e)"))
		{
			start = 15;
		}

		List<List<RowElement>> rows = IntStream.range(start, end)
				.mapToObj(id -> Collections.<RowElement> singletonList(new StringRowElement(String.valueOf(id))))
				.collect(Collectors.toList());

		return new ResultSet(null, null, query,
				Collections.singleton(new Column("EHRID", "/ehr_id/value")), rows);
	}
}
