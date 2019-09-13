package org.highmed.openehr.model.structur;

import java.util.HashMap;
import java.util.Map;

public class Request
{
	private String q;
	private int offset;
	private int fetch;

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

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public int getFetch()
	{
		return fetch;
	}

	public void setFetch(int fetch)
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
