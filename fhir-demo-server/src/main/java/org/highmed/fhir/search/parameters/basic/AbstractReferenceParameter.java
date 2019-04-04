package org.highmed.fhir.search.parameters.basic;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import org.highmed.fhir.dao.DaoProvider;
import org.highmed.fhir.search.SearchQueryIncludeParameter;
import org.highmed.fhir.search.SearchQueryIncludeParameter.IncludeParts;
import org.hl7.fhir.r4.model.DomainResource;

public abstract class AbstractReferenceParameter<R extends DomainResource> extends AbstractSearchParameter<R>
{
	private static final String PARAMETER_NAME_IDENTIFIER_MODIFIER = ":identifier";

	protected static enum ReferenceSearchType
	{
		ID, RESOURCE_NAME_AND_ID, URL, IDENTIFIER
	}

	protected static class ReferenceValueAndSearchType
	{
		public final String resourceName;
		public final String id;
		public final String url;
		public final TokenValueAndSearchType identifier;

		public final ReferenceSearchType type;

		private ReferenceValueAndSearchType(String resourceName, String id, String url,
				TokenValueAndSearchType identifier, ReferenceSearchType type)
		{
			this.resourceName = resourceName;
			this.id = id;
			this.url = url;
			this.type = type;
			this.identifier = identifier;
		}

		public static Optional<ReferenceValueAndSearchType> fromParamValue(List<String> targetResourceTypeNames,
				String param, String identifierParam)
		{
			if (param != null && !param.isBlank())
			{
				if (param.indexOf('/') == -1 && targetResourceTypeNames.size() == 1)
					return Optional
							.of(new ReferenceValueAndSearchType(null, param, null, null, ReferenceSearchType.ID));
				else if (param.indexOf('/') >= 0)
				{
					String[] splitAtSlash = param.split("/");
					if (splitAtSlash.length == 2 && targetResourceTypeNames.stream()
							.map(name -> name.equals(splitAtSlash[0])).anyMatch(b -> b))
						return Optional.of(new ReferenceValueAndSearchType(splitAtSlash[0], splitAtSlash[1], null, null,
								ReferenceSearchType.RESOURCE_NAME_AND_ID));
				}
				else if (param.startsWith("http") && targetResourceTypeNames.stream()
						.map(name -> param.contains("/" + name + "/")).anyMatch(b -> b))
					return Optional
							.of(new ReferenceValueAndSearchType(null, null, param, null, ReferenceSearchType.URL));
			}
			else if (identifierParam != null && !identifierParam.isBlank())
			{
				return TokenValueAndSearchType.fromParamValue(identifierParam)
						.map(identifier -> new ReferenceValueAndSearchType(null, null, null, identifier,
								ReferenceSearchType.IDENTIFIER));
			}

			return Optional.empty();
		}
	}

	private final Class<R> resourceType;
	private final String resourceTypeName;
	private final List<String> targetResourceTypeNames;

	private SearchQueryIncludeParameter includeParameter;

	protected ReferenceValueAndSearchType valueAndType;

	public AbstractReferenceParameter(Class<R> resourceType, String resourceTypeName, String parameterName,
			String... targetResourceTypeNames)
	{
		super(parameterName);

		this.resourceType = resourceType;
		this.resourceTypeName = resourceTypeName;
		this.targetResourceTypeNames = Arrays.asList(targetResourceTypeNames);
	}

	@Override
	protected void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		String param = getFirst(queryParameters, parameterName);
		String identifierParam = getFirst(queryParameters, parameterName + PARAMETER_NAME_IDENTIFIER_MODIFIER);

		valueAndType = ReferenceValueAndSearchType.fromParamValue(targetResourceTypeNames, param, identifierParam)
				.orElse(null);
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

			case IDENTIFIER:
			{
				switch (valueAndType.identifier.type)
				{
					case CODE:
						bundleUri.replaceQueryParam(parameterName + PARAMETER_NAME_IDENTIFIER_MODIFIER,
								valueAndType.identifier.codeValue);
						break;

					case CODE_AND_SYSTEM:
						bundleUri.replaceQueryParam(parameterName + PARAMETER_NAME_IDENTIFIER_MODIFIER,
								valueAndType.identifier.systemValue + "|" + valueAndType.identifier.codeValue);
						break;

					case CODE_AND_NO_SYSTEM_PROPERTY:
						bundleUri.replaceQueryParam(parameterName + PARAMETER_NAME_IDENTIFIER_MODIFIER,
								"|" + valueAndType.identifier.codeValue);
						break;

					case SYSTEM:
						bundleUri.replaceQueryParam(parameterName + PARAMETER_NAME_IDENTIFIER_MODIFIER,
								valueAndType.identifier.systemValue + "|");
						break;
				}
			}
		}
	}

	@Override
	public Optional<SearchQueryIncludeParameter> getIncludeParameter()
	{
		return Optional.ofNullable(includeParameter);
	}

	protected abstract String getIncludeSql(IncludeParts includeParts);

	@Override
	public void resolveReferencesForMatching(DomainResource resource, DaoProvider daoProvider) throws SQLException
	{
		if (resourceType.isInstance(resource))
			doResolveReferencesForMatching(resourceType.cast(resource), daoProvider);
	}

	protected abstract void doResolveReferencesForMatching(R resource, DaoProvider daoProvider) throws SQLException;
}
