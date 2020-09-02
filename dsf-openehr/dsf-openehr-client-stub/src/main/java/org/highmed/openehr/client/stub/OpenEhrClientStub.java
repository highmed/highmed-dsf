package org.highmed.openehr.client.stub;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ws.rs.core.MultivaluedMap;

import org.highmed.openehr.client.OpenEhrClient;
import org.highmed.openehr.model.datatypes.IntegerRowElement;
import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.openehr.model.structure.RowElement;

public class OpenEhrClientStub implements OpenEhrClient
{
	@Override
	public ResultSet query(String query, MultivaluedMap<String, Object> headers)
	{
		// TODO Implement correct check for default id query
		if (!query.startsWith("select count"))
		{
			List<List<RowElement>> rows = IntStream.range(0, 15)
					.mapToObj(id -> Collections.<RowElement>singletonList(new StringRowElement(String.valueOf(id))))
					.collect(Collectors.toList());

			return new ResultSet(null, null, query, Collections.singleton(new Column("EHRID", "/ehr_status/subject/external_ref/id/value")), rows);
		}
		else
		{
			List<List<RowElement>> rows = Collections
					.singletonList(Collections.singletonList(new IntegerRowElement(15)));

			return new ResultSet(null, null, query, Collections.singleton(new Column("COUNT", "/count")), rows);
		}
	}
}
