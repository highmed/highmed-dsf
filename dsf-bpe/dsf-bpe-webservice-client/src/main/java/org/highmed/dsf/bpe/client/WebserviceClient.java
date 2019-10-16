package org.highmed.dsf.bpe.client;

import java.util.List;
import java.util.Map;

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
	 * @param parameters
	 *            may be <code>null</code>
	 * @throws WebApplicationException
	 *             if return status != 201
	 */
	void startProcessLatestVersion(String processDefinitionKey, Map<String, List<String>> parameters)
			throws WebApplicationException;

	/**
	 * @param processDefinitionKey
	 *            not <code>null</code>
	 * @param versionTag
	 *            not <code>null</code>
	 * @throws WebApplicationException
	 *             if return status != 201
	 */
	void startProcessWithVersion(String processDefinitionKey, String versionTag) throws WebApplicationException;

	/**
	 * @param processDefinitionKey
	 *            not <code>null</code>
	 * @param versionTag
	 *            not <code>null</code>
	 * @param parameters
	 *            may be <code>null</code>
	 * @throws WebApplicationException
	 *             if return status != 201
	 */
	void startProcessWithVersion(String processDefinitionKey, String versionTag, Map<String, List<String>> parameters)
			throws WebApplicationException;
}
