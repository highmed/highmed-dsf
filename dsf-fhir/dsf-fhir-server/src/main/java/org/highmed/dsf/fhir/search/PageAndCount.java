package org.highmed.dsf.fhir.search;

public class PageAndCount
{
	private final int page;
	private final int count;

	public PageAndCount(int page, int count)
	{
		this.page = page;
		this.count = count;
	}

	public String getSql()
	{
		return " LIMIT " + count + (page > 1 ? (" OFFSET " + ((page - 1) * count)) : "");
	}

	public boolean isCountOnly(int total)
	{
		return page < 1 || count < 1 || getPageStart() > total;
	}

	public int getPage()
	{
		return page;
	}

	public int getCount()
	{
		return count;
	}

	public int getPageStart()
	{
		if (page < 1 || count < 1)
			return 0;

		return Math.min(1, page - 1) * count + 1;
	}

	public int getPageEnd()
	{
		if (page < 1 || count < 1)
			return 0;

		return getPageStart() - 1 + count;
	}

	public boolean isLastPage(int total)
	{
		return page >= getLastPage(total);
	}

	public int getLastPage(int total)
	{
		return (int) Math.ceil((double) total / count);
	}
}