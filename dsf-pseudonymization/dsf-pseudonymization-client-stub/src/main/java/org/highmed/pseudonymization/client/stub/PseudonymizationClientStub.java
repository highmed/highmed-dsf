package org.highmed.pseudonymization.client.stub;

import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.client.PseudonymizationClient;

public class PseudonymizationClientStub implements PseudonymizationClient
{
	@Override
	public ResultSet pseudonymize(ResultSet resultSet)
	{
		return resultSet;
	}
}
