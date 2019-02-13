package org.highmed.fhir.dao.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SearchQueryFactory
{
	public static class SearchQueryFactoryBuilder
	{
		private final String resourceTable;
		private final String resourceIdColumn;
		private final String resourceColumn;
		private final int page;
		private final int count;

		private final List<SearchQuery> searchQueries = new ArrayList<SearchQuery>();

		public SearchQueryFactoryBuilder(String resourceTable, String resourceIdColumn, String resourceColumn, int page,
				int count)
		{
			this.resourceTable = resourceTable;
			this.resourceIdColumn = resourceIdColumn;
			this.resourceColumn = resourceColumn;
			this.page = page;
			this.count = count;
		}

		public static SearchQueryFactoryBuilder create(String resourceTable, String resourceIdColumn,
				String resourceColumn, int page, int count)
		{
			return new SearchQueryFactoryBuilder(resourceTable, resourceIdColumn, resourceColumn, page, count);
		}

		public SearchQueryFactoryBuilder with(SearchQuery e)
		{
			searchQueries.add(e);
			return this;
		}

		public SearchQueryFactoryBuilder with(SearchQuery... e)
		{
			searchQueries.addAll(Arrays.asList(e));
			return this;
		}

		public SearchQueryFactory build()
		{
			return new SearchQueryFactory(resourceTable, resourceIdColumn, resourceColumn, page, count, searchQueries);
		}
	}

	private final String searchQueryMain;
	private final String countQueryMain;
	private final List<SearchQuery> searchQueries = new ArrayList<SearchQuery>();
	private final PageAndCount pageAndCount;

	SearchQueryFactory(String resourceTable, String resourceIdColumn, String resourceColumn, int page, int count,
			List<? extends SearchQuery> searchQueries)
	{
		this.searchQueryMain = "SELECT " + resourceColumn + " FROM (SELECT DISTINCT ON (" + resourceIdColumn + ") "
				+ resourceColumn + " FROM " + resourceTable + " WHERE NOT deleted ORDER BY " + resourceIdColumn
				+ ", version DESC) AS current_" + resourceTable;

		this.countQueryMain = "SELECT count(*) FROM (SELECT DISTINCT ON (" + resourceIdColumn + ") " + resourceColumn
				+ " FROM " + resourceTable + " WHERE NOT deleted ORDER BY " + resourceIdColumn
				+ ", version DESC) AS current_" + resourceTable;

		this.searchQueries.addAll(searchQueries);
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
