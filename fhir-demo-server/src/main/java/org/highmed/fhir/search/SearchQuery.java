package org.highmed.fhir.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.highmed.fhir.dao.search.DbSearchQuery;

public class SearchQuery implements DbSearchQuery
{
	public static class SearchQueryBuilder
	{
		private final String resourceTable;
		private final String resourceIdColumn;
		private final String resourceColumn;

		public SearchQueryBuilder(String resourceTable, String resourceIdColumn, String resourceColumn)
		{
			this.resourceTable = resourceTable;
			this.resourceIdColumn = resourceIdColumn;
			this.resourceColumn = resourceColumn;
		}

		public static SearchQueryBuilder create(String resourceTable, String resourceIdColumn, String resourceColumn)
		{
			return new SearchQueryBuilder(resourceTable, resourceIdColumn, resourceColumn);
		}

		public SearchQueryBuilderForDb with(int page, int count)
		{
			return new SearchQueryBuilderForDb(resourceTable, resourceIdColumn, resourceColumn, page, count);
		}
	}

	public static class SearchQueryBuilderForDb
	{
		private final String resourceTable;
		private final String resourceIdColumn;
		private final String resourceColumn;
		private final int page;
		private final int count;

		private final List<SearchParameter<?>> searchQueries = new ArrayList<SearchParameter<?>>();

		public SearchQueryBuilderForDb(String resourceTable, String resourceIdColumn, String resourceColumn, int page,
				int count)
		{
			this.resourceTable = resourceTable;
			this.resourceIdColumn = resourceIdColumn;
			this.resourceColumn = resourceColumn;
			this.page = page;
			this.count = count;
		}

		public SearchQueryBuilderForDb with(SearchParameter<?> searchQuery)
		{
			this.searchQueries.add(searchQuery);
			return this;
		}

		public SearchQueryBuilderForDb with(SearchParameter<?>... searchQueries)
		{
			this.searchQueries.addAll(Arrays.asList(searchQueries));
			return this;
		}

		public SearchQuery build()
		{
			return new SearchQuery(resourceTable, resourceIdColumn, resourceColumn, page, count, searchQueries);
		}
	}

	private final String searchQueryMain;
	private final String countQueryMain;
	private final List<SearchParameter<?>> searchParameters = new ArrayList<SearchParameter<?>>();
	private final PageAndCount pageAndCount;

	private String filterQuery = "";

	SearchQuery(String resourceTable, String resourceIdColumn, String resourceColumn, int page, int count,
			List<? extends SearchParameter<?>> searchParameters)
	{
		this.searchQueryMain = "SELECT " + resourceColumn + " FROM (SELECT DISTINCT ON (" + resourceIdColumn + ") "
				+ resourceColumn + " FROM " + resourceTable + " WHERE NOT deleted ORDER BY " + resourceIdColumn
				+ ", version DESC) AS current_" + resourceTable;

		this.countQueryMain = "SELECT count(*) FROM (SELECT DISTINCT ON (" + resourceIdColumn + ") " + resourceColumn
				+ " FROM " + resourceTable + " WHERE NOT deleted ORDER BY " + resourceIdColumn
				+ ", version DESC) AS current_" + resourceTable;

		this.searchParameters.addAll(searchParameters);
		this.pageAndCount = new PageAndCount(page, count);
	}

	public void configureParameters(MultivaluedMap<String, String> queryParameters)
	{
		searchParameters.forEach(p -> p.configure(queryParameters));

		filterQuery = searchParameters.stream().filter(SearchParameter::isDefined).map(SearchParameter::getFilterQuery)
				.collect(Collectors.joining(" AND "));
	}

	@Override
	public String getCountSql()
	{
		return countQueryMain + (!filterQuery.isEmpty() ? (" WHERE " + filterQuery) : "");
	}

	@Override
	public String getSearchSql()
	{
		return searchQueryMain + (!filterQuery.isEmpty() ? (" WHERE " + filterQuery) : "") + pageAndCount.sql();
	}

	@Override
	public void modifyStatement(PreparedStatement statement) throws SQLException
	{
		List<SearchParameter<?>> filtered = searchParameters.stream().filter(SearchParameter::isDefined)
				.collect(Collectors.toList());

		int index = 0;
		for (SearchParameter<?> q : filtered)
			for (int i = 0; i < q.getSqlParameterCount(); i++)
				q.modifyStatement(++index, i + 1, statement);
	}

	@Override
	public PageAndCount getPageAndCount()
	{
		return pageAndCount;
	}

	@Override
	public boolean isCountOnly(int overallCount)
	{
		return pageAndCount.getPage() < 1 || pageAndCount.getCount() < 1 || pageAndCount.getPageStart() > overallCount;
	}

	public void configureBundleUri(UriBuilder bundleUri)
	{
		searchParameters.stream().filter(SearchParameter::isDefined).forEach(p -> p.modifyBundleUri(bundleUri));
	}
}
