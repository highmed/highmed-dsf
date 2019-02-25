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

public class SearchQuery<R extends DomainResource> implements DbSearchQuery, Matcher
{
	public static class SearchQueryBuilder<R extends DomainResource>
	{
		public static <R extends DomainResource> SearchQueryBuilder<R> create(Class<R> resourceType,
				String resourceTable, String resourceIdColumn, String resourceColumn, int page, int count)
		{
			return new SearchQueryBuilder<R>(resourceType, resourceTable, resourceIdColumn, resourceColumn, page,
					count);
		}

		private final Class<R> resourceType;
		private final String resourceTable;
		private final String resourceIdColumn;
		private final String resourceColumn;
		private final int page;
		private final int count;

		private final List<SearchQueryParameter<R>> searchParameters = new ArrayList<SearchQueryParameter<R>>();
		private String sortParameters;

		private SearchQueryBuilder(Class<R> resourceType, String resourceTable, String resourceIdColumn,
				String resourceColumn, int page, int count)
		{
			this.resourceType = resourceType;
			this.resourceTable = resourceTable;
			this.resourceIdColumn = resourceIdColumn;
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

		public SearchQueryBuilder<R> sort(String sortParameters)
		{
			this.sortParameters = sortParameters;
			return this;
		}

		public SearchQuery<R> build()
		{
			return new SearchQuery<R>(resourceType, resourceTable, resourceIdColumn, resourceColumn, page, count,
					sortParameters, searchParameters);
		}
	}

	private final Class<R> resourceType;
	private final String searchQueryMain;
	private final String countQueryMain;
	private final List<SearchQueryParameter<R>> searchParameters = new ArrayList<>();
	private final PageAndCount pageAndCount;

	private String filterQuery = "";
	private String sortSql = "";
	private List<SearchQueryParameter<R>> sortParameters = Collections.emptyList();

	SearchQuery(Class<R> resourceType, String resourceTable, String resourceIdColumn, String resourceColumn, int page,
			int count, List<? extends SearchQueryParameter<R>> searchParameters)
	{
		this(resourceType, resourceTable, resourceIdColumn, resourceColumn, page, count, null, searchParameters);
	}

	SearchQuery(Class<R> resourceType, String resourceTable, String resourceIdColumn, String resourceColumn, int page,
			int count, String sortParameters, List<? extends SearchQueryParameter<R>> searchParameters)
	{
		this.resourceType = resourceType;
		this.searchQueryMain = "SELECT " + resourceColumn + " FROM (SELECT DISTINCT ON (" + resourceIdColumn + ") "
				+ resourceColumn + " FROM " + resourceTable + " WHERE NOT deleted ORDER BY " + resourceIdColumn
				+ ", version DESC) AS current_" + resourceTable;

		this.countQueryMain = "SELECT count(*) FROM (SELECT DISTINCT ON (" + resourceIdColumn + ") " + resourceColumn
				+ " FROM " + resourceTable + " WHERE NOT deleted ORDER BY " + resourceIdColumn
				+ ", version DESC) AS current_" + resourceTable;

		this.searchParameters.addAll(searchParameters);
		this.pageAndCount = new PageAndCount(page, count);

		if (sortParameters != null)
			createSortSql(sortParameters);
	}

	public void configureParameters(Map<String, List<String>> queryParameters)
	{
		searchParameters.forEach(p -> p.configure(queryParameters));

		filterQuery = searchParameters.stream().filter(SearchQueryParameter::isDefined).map(SearchQueryParameter::getFilterQuery)
				.collect(Collectors.joining(" AND "));

		createSortSql(getFirst(queryParameters, AbstractSearchParameter.SORT_PARAMETER));
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

		sortParameters = Arrays.stream(sortParameterValue.split("[,]+")).map(this::getSearchParameter)
				.filter(sp -> sp != null).collect(Collectors.toList());

		if (sortParameters.isEmpty())
			return;

		sortSql = sortParameters.stream().map(sp -> sp.getSortParameter().getSql())
				.collect(Collectors.joining(", ", " ORDER BY ", ""));
	}

	private SearchQueryParameter<R> getSearchParameter(String sort)
	{
		for (SearchQueryParameter<R> p : searchParameters)
		{
			if (p.getParameterName().equals(sort) || ("+" + p.getParameterName()).equals(sort)
					|| ("-" + p.getParameterName()).equals(sort))
				return p;
		}

		return null;
	}

	@Override
	public String getCountSql()
	{
		return countQueryMain + (!filterQuery.isEmpty() ? (" WHERE " + filterQuery) : "");
	}

	@Override
	public String getSearchSql()
	{
		return searchQueryMain + (!filterQuery.isEmpty() ? (" WHERE " + filterQuery) : "") + sortSql
				+ pageAndCount.sql();
	}

	@Override
	public void modifyStatement(PreparedStatement statement) throws SQLException
	{
		List<SearchQueryParameter<?>> filtered = searchParameters.stream().filter(SearchQueryParameter::isDefined)
				.collect(Collectors.toList());

		int index = 0;
		for (SearchQueryParameter<?> q : filtered)
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

	public UriBuilder configureBundleUri(UriBuilder bundleUri)
	{
		Objects.requireNonNull(bundleUri, "bundleUri");

		searchParameters.stream().filter(SearchQueryParameter::isDefined).forEach(p -> p.modifyBundleUri(bundleUri));

		if (!sortParameters.isEmpty())
			bundleUri.replaceQueryParam(AbstractSearchParameter.SORT_PARAMETER, sortParameter());

		return bundleUri;
	}

	private String sortParameter()
	{
		return sortParameters.stream()
				.map(p -> p.getSortParameter().getDirection().getUrlModifier() + p.getParameterName())
				.collect(Collectors.joining(","));
	}

	public Class<R> getResourceType()
	{
		return resourceType;
	}

	@Override
	public boolean matches(DomainResource resource)
	{
		if (!getResourceType().isInstance(resource))
			return false;

		return searchParameters.stream().filter(SearchQueryParameter::isDefined).map(p -> p.matches(resource))
				.allMatch(b -> b);
	}
}
