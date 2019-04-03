package org.highmed.fhir.search.parameters.basic;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.highmed.fhir.dao.DaoProvider;
import org.highmed.fhir.search.SearchQueryIncludeParameter;
import org.highmed.fhir.search.SearchQueryParameter;
import org.highmed.fhir.search.SearchQuerySortParameter;
import org.highmed.fhir.search.SearchQuerySortParameter.SortDirection;
import org.hl7.fhir.r4.model.DomainResource;

public abstract class AbstractSearchParameter<R extends DomainResource> implements SearchQueryParameter<R>
{
	public static final String SORT_PARAMETER = "_sort";
	public static final String INCLUDE_PARAMETER = "_include";

	protected final String parameterName;

	private SearchQuerySortParameter sortParameter;

	public AbstractSearchParameter(String parameterName)
	{
		this.parameterName = parameterName;
	}

	public final String getParameterName()
	{
		return parameterName;
	}

	protected IllegalStateException notDefined()
	{
		return new IllegalStateException("not defined");
	}

	@Override
	public final void configure(Map<String, List<String>> queryParameters)
	{
		SortDirection sortDirection = getSortDirection(getFirst(queryParameters, SORT_PARAMETER));
		if (sortDirection != null)
			sortParameter = new SearchQuerySortParameter(getSortSql(sortDirection.getSqlModifierWithSpacePrefix()),
					parameterName, sortDirection);

		configureIncludeParameter(queryParameters);

		configureSearchParameter(queryParameters);
	}

	protected String getFirst(Map<String, List<String>> queryParameters, String key)
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
	public Optional<SearchQueryIncludeParameter> getIncludeParameter()
	{
		return Optional.empty();
	}

	@Override
	public void resloveReferencesForMatching(DomainResource resource, DaoProvider daoProvider) throws SQLException
	{
	}
}
