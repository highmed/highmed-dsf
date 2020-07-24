package org.highmed.dsf.bpe.variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FeasibilityQueryResults
{
	private final List<FeasibilityQueryResult> results = new ArrayList<>();

	@JsonCreator
	public FeasibilityQueryResults(@JsonProperty("results") Collection<? extends FeasibilityQueryResult> results)
	{
		if (results != null)
			this.results.addAll(results);
	}

	public void add(FeasibilityQueryResult newResult)
	{
		if (newResult != null)
			results.add(newResult);
	}

	public void addAll(Collection<FeasibilityQueryResult> results)
	{
		if (results != null)
			this.results.addAll(results);
	}

	public List<FeasibilityQueryResult> getResults()
	{
		return Collections.unmodifiableList(results);
	}
}
