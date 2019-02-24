package org.highmed.fhir.search.parameters.basic;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.highmed.fhir.search.parameters.basic.SortParameter.SortDirection;
import org.hl7.fhir.r4.model.DomainResource;

public abstract class AbstractSearchParameter<R extends DomainResource> implements SearchParameter<R>
{
	public static final String SORT_PARAMETER = "_sort";

	protected final Class<R> resourceType;
	protected final String parameterName;

	private SortDirection sortDirection;

	public AbstractSearchParameter(Class<R> resourceType, String parameterName)
	{
		this.resourceType = resourceType;
		this.parameterName = parameterName;
	}

	public final String getParameterName()
	{
		return parameterName;
	}

	@Override
	public final void configure(Map<String, List<String>> queryParameters)
	{
		sortDirection = getSortDirection(getFirst(queryParameters, SORT_PARAMETER));

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

	protected abstract void configureSearchParameter(Map<String, List<String>> queryParameters);

	@Override
	public SortParameter getSortParameter()
	{
		return new SortParameter(getSortSql(sortDirection.getSqlModifierWithSpacePrefix()), sortDirection);
	}

	protected abstract String getSortSql(String sortDirectionWithSpacePrefix);

	@Override
	public Class<R> getResourceType()
	{
		return resourceType;
	}
}
