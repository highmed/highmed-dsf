package org.highmed.bpe.client;

import javax.ws.rs.WebApplicationException;

public interface WebserviceClient
{
	/**
	 * @param processDefinitionKey
	 *            not <code>null</code>
	 * @throws WebApplicationException
	 *             if return status != 201
	 */
	void startProcessLatestVersion(String processDefinitionKey) throws WebApplicationException;

	/**
	 * @param processDefinitionKey
	 *            not <code>null</code>
	 * @param versionTag
	 *            not <code>null</code>
	 * @throws WebApplicationException
	 *             if return status != 201
	 */
	void startProcessWithVersion(String processDefinitionKey, String versionTag) throws WebApplicationException;
}
