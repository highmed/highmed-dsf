package org.highmed.fhir.dao.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

public class SearchQueryFactory
{
	public static class SearchQueryFactoryBuilder
	{
		private final String resourceTable;
		private final String resourceIdColumn;
		private final String resourceColumn;
		private final int page;
		private final int count;

		private final List<SearchParameter> searchQueries = new ArrayList<SearchParameter>();

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

		public SearchQueryFactoryBuilder with(SearchParameter searchQuery)
		{
			this.searchQueries.add(searchQuery);
			return this;
		}

		public SearchQueryFactoryBuilder with(SearchParameter... searchQueries)
		{
			this.searchQueries.addAll(Arrays.asList(searchQueries));
			return this;
		}

		public SearchQueryFactory build()
		{
			return new SearchQueryFactory(resourceTable, resourceIdColumn, resourceColumn, page, count, searchQueries);
		}
	}

	private final String searchQueryMain;
	private final String countQueryMain;
	private final List<SearchParameter> searchParameters = new ArrayList<SearchParameter>();
	private final PageAndCount pageAndCount;

	SearchQueryFactory(String resourceTable, String resourceIdColumn, String resourceColumn, int page, int count,
			List<? extends SearchParameter> searchParameters)
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

	public String createCountSql()
	{
		String filter = searchParameters.stream().filter(SearchParameter::isDefined).map(SearchParameter::getSubquery)
				.collect(Collectors.joining(" AND "));

		return countQueryMain + (!filter.isEmpty() ? (" WHERE " + filter) : "");
	}

	public String createSearchSql()
	{
		String filter = searchParameters.stream().filter(SearchParameter::isDefined).map(SearchParameter::getSubquery)
				.collect(Collectors.joining(" AND "));

		return searchQueryMain + (!filter.isEmpty() ? (" WHERE " + filter) : "") + pageAndCount.sql();
	}

	public void modifyStatement(PreparedStatement statement) throws SQLException
	{
		List<SearchParameter> filtered = searchParameters.stream().filter(SearchParameter::isDefined)
				.collect(Collectors.toList());

		int index = 0;
		for (SearchParameter q : filtered)
			for (int i = 0; i < q.getSqlParameterCount(); i++)
				q.modifyStatement(++index, i, statement);
	}

	public PageAndCount getPageAndCount()
	{
		return pageAndCount;
	}

	public boolean isCountOnly(int overallCount)
	{
		return pageAndCount.getPage() < 1 || pageAndCount.getCount() < 1 || pageAndCount.getPageStart() > overallCount;
	}

	public void configureParameters(MultivaluedMap<String, String> queryParameters)
	{
		searchParameters.forEach(p -> p.configure(queryParameters));
	}

	public void configureBundleUri(UriBuilder bundleUri)
	{
		searchParameters.forEach(p -> p.modifyBundleUri(bundleUri));
	}

	public void reset()
	{
		searchParameters.forEach(SearchParameter::reset);
	}
}
