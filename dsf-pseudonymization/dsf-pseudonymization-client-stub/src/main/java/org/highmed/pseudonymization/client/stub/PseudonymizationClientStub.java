package org.highmed.pseudonymization.client.stub;

import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.client.PseudonymizationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PseudonymizationClientStub implements PseudonymizationClient
{
	private static final Logger logger = LoggerFactory.getLogger(PseudonymizationClientStub.class);

	@Override
	public ResultSet pseudonymize(ResultSet resultSet)
	{
		logger.warn("No pseudonymization applied, ResultSet will be returned as provided");

		return resultSet;
	}
}
