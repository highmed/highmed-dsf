package org.highmed.openehr.client.stub;

import java.util.function.Function;

import org.highmed.openehr.client.OpenEhrClient;
import org.highmed.openehr.client.OpenEhrClientFactory;
import org.highmed.openehr.json.OpenEhrObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenEhrClientStubFactory implements OpenEhrClientFactory
{
	@Override
	public OpenEhrClient createClient(Function<String, String> propertyResolver)
	{
		ObjectMapper openEhrObjectMapper = OpenEhrObjectMapperFactory.createObjectMapper();
		return new OpenEhrClientStub(openEhrObjectMapper);
	}
}
