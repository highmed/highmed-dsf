package org.highmed.dsf.fhir.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.highmed.dsf.fhir.search.PageAndCount;

public class History
{
	private final int total;
	private final PageAndCount pageAndCount;
	private final List<HistoryEntry> entries = new ArrayList<>();

	public History(int total, PageAndCount pageAndCount, Collection<? extends HistoryEntry> entries)
	{
		this.total = total;
		this.pageAndCount = pageAndCount;
		if (entries != null)
			this.entries.addAll(entries);
	}

	public int getTotal()
	{
		return total;
	}

	public PageAndCount getPageAndCount()
	{
		return pageAndCount;
	}

	public List<HistoryEntry> getEntries()
	{
		return Collections.unmodifiableList(entries);
	}

}