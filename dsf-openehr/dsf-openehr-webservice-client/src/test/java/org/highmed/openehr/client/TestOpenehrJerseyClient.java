package org.highmed.openehr.client;

import org.highmed.openehr.OpenEhrObjectMapperFactory;
import org.highmed.openehr.model.datatypes.JsonNodeRowElement;
import org.highmed.openehr.model.structure.ResultSet;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestOpenehrJerseyClient
{
	public static void main(String... args)
	{
		OpenehrWebserviceClient client = new OpenehrWebserviceClientJersey("http://localhost:8003/rest/openehr/v1",
				"username", "password", 2500, 5000, objectMapper());

		String query = "SELECT e FROM EHR e";
		ResultSet resultSet = client.query(query, null);
		// DvCount result = (DvCount) resultSet.getRow(0).get(0);
		JsonNodeRowElement result = (JsonNodeRowElement) resultSet.getRow(0).get(0);
		System.out.println(result.getValue());
	}

	private static ObjectMapper objectMapper()
	{
		return OpenEhrObjectMapperFactory.createObjectMapper();
	}
}
