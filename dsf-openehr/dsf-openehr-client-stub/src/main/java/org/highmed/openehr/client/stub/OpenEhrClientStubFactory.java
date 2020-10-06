package org.highmed.openehr.client.stub;

import java.util.function.Function;

import org.highmed.openehr.client.OpenEhrClient;
import org.highmed.openehr.client.OpenEhrClientFactory;

public class OpenEhrClientStubFactory implements OpenEhrClientFactory
{
	@Override
	public OpenEhrClient createClient(Function<String, String> propertyResolver)
	{
		return new OpenEhrClientStub();
	}
}
