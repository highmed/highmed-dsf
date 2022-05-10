package org.highmed.consent.client.stub;

import static org.highmed.consent.client.ConsentClient.EHRID_COLUMN_DEFAULT_NAME;
import static org.highmed.consent.client.ConsentClient.EHRID_COLUMN_DEFAULT_PATH;

import java.util.function.BiFunction;

import org.highmed.consent.client.ConsentClient;
import org.highmed.consent.client.ConsentClientFactory;

public class ConsentClientStubFactory implements ConsentClientFactory
{
	@Override
	public ConsentClient createClient(BiFunction<String, String, String> propertyResolver)
	{
		String ehrIdColumnName = propertyResolver.apply("org.highmed.dsf.bpe.openehr.subject.external.id.name",
				EHRID_COLUMN_DEFAULT_NAME);
		String ehrIdColumnPath = propertyResolver.apply("org.highmed.dsf.bpe.openehr.subject.external.id.path",
				EHRID_COLUMN_DEFAULT_PATH);

		return new ConsentClientStub(ehrIdColumnName, ehrIdColumnPath);
	}
}
