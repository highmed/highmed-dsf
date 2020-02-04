package org.highmed.dsf.fhir.search.parameters.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.search.SearchQueryIncludeParameter;
import org.highmed.dsf.fhir.search.SearchQueryParameter;
import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.search.SearchQuerySortParameter;
import org.highmed.dsf.fhir.search.SearchQuerySortParameter.SortDirection;
import org.hl7.fhir.r4.model.Resource;

public abstract class AbstractSearchParameter<R extends Resource> implements SearchQueryParameter<R>
{
	protected final String parameterName;

	private SearchQuerySortParameter sortParameter;
	private final List<SearchQueryParameterError> errors = new ArrayList<SearchQueryParameterError>();

	public AbstractSearchParameter(String parameterName)
	{
		this.parameterName = parameterName;
	}

	@Override
	public final String getParameterName()
	{
		return parameterName;
	}

	@Override
	public Stream<String> getBaseAndModifiedParameterNames()
	{
		return Stream.concat(Stream.of(getParameterName()), getModifiedParameterNames());
	}

	protected Stream<String> getModifiedParameterNames()
	{
		return Stream.empty();
	}

	protected IllegalStateException notDefined()
	{
		return new IllegalStateException("not defined");
	}

	@Override
	public final void configure(Map<String, List<String>> queryParameters)
	{
		SortDirection sortDirection = getSortDirection(getFirst(queryParameters, SearchQuery.PARAMETER_SORT));
		if (sortDirection != null)
			sortParameter = new SearchQuerySortParameter(getSortSql(sortDirection.getSqlModifierWithSpacePrefix()),
					parameterName, sortDirection);

		configureIncludeParameter(queryParameters);

		configureSearchParameter(queryParameters);
	}

	@Override
	public List<SearchQueryParameterError> getErrors()
	{
		return Collections.unmodifiableList(errors);
	}

	protected final void addError(SearchQueryParameterError error)
	{
		errors.add(error);
	}

	protected static String getFirst(Map<String, List<String>> queryParameters, String key)
	{
		if (queryParameters.containsKey(key) && !queryParameters.get(key).isEmpty())
			return queryParameters.get(key).get(0);
		else
			return null;
	}

	private SortDirection getSortDirection(String sortParameters)
	{
		if (sortParameters == null || sortParameters.isBlank())
			return null;

		Optional<String> sortParameter = Arrays.stream(sortParameters.split(","))
				.filter(s -> s.equals(parameterName) || s.equals("+" + parameterName) || s.equals("-" + parameterName))
				.findFirst();

		return sortParameter.map(SortDirection::fromString).orElse(null);
	}

	protected void configureIncludeParameter(Map<String, List<String>> queryParameters)
	{
		// default impl does nothing
	}

	protected abstract void configureSearchParameter(Map<String, List<String>> queryParameters);

	@Override
	public Optional<SearchQuerySortParameter> getSortParameter()
	{
		return Optional.ofNullable(sortParameter);
	}

	protected abstract String getSortSql(String sortDirectionWithSpacePrefix);

	@Override
	public List<SearchQueryIncludeParameter> getIncludeParameters()
	{
		return Collections.emptyList();
	}
}
