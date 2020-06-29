package org.highmed.dsf.fhir.search;

import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.Resource;

public class PartialResult<R extends Resource>
{
	private final int total;
	private final PageAndCount pageAndCount;
	private final List<R> partialResult;
	private final List<Resource> includes;

	public PartialResult(int total, PageAndCount pageAndCount, List<R> partialResult, List<Resource> includes)
	{
		this.total = total;
		this.pageAndCount = pageAndCount;
		this.partialResult = partialResult;
		this.includes = includes;
	}

	public int getTotal()
	{
		return total;
	}

	public PageAndCount getPageAndCount()
	{
		return pageAndCount;
	}

	public List<R> getPartialResult()
	{
		return Collections.unmodifiableList(partialResult);
	}

	public List<Resource> getIncludes()
	{
		return Collections.unmodifiableList(includes);
	}
}
