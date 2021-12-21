package org.highmed.openehr.client.impl;

import org.highmed.openehr.client.OpenEhrClient;
import org.highmed.openehr.json.OpenEhrObjectMapperFactory;
import org.highmed.openehr.model.datatypes.JsonNodeRowElement;
import org.highmed.openehr.model.structure.ResultSet;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestOpenEhrClientJersey
{
	public static void main(String... args) throws Exception
	{
		OpenEhrClient client = new OpenEhrClientJersey("http://localhost:8003/rest/openehr/v1", "username", "password",
				"truststore.pem", 2000, 10000, objectMapper());

		String query = "SELECT e FROM EHR e";
		ResultSet resultSet = client.query(query, null);
		JsonNodeRowElement result = (JsonNodeRowElement) resultSet.getRow(0).get(0);
		System.out.println(result.getValue());
	}

	private static ObjectMapper objectMapper()
	{
		return OpenEhrObjectMapperFactory.createObjectMapper();
	}
}

