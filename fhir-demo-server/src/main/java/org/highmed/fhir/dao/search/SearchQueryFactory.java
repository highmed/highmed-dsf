package org.highmed.fhir.dao.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SearchQueryFactory
{
	private final String searchQueryMain;
	private final String countQueryMain;
	private final List<SearchQuery> searchQueries;
	private final PageAndCount pageAndCount;

	public SearchQueryFactory(String resourceTable, String resourceIdColumn, String resourceColumn, int page, int count,
			SearchQuery... searchQueries)
	{
		this.searchQueryMain = "SELECT " + resourceColumn + " FROM (SELECT DISTINCT ON (" + resourceIdColumn + ") "
				+ resourceColumn + " FROM " + resourceTable + " WHERE NOT deleted ORDER BY " + resourceIdColumn
				+ ", version DESC) AS current_" + resourceTable;

		this.countQueryMain = "SELECT count(*) FROM (SELECT DISTINCT ON (" + resourceIdColumn + ") " + resourceColumn
				+ " FROM " + resourceTable + " WHERE NOT deleted ORDER BY " + resourceIdColumn
				+ ", version DESC) AS current_" + resourceTable;

		this.searchQueries = Arrays.asList(searchQueries);
		this.pageAndCount = new PageAndCount(page, count);
	}

	public String createCountSql()
	{
		String filter = searchQueries.stream().filter(SearchQuery::isDefined).map(SearchQuery::getSubquery)
				.collect(Collectors.joining(" AND "));

		return countQueryMain + (!filter.isEmpty() ? (" WHERE " + filter) : "");
	}

	public String createSearchSql()
	{
		String filter = searchQueries.stream().filter(SearchQuery::isDefined).map(SearchQuery::getSubquery)
				.collect(Collectors.joining(" AND "));

		return searchQueryMain + (!filter.isEmpty() ? (" WHERE " + filter) : "") + pageAndCount.sql();
	}

	public void modifyStatement(PreparedStatement statement) throws SQLException
	{
		List<SearchQuery> filtered = searchQueries.stream().filter(SearchQuery::isDefined).collect(Collectors.toList());

		int index = 0;
		for (SearchQuery q : filtered)
			for (int i = 0; i < q.getSqlParameterCount(); i++)
				q.modifyStatement(++index, statement);
	}

	public PageAndCount getPageAndCount()
	{
		return pageAndCount;
	}

	public boolean isCountOnly(int overallCount)
	{
		return pageAndCount.getPage() < 1 || pageAndCount.getCount() < 1 || pageAndCount.getPageStart() > overallCount;
	}
}
