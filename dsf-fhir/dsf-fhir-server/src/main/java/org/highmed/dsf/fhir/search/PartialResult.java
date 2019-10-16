package org.highmed.dsf.fhir.search;

import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.Resource;

public class PartialResult<R extends Resource>
{
	private final int overallCount;
	private final PageAndCount pageAndCount;
	private final List<R> partialResult;
	private final List<Resource> includes;
	private final boolean countOnly;

	public PartialResult(int overallCount, PageAndCount pageAndCount, List<R> partialResult, List<Resource> includes,
			boolean countOnly)
	{
		this.overallCount = overallCount;
		this.pageAndCount = pageAndCount;
		this.partialResult = partialResult;
		this.includes = includes;
		this.countOnly = countOnly;
	}

	public int getOverallCount()
	{
		return overallCount;
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

	public boolean isLastPage()
	{
		return pageAndCount.getPage() >= getLastPage();
	}

	public int getLastPage()
	{
		return (int) Math.ceil((double) overallCount / pageAndCount.getCount());
	}

	public boolean isCountOnly()
	{
		return countOnly;
	}
}
