package org.highmed.dsf.bpe.variables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MultiInstanceResults implements Serializable
{
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
