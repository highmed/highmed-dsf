package org.highmed.fhir.search.parameters.basic;

import java.util.Arrays;
import java.util.Optional;

import javax.ws.rs.core.MultivaluedMap;

import org.highmed.fhir.search.parameters.basic.SortParameter.SortDirection;
import org.hl7.fhir.r4.model.DomainResource;

public abstract class AbstractSearchParameter<R extends DomainResource> implements SearchParameter<R>
{
	public static final String SORT_PARAMETER = "_sort";

	protected final String parameterName;

	private SortDirection sortDirection;

	public AbstractSearchParameter(String parameterName)
	{
		this.parameterName = parameterName;
	}

	public final String getParameterName()
	{
		return parameterName;
	}

	@Override
	public final void configure(MultivaluedMap<String, String> queryParameters)
	{
		sortDirection = getSortDirection(queryParameters.getFirst(SORT_PARAMETER));

		configureSearchParameter(queryParameters);
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

	protected abstract void configureSearchParameter(MultivaluedMap<String, String> queryParameters);

	@Override
	public SortParameter getSortParameter()
	{
		return new SortParameter(getSortSql(sortDirection.getSqlModifierWithSpacePrefix()), sortDirection);
	}

	protected abstract String getSortSql(String sortDirectionWithSpacePrefix);
}
