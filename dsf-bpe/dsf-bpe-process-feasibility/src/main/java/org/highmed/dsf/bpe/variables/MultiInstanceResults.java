package org.highmed.dsf.bpe.variables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// TODO: check if Serializable can be replaced by JSON serialization
public class MultiInstanceResults implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final List<MultiInstanceResult> results;

	public MultiInstanceResults()
	{
		results = new ArrayList<>();
	}

	public void add(MultiInstanceResult newResult)
	{
		results.add(newResult);
	}

	public List<MultiInstanceResult> getResults()
	{
		return results;
	}
}
