package org.highmed.openehr.model.structur;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Request
{
	private String q;
	private String offset;
	private String fetch;

	private Map<String, Object> queryParameters;

	public Request(String q) {
		this.q = q;
		this.queryParameters = new HashMap<>();
	}

	public String getQ()
	{
		return q;
	}

	public void setQ(String q)
	{
		this.q = q;
	}

	public String getOffset()
	{
		return offset;
	}

	public void setOffset(String offset)
	{
		this.offset = offset;
	}

	public String getFetch()
	{
		return fetch;
	}

	public void setFetch(String fetch)
	{
		this.fetch = fetch;
	}

	public Request addParameter(String key, Object value)
	{
		queryParameters.put(key, value);
		return this;
	}

	public Map<String, Object> getQueryParameters()
	{
		return queryParameters;
	}
}
