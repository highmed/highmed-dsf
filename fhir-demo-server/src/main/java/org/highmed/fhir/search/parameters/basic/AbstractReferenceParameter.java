package org.highmed.fhir.search.parameters.basic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import org.highmed.fhir.search.SearchQueryIncludeParameter;
import org.highmed.fhir.search.SearchQueryIncludeParameter.IncludeParts;
import org.hl7.fhir.r4.model.DomainResource;

public abstract class AbstractReferenceParameter<R extends DomainResource> extends AbstractSearchParameter<R>
{
	protected enum ReferenceSearchType
	{
		ID, RESOURCE_NAME_AND_ID, URL
	}

	protected class ReferenceValueAndSearchType
	{
		public final String resourceName;
		public final String id;
		public final String url;

		public final ReferenceSearchType type;

		ReferenceValueAndSearchType(String resourceName, String id, String url, ReferenceSearchType type)
		{
			this.resourceName = resourceName;
			this.id = id;
			this.url = url;
			this.type = type;
		}
	}

	private final String resourceTypeName;
	private final List<String> targetResourceTypeNames;

	private SearchQueryIncludeParameter includeParameter;

	protected ReferenceValueAndSearchType valueAndType;

	public AbstractReferenceParameter(String resourceTypeName, String parameterName, String... targetResourceTypeNames)
	{
		super(parameterName);
		this.resourceTypeName = resourceTypeName;
		this.targetResourceTypeNames = Arrays.asList(targetResourceTypeNames);
	}

	@Override
	protected void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		String param = getFirst(queryParameters, parameterName);
		if (param != null && !param.isEmpty())
		{
			if (param.indexOf('/') == -1 && targetResourceTypeNames.size() == 1)
				valueAndType = new ReferenceValueAndSearchType(null, param, null, ReferenceSearchType.ID);
			else if (param.indexOf('/') >= 0)
			{
				String[] splitAtSlash = param.split("/");
				if (splitAtSlash.length == 2
						&& targetResourceTypeNames.stream().map(n -> n.equals(splitAtSlash[0])).anyMatch(b -> b))
					valueAndType = new ReferenceValueAndSearchType(splitAtSlash[0], splitAtSlash[1], null,
							ReferenceSearchType.RESOURCE_NAME_AND_ID);
			}
			else if (param.startsWith("http")
					&& targetResourceTypeNames.stream().map(n -> param.contains("/" + n + "/")).anyMatch(b -> b))
				valueAndType = new ReferenceValueAndSearchType(null, null, param, ReferenceSearchType.URL);
		}
	}

	@Override
	protected void configureIncludeParameter(Map<String, List<String>> queryParameters)
	{
		List<IncludeParts> includeParts = getIncludeParts(queryParameters);

		if (!includeParts.isEmpty())
		{
			List<String> includeSqls = includeParts.stream().map(this::getIncludeSql).filter(s -> s != null)
					.collect(Collectors.toList());
			includeParameter = new SearchQueryIncludeParameter(includeSqls, includeParts);
		}
	}

	private List<IncludeParts> getIncludeParts(Map<String, List<String>> queryParameters)
	{
		List<String> includeParameterValues = queryParameters.getOrDefault(INCLUDE_PARAMETER, Collections.emptyList());

		return includeParameterValues.stream().map(IncludeParts::fromString)
				.filter(p -> resourceTypeName.equals(p.getSourceResourceTypeName())
						&& parameterName.equals(p.getSearchParameterName())
						&& (p.getTargetResourceTypeName() == null
								|| targetResourceTypeNames.contains(p.getTargetResourceTypeName())))
				.collect(Collectors.toList());
	}

	@Override
	public boolean isDefined()
	{
		return valueAndType != null;
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		switch (valueAndType.type)
		{
			case ID:
			case URL:
				bundleUri.replaceQueryParam(parameterName, valueAndType.id);
				break;

			case RESOURCE_NAME_AND_ID:
				bundleUri.replaceQueryParam(parameterName, valueAndType.resourceName + "/" + valueAndType.id);
				break;
		}
	}

	@Override
	public Optional<SearchQueryIncludeParameter> getIncludeParameter()
	{
		return Optional.ofNullable(includeParameter);
	}

	protected abstract String getIncludeSql(IncludeParts includeParts);
}
