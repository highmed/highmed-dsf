package org.highmed.openehr.client;

import org.highmed.openehr.deserializer.RowElementDeserializer;
import org.highmed.openehr.model.datatypes.DvOther;
import org.highmed.openehr.model.structur.ResultSet;
import org.highmed.openehr.model.structur.RowElement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class TestOpenehrJerseyClient
{
	public static void main(String... args)
	{
		OpenehrWebserviceClient client = new OpenehrWebserviceClientJersey("http://localhost:8003/rest/openehr/v1",
				"username", "password", 2500, 5000, objectMapper());

		String query = "SELECT e FROM EHR e";
		ResultSet resultSet = client.query(query, null);
		//DvCount result = (DvCount) resultSet.getRow(0).get(0);
		DvOther result = (DvOther) resultSet.getRow(0).get(0);
		System.out.println(result.getValue());
	}

	private static ObjectMapper objectMapper()
	{
		ObjectMapper objectMapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addDeserializer(RowElement.class, new RowElementDeserializer());
		objectMapper.registerModule(module);

		return objectMapper;
	}
}
