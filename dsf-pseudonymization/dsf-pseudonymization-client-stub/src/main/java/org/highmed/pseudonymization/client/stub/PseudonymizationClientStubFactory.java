package org.highmed.pseudonymization.client.stub;

import java.util.function.BiFunction;

import org.highmed.pseudonymization.client.PseudonymizationClient;
import org.highmed.pseudonymization.client.PseudonymizationClientFactory;

public class PseudonymizationClientStubFactory implements PseudonymizationClientFactory
{
	@Override
	public PseudonymizationClient createClient(BiFunction<String, String, String> propertyResolver)
	{
		return new PseudonymizationClientStub();
	}
}
