package org.highmed.openehr.client.stub;

import java.util.function.BiFunction;

import org.highmed.openehr.client.OpenEhrClient;
import org.highmed.openehr.client.OpenEhrClientFactory;
import org.highmed.openehr.json.OpenEhrObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenEhrClientStubFactory implements OpenEhrClientFactory
{
	@Override
	public OpenEhrClient createClient(BiFunction<String, String, String> propertyResolver)
	{
		ObjectMapper openEhrObjectMapper = OpenEhrObjectMapperFactory.createObjectMapper();
		return new OpenEhrClientStub(openEhrObjectMapper);
	}
}
