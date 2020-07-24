package org.highmed.dsf.bpe.variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FinalFeasibilityQueryResults
{
	private final List<FinalFeasibilityQueryResult> results = new ArrayList<>();

	@JsonCreator
	public FinalFeasibilityQueryResults(@JsonProperty("results") Collection<? extends FinalFeasibilityQueryResult> results)
	{
		if (results != null)
			this.results.addAll(results);
	}

	public void add(FinalFeasibilityQueryResult newResult)
	{
		if (newResult != null)
			results.add(newResult);
	}

	public void addAll(Collection<FinalFeasibilityQueryResult> results)
	{
		if (results != null)
			this.results.addAll(results);
	}

	public List<FinalFeasibilityQueryResult> getResults()
	{
		return Collections.unmodifiableList(results);
	}
}
