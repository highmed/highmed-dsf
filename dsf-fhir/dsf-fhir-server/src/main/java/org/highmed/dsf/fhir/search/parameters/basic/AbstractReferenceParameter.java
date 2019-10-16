package org.highmed.dsf.fhir.search.parameters.basic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.UriBuilder;

import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.search.SearchQueryIncludeParameter;
import org.highmed.dsf.fhir.search.SearchQueryIncludeParameter.IncludeParts;
import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.search.SearchQueryParameterError.SearchQueryParameterErrorType;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Resource;

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
				String parameterName, Map<String, List<String>> queryParameters,
				Consumer<SearchQueryParameterError> errors)
		{
			List<String> allValues = new ArrayList<>();
			allValues.addAll(queryParameters.getOrDefault(parameterName, Collections.emptyList()));
			allValues.addAll(queryParameters.getOrDefault(parameterName + PARAMETER_NAME_IDENTIFIER_MODIFIER,
					Collections.emptyList()));
			if (allValues.size() > 1)
				errors.accept(new SearchQueryParameterError(SearchQueryParameterErrorType.UNSUPPORTED_NUMBER_OF_VALUES,
						parameterName, allValues));

			final String param = getFirst(queryParameters, parameterName);
			final String identifierParam = getFirst(queryParameters,
					parameterName + PARAMETER_NAME_IDENTIFIER_MODIFIER);

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
					else
						errors.accept(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE,
								parameterName, queryParameters.get(parameterName),
								"Unsupported target resource type name " + splitAtSlash[0] + ", not one of "
										+ targetResourceTypeNames));
				}
				else if (param.startsWith("http") && targetResourceTypeNames.stream()
						.map(name -> param.contains("/" + name + "/")).anyMatch(b -> b))
					return Optional
							.of(new ReferenceValueAndSearchType(null, null, param, null, ReferenceSearchType.URL));
				else
					errors.accept(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE,
							parameterName, queryParameters.get(parameterName)));
			}
			else if (param != null && param.isBlank())
			{
				errors.accept(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE,
						parameterName, queryParameters.get(parameterName), "Value empty"));
			}
			else if (identifierParam != null && !identifierParam.isBlank())
			{
				return TokenValueAndSearchType
						.fromParamValue(parameterName + PARAMETER_NAME_IDENTIFIER_MODIFIER, queryParameters, errors)
						.map(identifier -> new ReferenceValueAndSearchType(null, null, null, identifier,
								ReferenceSearchType.IDENTIFIER));
			}
			else if (identifierParam != null && identifierParam.isBlank())
			{
				errors.accept(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE,
						parameterName, queryParameters.get(parameterName), "Value empty"));
			}

			if (queryParameters.get(parameterName) != null && queryParameters.get(parameterName).size() > 1)
				errors.accept(new SearchQueryParameterError(SearchQueryParameterErrorType.UNSUPPORTED_NUMBER_OF_VALUES,
						parameterName, queryParameters.get(parameterName)));

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
	protected Stream<String> getModifiedParameterNames()
	{
		return Stream.of(getParameterName() + PARAMETER_NAME_IDENTIFIER_MODIFIER);
	}

	@Override
	protected void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		valueAndType = ReferenceValueAndSearchType
				.fromParamValue(targetResourceTypeNames, parameterName, queryParameters, this::addError).orElse(null);
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
		List<String> includeParameterValues = queryParameters.getOrDefault(SearchQuery.PARAMETER_INCLUDE,
				Collections.emptyList());

		List<String> nonMatchingIncludeParameters = includeParameterValues.stream().map(IncludeParts::fromString)
				.filter(p -> !resourceTypeName.equals(p.getSourceResourceTypeName())
						|| !parameterName.equals(p.getSearchParameterName())
						|| !((targetResourceTypeNames.size() == 1 && p.getTargetResourceTypeName() == null)
								|| targetResourceTypeNames.contains(p.getTargetResourceTypeName())))
				.map(IncludeParts::toString).collect(Collectors.toList());

		if (!nonMatchingIncludeParameters.isEmpty())
			addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE,
					SearchQuery.PARAMETER_INCLUDE, includeParameterValues, "Non matching include parameter"
							+ (nonMatchingIncludeParameters.size() != 1 ? "s " : " ") + nonMatchingIncludeParameters));

		return includeParameterValues.stream().map(IncludeParts::fromString)
				.filter(p -> resourceTypeName.equals(p.getSourceResourceTypeName())
						&& parameterName.equals(p.getSearchParameterName())
						&& ((targetResourceTypeNames.size() == 1 && p.getTargetResourceTypeName() == null)
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
	public void resolveReferencesForMatching(Resource resource, DaoProvider daoProvider) throws SQLException
	{
		if (resourceType.isInstance(resource))
			doResolveReferencesForMatching(resourceType.cast(resource), daoProvider);
	}

	protected abstract void doResolveReferencesForMatching(R resource, DaoProvider daoProvider) throws SQLException;

	@Override
	public void modifyIncludeResource(Resource resource, Connection connection)
	{
		doModifyIncludeResource(resource, connection);
	}

	/**
	 * Use this method to modify the include resources. This method can be used if the resources returned by the include
	 * SQL are not complete and additional content needs to be retrieved from a not included column. For example the
	 * content of a {@link Binary} resource might not be stored in the json column.
	 * 
	 * @param resource
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 */
	protected abstract void doModifyIncludeResource(Resource resource, Connection connection);
}
