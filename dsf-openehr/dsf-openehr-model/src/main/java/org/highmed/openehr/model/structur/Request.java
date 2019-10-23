package org.highmed.openehr.model.structur;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Request
{
	private final String query;

	private final String offset;
	private final String fetch;

	private final Map<String, Object> queryParameters;

	public Request(
			@JsonProperty("q")
					String query,
			@JsonProperty("offset")
					String offset,
			@JsonProperty("fetch")
					String fetch,
			@JsonProperty("query-parameters")
					Map<String, Object> queryParameters)
	{
		this.query = query;
		this.offset = offset;
		this.fetch = fetch;
		this.queryParameters = queryParameters;
	}

	public String getQuery()
	{
		return query;
	}

	public String getOffset()
	{
		return offset;
	}

	public String getFetch()
	{
		return fetch;
	}

	public Map<String, Object> getQueryParameters()
	{
		return queryParameters;
	}
}
