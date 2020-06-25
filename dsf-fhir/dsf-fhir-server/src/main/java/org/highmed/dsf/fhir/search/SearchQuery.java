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
import java.util.stream.Stream;

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
	public static final String PARAMETER_REVINCLUDE = "_revinclude";
	public static final String PARAMETER_PAGE = "_page";
	public static final String PARAMETER_COUNT = "_count";
	public static final String PARAMETER_FORMAT = "_format";
	public static final String PARAMETER_PRETTY = "_pretty";

	public static final String[] STANDARD_PARAMETERS = { PARAMETER_SORT, PARAMETER_INCLUDE, PARAMETER_REVINCLUDE,
			PARAMETER_PAGE, PARAMETER_COUNT, PARAMETER_FORMAT, PARAMETER_PRETTY };

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
		private final List<SearchQueryRevIncludeParameterFactory> revIncludeParameters = new ArrayList<>();

		private SearchQueryUserFilter userFilter; // may be null

		private SearchQueryBuilder(Class<R> resourceType, String resourceTable, String resourceColumn, int page,
				int count)
		{
			this.resourceType = resourceType;
			this.resourceTable = resourceTable;
			this.resourceColumn = resourceColumn;

			this.page = page;
			this.count = count;
		}

		public SearchQueryBuilder<R> with(SearchQueryUserFilter userFilter)
		{
			this.userFilter = userFilter;
			return this;
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

		public SearchQueryBuilder<R> withRevInclude(SearchQueryRevIncludeParameterFactory searchParameters)
		{
			this.revIncludeParameters.add(searchParameters);
			return this;
		}

		public SearchQueryBuilder<R> withRevInclude(SearchQueryRevIncludeParameterFactory... searchParameters)
		{
			return withRevInclude(Arrays.asList(searchParameters));
		}

		public SearchQueryBuilder<R> withRevInclude(List<SearchQueryRevIncludeParameterFactory> searchParameters)
		{
			this.revIncludeParameters.addAll(searchParameters);
			return this;
		}

		public SearchQuery<R> build()
		{
			return new SearchQuery<R>(resourceType, resourceTable, resourceColumn, userFilter, page, count,
					searchParameters, revIncludeParameters);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(SearchQuery.class);

	private final Class<R> resourceType;
	private final String resourceColumn;
	private final String resourceTable;

	private final SearchQueryUserFilter userFilter;

	private final PageAndCount pageAndCount;

	private final List<SearchQueryParameter<R>> searchParameters = new ArrayList<>();
	private final List<SearchQueryRevIncludeParameterFactory> revIncludeParameterFactories = new ArrayList<>();

	private String filterQuery;
	private String sortSql;
	private String includeSql;
	private String revIncludeSql;
	private List<SearchQueryParameter<R>> sortParameters = Collections.emptyList();
	private List<SearchQueryIncludeParameter> includeParameters = Collections.emptyList();
	private List<SearchQueryIncludeParameter> revIncludeParameters = Collections.emptyList();

	SearchQuery(Class<R> resourceType, String resourceTable, String resourceColumn, SearchQueryUserFilter userFilter,
			int page, int count, List<? extends SearchQueryParameter<R>> searchParameters,
			List<? extends SearchQueryRevIncludeParameterFactory> revIncludeParameters)
	{
		this.resourceType = resourceType;
		this.resourceTable = resourceTable;
		this.resourceColumn = resourceColumn;

		this.userFilter = userFilter;

		this.pageAndCount = new PageAndCount(page, count);

		this.searchParameters.addAll(searchParameters);
		this.revIncludeParameterFactories.addAll(revIncludeParameters);
	}

	public SearchQuery<R> configureParameters(Map<String, List<String>> queryParameters)
	{
		searchParameters.forEach(p -> p.configure(queryParameters));

		List<String> revIncludeParameterValues = queryParameters.getOrDefault(PARAMETER_REVINCLUDE,
				Collections.emptyList());
		revIncludeParameterFactories.forEach(p -> p.configure(revIncludeParameterValues));

		includeSql = createIncludeSql(queryParameters.get(PARAMETER_INCLUDE));
		revIncludeSql = createRevIncludeSql();

		filterQuery = createFilterQuery();

		sortSql = createSortSql(getFirst(queryParameters, PARAMETER_SORT));

		return this;
	}

	private String createFilterQuery()
	{
		Stream<String> elements = searchParameters.stream().filter(SearchQueryParameter::isDefined)
				.map(SearchQueryParameter::getFilterQuery);

		if (userFilter != null && !userFilter.getFilterQuery().isEmpty())
			elements = Stream.concat(Stream.of(userFilter.getFilterQuery()), elements);

		return elements.collect(Collectors.joining(" AND "));
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
		revIncludeParameterFactories.stream().flatMap(p -> p.getErrors().stream()).forEach(errors::add);

		List<String> includeParameterValues = queryParameters.getOrDefault(PARAMETER_INCLUDE, Collections.emptyList());
		includeParameters.stream().map(SearchQueryIncludeParameter::getBundleUriQueryParameterValues)
				.forEach(v -> includeParameterValues.remove(v));
		if (!includeParameterValues.isEmpty())
			errors.add(new SearchQueryParameterError(SearchQueryParameterErrorType.UNSUPPORTED_PARAMETER,
					PARAMETER_INCLUDE, includeParameterValues));

		List<String> revIncludeParameterValues = new ArrayList<>(
				queryParameters.getOrDefault(PARAMETER_REVINCLUDE, Collections.emptyList()));
		revIncludeParameters.stream().map(SearchQueryIncludeParameter::getBundleUriQueryParameterValues)
				.forEach(v -> revIncludeParameterValues.remove(v));
		if (!revIncludeParameterValues.isEmpty())
			errors.add(new SearchQueryParameterError(SearchQueryParameterErrorType.UNSUPPORTED_PARAMETER,
					PARAMETER_REVINCLUDE, revIncludeParameterValues));

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
				if ((!PARAMETER_INCLUDE.equals(parameter) && !PARAMETER_REVINCLUDE.equals(parameter))
						|| hasDuplicates(values))
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

	private String createSortSql(String sortParameterValue)
	{
		if (sortParameterValue == null)
			return "";

		sortParameters = searchParameters.stream().filter(sp -> sp.getSortParameter().isPresent())
				.collect(Collectors.toList());

		if (sortParameters.isEmpty())
			return "";

		return sortParameters.stream().map(sp -> sp.getSortParameter().get().getSql())
				.collect(Collectors.joining(", ", " ORDER BY ", ""));
	}

	private String createIncludeSql(List<String> includeParameterValues)
	{
		if (includeParameterValues == null || includeParameterValues.isEmpty())
			return "";

		includeParameters = searchParameters.stream().flatMap(sp -> sp.getIncludeParameters().stream())
				.collect(Collectors.toList());

		if (includeParameters.isEmpty())
			return "";

		return includeParameters.stream().map(SearchQueryIncludeParameter::getSql)
				.collect(Collectors.joining(", ", ", ", ""));
	}

	private String createRevIncludeSql()
	{
		if (revIncludeParameterFactories == null || revIncludeParameterFactories.isEmpty())
			return "";

		revIncludeParameters = revIncludeParameterFactories.stream().flatMap(f -> f.getRevIncludeParameters().stream())
				.collect(Collectors.toList());

		if (revIncludeParameters.isEmpty())
			return "";

		return revIncludeParameters.stream().map(SearchQueryIncludeParameter::getSql)
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
		String searchQueryMain = "SELECT " + resourceColumn + includeSql + revIncludeSql + " FROM current_"
				+ resourceTable;

		return searchQueryMain + (!filterQuery.isEmpty() ? (" WHERE " + filterQuery) : "") + sortSql
				+ pageAndCount.getSql();
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
			if (userFilter != null)
			{
				while (index < userFilter.getSqlParameterCount())
				{
					int i = ++index;
					userFilter.modifyStatement(i, i, statement);
				}
			}

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

	public UriBuilder configureBundleUri(UriBuilder bundleUri)
	{
		Objects.requireNonNull(bundleUri, "bundleUri");

		searchParameters.stream().filter(SearchQueryParameter::isDefined).forEach(p -> p.modifyBundleUri(bundleUri));

		if (!sortParameters.isEmpty())
			bundleUri.replaceQueryParam(PARAMETER_SORT, sortParameter());
		if (!includeParameters.isEmpty())
			bundleUri.replaceQueryParam(PARAMETER_INCLUDE, includeParameters());
		if (!revIncludeParameterFactories.isEmpty())
			bundleUri.replaceQueryParam(PARAMETER_REVINCLUDE, revIncludeParameters());

		return bundleUri;
	}

	private String sortParameter()
	{
		return sortParameters.stream().map(p -> p.getSortParameter().get().getBundleUriQueryParameterValuePart())
				.collect(Collectors.joining(","));
	}

	private Object[] includeParameters()
	{
		return includeParameters.stream().map(SearchQueryIncludeParameter::getBundleUriQueryParameterValues).toArray();
	}

	private Object[] revIncludeParameters()
	{
		return revIncludeParameters.stream().map(SearchQueryIncludeParameter::getBundleUriQueryParameterValues)
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
		int includeParameterCount = includeParameters.size();
		int revIncludeParameterCount = revIncludeParameters.size();

		if (includeParameterCount > 0 && columnIndex - 1 <= includeParameterCount)
		{
			includeParameters.get(columnIndex - 2).modifyIncludeResource(resource, connection);
		}
		else if (revIncludeParameters.size() > 0 && columnIndex - 1 - includeParameterCount <= revIncludeParameterCount)
		{
			revIncludeParameters.get(columnIndex - 2 - includeParameterCount).modifyIncludeResource(resource,
					connection);
		}
		else
		{
			logger.warn(
					"Unexpected column-index {}, column-index - 1 larger than include ({}) + revinclude ({}) parameter count {}",
					columnIndex, includeParameterCount, revIncludeParameterCount,
					includeParameterCount + revIncludeParameterCount);
			throw new IllegalStateException(
					"Unexpected column-index " + columnIndex + ", column-index - 1 larger than include ("
							+ includeParameterCount + ") + revinclude (" + revIncludeParameterCount
							+ ") parameter count " + (includeParameterCount + revIncludeParameterCount));
		}
	}
}
