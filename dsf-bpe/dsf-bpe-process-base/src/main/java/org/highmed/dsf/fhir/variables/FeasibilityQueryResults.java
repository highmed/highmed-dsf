package org.highmed.dsf.fhir.variables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// TODO: check if Serializable can be replaced by JSON serialization
public class FeasibilityQueryResults implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final List<FeasibilityQueryResult> results;

	public FeasibilityQueryResults()
	{
		results = new ArrayList<>();
	}

	public FeasibilityQueryResults(Collection<? extends FeasibilityQueryResult> results)
	{
		this();
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
