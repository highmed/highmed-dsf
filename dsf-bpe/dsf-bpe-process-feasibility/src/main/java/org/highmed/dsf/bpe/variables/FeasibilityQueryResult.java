package org.highmed.dsf.bpe.variables;

import org.highmed.openehr.model.structure.ResultSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FeasibilityQueryResult
{
	private final String organizationIdentifier;
	private final String cohortId;
	private final int cohortSize;
	private final ResultSet resultSet;
	private final String resultSetUrl;

	public static FeasibilityQueryResult countResult(String organizationIdentifier, String cohortId, int cohortSize)
	{
		if (cohortSize < 0)
			throw new IllegalArgumentException("cohortSize >= 0 expected");

		return new FeasibilityQueryResult(organizationIdentifier, cohortId, cohortSize, null, null);
	}

	public static FeasibilityQueryResult idResult(String organizationIdentifier, String cohortId, ResultSet resultSet)
	{
		return new FeasibilityQueryResult(organizationIdentifier, cohortId, -1, resultSet, null);
	}

	public static FeasibilityQueryResult idResult(String organizationIdentifier, String cohortId, String resultSetUrl)
	{
		return new FeasibilityQueryResult(organizationIdentifier, cohortId, -1, null, resultSetUrl);
	}

	@JsonCreator
	public FeasibilityQueryResult(@JsonProperty("organizationIdentifier") String organizationIdentifier,
			@JsonProperty("cohortId") String cohortId, @JsonProperty("cohortSize") int cohortSize,
			@JsonProperty("resultSet") ResultSet resultSet, @JsonProperty("resultSetUrl") String resultSetUrl)
	{
		this.organizationIdentifier = organizationIdentifier;
		this.cohortId = cohortId;
		this.cohortSize = cohortSize;
		this.resultSet = resultSet;
		this.resultSetUrl = resultSetUrl;
	}

	public String getOrganizationIdentifier()
	{
		return organizationIdentifier;
	}

	public String getCohortId()
	{
		return cohortId;
	}

	public int getCohortSize()
	{
		return cohortSize;
	}

	public ResultSet getResultSet()
	{
		return resultSet;
	}

	public String getResultSetUrl()
	{
		return resultSetUrl;
	}

	@JsonIgnore
	public boolean isCohortSizeResult()
	{
		return resultSet == null && resultSetUrl == null;
	}

	@JsonIgnore
	public boolean isIdResultSetResult()
	{
		return resultSet != null && resultSetUrl == null;
	}

	@JsonIgnore
	public boolean isIdResultSetUrlResult()
	{
		return resultSet == null && resultSetUrl != null;
	}
}
