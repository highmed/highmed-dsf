package org.highmed.dsf.bpe.variables;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MultiInstanceResult implements Serializable
{
	private final String organizationIdentifier;

	// key = group cohort id
	// value = cohort size
	private final Map<String, String> queryResults;

	public MultiInstanceResult(String organizationIdentifier, Map<String, String> queryResult)
	{
		this.organizationIdentifier = organizationIdentifier;
		this.queryResults = queryResult;
	}

	public String getOrganizationIdentifier()
	{
		return organizationIdentifier;
	}

	public Map<String, String> getQueryResults()
	{
		return queryResults;
	}
}
