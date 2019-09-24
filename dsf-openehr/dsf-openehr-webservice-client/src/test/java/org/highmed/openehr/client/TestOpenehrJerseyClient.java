package org.highmed.openehr.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.highmed.openehr.deserializer.RowElementDeserializer;
import org.highmed.openehr.model.datatypes.DV_Other;
import org.highmed.openehr.model.structur.ResultSet;
import org.highmed.openehr.model.structur.RowElement;

public class TestOpenehrJerseyClient
{

	public static void main(String... args) {
		ObjectMapper objectMapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addDeserializer(RowElement.class, new RowElementDeserializer());
		objectMapper.registerModule(module);

		OpenehrWebserviceClient client = new OpenehrWebserviceClientJersey("http://161.42.236.79:31063/rest/openehr/v1", "username", "password",
				2500, 5000,  objectMapper);

		String query = "select e from ehr e";
		ResultSet resultSet = client.query(query, null);
		//DV_Count count = (DV_Count) resultSet.getRow(0).get(0);
		DV_Other count = (DV_Other) resultSet.getRow(0).get(0);
		System.out.println(count.getValue());
	}
}
