package org.highmed.fhir.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import org.highmed.fhir.search.parameters.basic.AbstractSearchParameter;
import org.hl7.fhir.r4.model.DomainResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchQuery<R extends DomainResource> implements DbSearchQuery, Matcher
{
	public static class SearchQueryBuilder<R extends DomainResource>
	{
		public static <R extends DomainResource> SearchQueryBuilder<R> create(Class<R> resourceType,
				String resourceTable, String resourceColumn, int page, int count)
		{
			return new SearchQueryBuilder<R>(resourceType, resourceTable, resourceColumn, page, count);
		}

		private final Class<R> resourceType;
		private final String resourceTable;
		private final String resourceColumn;
		private final int page;
		private final int count;

		private final List<SearchQueryParameter<R>> searchParameters = new ArrayList<SearchQueryParameter<R>>();

		private SearchQueryBuilder(Class<R> resourceType, String resourceTable, String resourceColumn, int page,
				int count)
		{
			this.resourceType = resourceType;
			this.resourceTable = resourceTable;
			this.resourceColumn = resourceColumn;
			this.page = page;
			this.count = count;
		}

		public SearchQueryBuilder<R> with(SearchQueryParameter<R> searchParameters)
		{
			this.searchParameters.add(searchParameters);
			return this;
		}

		public SearchQueryBuilder<R> with(@SuppressWarnings("unchecked") SearchQueryParameter<R>... searchParameters)
		{
			return with(Arrays.asList(searchParameters));
		}

		public SearchQueryBuilder<R> with(List<SearchQueryParameter<R>> searchParameters)
		{
			this.searchParameters.addAll(searchParameters);
			return this;
		}

		public SearchQuery<R> build()
		{
			return new SearchQuery<R>(resourceType, resourceTable, resourceColumn, page, count, searchParameters);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(SearchQuery.class);

	private final Class<R> resourceType;
	private final String resourceColumn;
	private final String resourceTable;
	private final List<SearchQueryParameter<R>> searchParameters = new ArrayList<>();
	private final PageAndCount pageAndCount;

	private String filterQuery = "";
	private String sortSql = "";
	private String includeSql = "";
	private List<SearchQueryParameter<R>> sortParameters = Collections.emptyList();
	private List<SearchQueryParameter<R>> includeParameters = Collections.emptyList();

	SearchQuery(Class<R> resourceType, String resourceTable, String resourceColumn, int page, int count,
			List<? extends SearchQueryParameter<R>> searchParameters)
	{
		this.resourceType = resourceType;
		this.resourceTable = resourceTable;
		this.resourceColumn = resourceColumn;

		this.searchParameters.addAll(searchParameters);
		this.pageAndCount = new PageAndCount(page, count);
	}

	public void configureParameters(Map<String, List<String>> queryParameters)
	{
		searchParameters.forEach(p -> p.configure(queryParameters));

		filterQuery = searchParameters.stream().filter(SearchQueryParameter::isDefined)
				.map(SearchQueryParameter::getFilterQuery).collect(Collectors.joining(" AND "));

		createSortSql(getFirst(queryParameters, AbstractSearchParameter.SORT_PARAMETER));
		createIncludeSql(queryParameters.get(AbstractSearchParameter.INCLUDE_PARAMETER));
	}

	private String getFirst(Map<String, List<String>> queryParameters, String key)
	{
		if (queryParameters.containsKey(key) && !queryParameters.get(key).isEmpty())
			return queryParameters.get(key).get(0);
		else
			return null;
	}

	private void createSortSql(String sortParameterValue)
	{
		if (sortParameterValue == null)
			return;

		sortParameters = searchParameters.stream().filter(sp -> sp.getSortParameter().isPresent())
				.collect(Collectors.toList());

		if (sortParameters.isEmpty())
			return;

		sortSql = sortParameters.stream().map(sp -> sp.getSortParameter().get().getSql())
				.collect(Collectors.joining(", ", " ORDER BY ", ""));
	}

	private void createIncludeSql(List<String> includeParameterValues)
	{
		if (includeParameterValues == null || includeParameterValues.isEmpty())
			return;

		includeParameters = searchParameters.stream().filter(sp -> sp.getIncludeParameter().isPresent())
				.collect(Collectors.toList());

		if (includeParameters.isEmpty())
			return;

		includeSql = includeParameters.stream().map(sp -> sp.getIncludeParameter().get().getSql())
				.collect(Collectors.joining(", ", ", ", ""));
	}

	@Override
	public String getCountSql()
	{
		String countQueryMain = "SELECT count(*) FROM current_" + resourceTable;

		return countQueryMain + (!filterQuery.isEmpty() ? (" WHERE " + filterQuery) : "");
	}

	@Override
	public String getSearchSql()
	{
		String searchQueryMain = "SELECT " + resourceColumn + includeSql + " FROM current_" + resourceTable;

		return searchQueryMain + (!filterQuery.isEmpty() ? (" WHERE " + filterQuery) : "") + sortSql
				+ pageAndCount.sql();
	}

	@Override
	public void modifyStatement(PreparedStatement statement) throws SQLException
	{
		try
		{
			List<SearchQueryParameter<?>> filtered = searchParameters.stream().filter(SearchQueryParameter::isDefined)
					.collect(Collectors.toList());

			int index = 0;
			for (SearchQueryParameter<?> q : filtered)
				for (int i = 0; i < q.getSqlParameterCount(); i++)
					q.modifyStatement(++index, i + 1, statement);
		}
		catch (SQLException e)
		{
			logger.warn("Error while modifying prepared statement '{}': {}", statement.toString(), e.getMessage());
			throw e;
		}
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

	public UriBuilder configureBundleUri(UriBuilder bundleUri)
	{
		Objects.requireNonNull(bundleUri, "bundleUri");

		searchParameters.stream().filter(SearchQueryParameter::isDefined).forEach(p -> p.modifyBundleUri(bundleUri));

		if (!sortParameters.isEmpty())
			bundleUri.replaceQueryParam(AbstractSearchParameter.SORT_PARAMETER, sortParameter());
		if (!includeParameters.isEmpty())
			bundleUri.replaceQueryParam(AbstractSearchParameter.INCLUDE_PARAMETER, includeParameters());

		return bundleUri;
	}

	private String sortParameter()
	{
		return sortParameters.stream().map(p -> p.getSortParameter().get().getBundleUriQueryParameterValuePart())
				.collect(Collectors.joining(","));
	}

	private Object[] includeParameters()
	{
		return includeParameters.stream().flatMap(p -> p.getIncludeParameter().get().getBundleUriQueryParameterValues())
				.toArray();
	}

	public Class<R> getResourceType()
	{
		return resourceType;
	}

	@Override
	public boolean matches(DomainResource resource)
	{
		if (resource == null)
			return false;

		if (!getResourceType().isInstance(resource))
			return false;

		return searchParameters.stream().filter(SearchQueryParameter::isDefined).map(p -> p.matches(resource))
				.allMatch(b -> b);
	}

	@Override
	public Class<? extends DomainResource> getIncludeResourceTypForColumName(String columnName)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
