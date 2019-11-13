package org.highmed.dsf.fhir.search;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.function.BiFunctionWithSqlException;
import org.highmed.dsf.fhir.search.SearchQueryParameterError.SearchQueryParameterErrorType;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchQuery<R extends Resource> implements DbSearchQuery, Matcher
{
	public static final String PARAMETER_SORT = "_sort";
	public static final String PARAMETER_INCLUDE = "_include";
	public static final String PARAMETER_PAGE = "_page";
	public static final String PARAMETER_COUNT = "_count";
	public static final String PARAMETER_FORMAT = "_format";
	public static final String PARAMETER_PRETTY = "_pretty";

	public static final String[] STANDARD_PARAMETERS = { PARAMETER_SORT, PARAMETER_INCLUDE, PARAMETER_PAGE,
			PARAMETER_COUNT, PARAMETER_FORMAT, PARAMETER_PRETTY };

	public static class SearchQueryBuilder<R extends Resource>
	{
		public static <R extends Resource> SearchQueryBuilder<R> create(Class<R> resourceType, String resourceTable,
				String resourceColumn, int page, int count)
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

		createSortSql(getFirst(queryParameters, PARAMETER_SORT));
		createIncludeSql(queryParameters.get(PARAMETER_INCLUDE));
	}

	public List<SearchQueryParameterError> getUnsupportedQueryParameters(Map<String, List<String>> queryParameters)
	{
		Map<String, List<String>> parameters = new HashMap<String, List<String>>(queryParameters);
		searchParameters.stream().flatMap(p -> p.getBaseAndModifiedParameterNames()).forEach(parameters::remove);
		Arrays.asList(STANDARD_PARAMETERS).forEach(parameters::remove);

		List<SearchQueryParameterError> errors = new ArrayList<>(getDuplicateStandardParameters(queryParameters));

		parameters.keySet().stream().map(
				name -> new SearchQueryParameterError(SearchQueryParameterErrorType.UNSUPPORTED_PARAMETER, name, null))
				.forEach(errors::add);

		searchParameters.stream().flatMap(p -> p.getErrors().stream()).forEach(errors::add);

		if (!errors.isEmpty())
			logger.warn("Query parameters with error: {}", errors);
		
		return errors;
	}

	private List<SearchQueryParameterError> getDuplicateStandardParameters(Map<String, List<String>> queryParameters)
	{
		List<SearchQueryParameterError> errors = new ArrayList<>();
		for (String parameter : STANDARD_PARAMETERS)
		{
			List<String> values = queryParameters.get(parameter);
			if (values != null && values.size() > 1)
			{
				if (!PARAMETER_INCLUDE.equals(parameter) || hasDuplicates(values))
					errors.add(new SearchQueryParameterError(SearchQueryParameterErrorType.UNSUPPORTED_NUMBER_OF_VALUES,
							parameter, values));
			}
		}
		return errors;
	}

	private boolean hasDuplicates(List<String> values)
	{
		return values.size() != new HashSet<>(values).size();
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
	public void modifyStatement(PreparedStatement statement,
			BiFunctionWithSqlException<String, Object[], Array> arrayCreator) throws SQLException
	{
		try
		{
			List<SearchQueryParameter<?>> filtered = searchParameters.stream().filter(SearchQueryParameter::isDefined)
					.collect(Collectors.toList());

			int index = 0;
			for (SearchQueryParameter<?> q : filtered)
				for (int i = 0; i < q.getSqlParameterCount(); i++)
					q.modifyStatement(++index, i + 1, statement, arrayCreator);
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
			bundleUri.replaceQueryParam(PARAMETER_SORT, sortParameter());
		if (!includeParameters.isEmpty())
			bundleUri.replaceQueryParam(PARAMETER_INCLUDE, includeParameters());

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
	public void resloveReferencesForMatching(Resource resource, DaoProvider daoProvider) throws SQLException
	{
		if (resource == null)
			return;

		if (!getResourceType().isInstance(resource))
			return;

		List<SQLException> exceptions = searchParameters.stream().filter(SearchQueryParameter::isDefined).map(p ->
		{
			try
			{
				p.resolveReferencesForMatching(resource, daoProvider);
				return null;
			}
			catch (SQLException e)
			{
				return e;
			}
		}).filter(e -> e != null).collect(Collectors.toList());

		if (!exceptions.isEmpty())
		{
			SQLException sqlException = new SQLException("Error while resoling references");
			exceptions.forEach(sqlException::addSuppressed);
			throw sqlException;
		}
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (resource == null)
			return false;

		if (!getResourceType().isInstance(resource))
			return false;

		return searchParameters.stream().filter(SearchQueryParameter::isDefined).map(p -> p.matches(resource))
				.allMatch(b -> b);
	}

	@Override
	public void modifyIncludeResource(Resource resource, int columnIndex, Connection connection) throws SQLException
	{
		if (columnIndex - 1 > includeParameters.size())
		{
			logger.warn("Unexpected column-index {}, column-index - 1 larger than include parameter count {}",
					columnIndex, includeParameters.size());
			throw new IllegalStateException("Unexpected column-index " + columnIndex
					+ ", column-index - 1 larger than include parameter count " + includeParameters.size());
		}

		includeParameters.get(columnIndex - 2).modifyIncludeResource(resource, connection);
	}
}
