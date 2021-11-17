package org.highmed.openehr.client.stub;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ws.rs.core.MultivaluedMap;

import org.highmed.openehr.client.OpenEhrClient;
import org.highmed.openehr.model.datatypes.IntegerRowElement;
import org.highmed.openehr.model.datatypes.JsonNodeRowElement;
import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.openehr.model.structure.RowElement;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenEhrClientStub implements OpenEhrClient
{
	private final ObjectMapper openEhrObjectMapper;

	protected OpenEhrClientStub(ObjectMapper openEhrObjectMapper)
	{
		this.openEhrObjectMapper = openEhrObjectMapper;
	}

	@Override
	public ResultSet query(String query, MultivaluedMap<String, Object> headers)
	{
		// TODO Implement correct check for default id query
		if (query.toLowerCase().startsWith("select count"))
		{
			List<List<RowElement>> rows = Collections
					.singletonList(Collections.singletonList(new IntegerRowElement(15)));

			return new ResultSet(null, null, query, Collections.singleton(new Column("COUNT", "/count")), rows);
		}
		else
		{
			List<Column> columns = getColumns();
			List<List<RowElement>> rows = IntStream.range(0, 15).mapToObj(this::getRow).collect(Collectors.toList());

			return new ResultSet(null, null, query, columns, rows);
		}
	}

	private List<Column> getColumns()
	{
		return Arrays.asList(new Column("EHRID", "/ehr_status/subject/external_ref/id/value"),
				new Column("#1", "/items[at0024]/value"), new Column("#2", "/items[at0001]"),
				new Column("#2", "/items[at0006]/value"));
	}

	private List<RowElement> getRow(int id)
	{
		return Arrays.asList(StringRowElement.fromString(String.valueOf(id)),
				JsonNodeRowElement.fromString(
						"{\"_type\":\"DV_CODED_TEXT\",\"value\":\"Natrium\",\"mappings\":[{\"_type\":\"TERM_MAPPING\","
								+ "\"match\":\"?\",\"target\":{\"_type\":\"CODE_PHRASE\",\"terminology_id\":{\"_type\":"
								+ "\"TERMINOLOGY_ID\",\"value\":\"http://loinc.org\"},\"code_string\":\"2951-2\"}}],"
								+ "\"defining_code\":{\"_type\":\"CODE_PHRASE\",\"terminology_id\":{\"_type\":"
								+ "\"TERMINOLOGY_ID\",\"value\":\"SWL\"},\"code_string\":\"NA\"}}",
						openEhrObjectMapper),
				JsonNodeRowElement.fromString("{\"_type\":\"ELEMENT\",\"name\":{\"_type\":\"DV_TEXT\","
						+ "\"value\":\"Messwert\"},\"uid\":{\"_type\":\"OBJECT_VERSION_ID\",\"value\":"
						+ "\"3880f8fa-428c-4cd4-adee-1f8f39c03b94\"},\"archetype_node_id\":\"at0001\","
						+ "\"value\":{\"_type\":\"DV_QUANTITY\",\"normal_range\":{\"_type\":\"DV_INTERVAL\","
						+ "\"lower\":{\"_type\":\"DV_QUANTITY\",\"magnitude\":135.0,\"units\":\"mmol/L\"},"
						+ "\"upper\":{\"_type\":\"DV_QUANTITY\",\"magnitude\":146.0,\"units\":\"mmol/L\"},"
						+ "\"lower_included\":true,\"upper_included\":true,\"lower_unbounded\":false,"
						+ "\"upper_unbounded\":false},\"magnitude_status\":\"=\",\"magnitude\":144.0,"
						+ "\"units\":\"mmol/L\"}}", openEhrObjectMapper),
				JsonNodeRowElement.fromString("{\"_type\":\"DV_DATE_TIME\",\"value\":\"2019-08-08T14:21:00\"}",
						openEhrObjectMapper));
	}
}
