package org.highmed.consent.client.stub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.highmed.consent.client.ConsentClient;
import org.highmed.openehr.json.OpenEhrObjectMapperFactory;
import org.highmed.openehr.model.structure.ResultSet;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ConsentClientStubTest
{
	@Test
	public void testConsentClientStub() throws Exception
	{
		ObjectMapper openEhrObjectMapper = OpenEhrObjectMapperFactory.createObjectMapper();
		ResultSet resultSet = openEhrObjectMapper
				.readValue(Files.readAllBytes(Paths.get("src/test/resources/result.json")), ResultSet.class);

		ConsentClient consentClient = new ConsentClientStubFactory()
				.createClient((String key, String defaultValue) -> defaultValue);

		assertNotNull(consentClient);

		int initialColumnsSize = resultSet.getColumns().size();
		int initialRowsSize = resultSet.getRows().size();

		ResultSet filteredResultSet = consentClient.removeRowsWithoutConsent(resultSet);

		assertNotNull(filteredResultSet);
		assertEquals(initialColumnsSize, filteredResultSet.getColumns().size());
		assertEquals(initialRowsSize, filteredResultSet.getRows().size());
	}
}
