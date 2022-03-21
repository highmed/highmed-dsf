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
import org.highmed.dsf.fhir.search.IncludeParts;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.search.SearchQueryIncludeParameter;
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
		ID, TYPE_AND_ID, RESOURCE_NAME_AND_ID, TYPE_AND_RESOURCE_NAME_AND_ID, URL, IDENTIFIER
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

		// [parameter]=[uuid] -> local id
		//
		// [parameter]=[Type]/[uuid] -> local type+id
		//
		// [parameter]=[url] -> absolute id or canonical
		//
		// [parameter]:[Type]=[uuid] -> local type+id
		// [parameter]:identifier=[identifier] -> identifier (not supported for canonical references)
		public static Optional<ReferenceValueAndSearchType> fromParamValue(List<String> targetResourceTypeNames,
				String parameterName, Map<String, List<String>> queryParameters,
				Consumer<SearchQueryParameterError> errors)
		{
			List<String> allValues = new ArrayList<>();
			allValues.addAll(queryParameters.getOrDefault(parameterName, Collections.emptyList()));
			allValues.addAll(queryParameters.getOrDefault(parameterName + PARAMETER_NAME_IDENTIFIER_MODIFIER,
					Collections.emptyList()));
			allValues.addAll(targetResourceTypeNames.stream().flatMap(
					type -> queryParameters.getOrDefault(parameterName + ":" + type, Collections.emptyList()).stream())
					.collect(Collectors.toList()));

			if (allValues.size() > 1)
				errors.accept(new SearchQueryParameterError(SearchQueryParameterErrorType.UNSUPPORTED_NUMBER_OF_VALUES,
						parameterName, allValues));
			else if (allValues.isEmpty())
				return Optional.empty();

			final String value = allValues.get(0);

			// simple case
			if (queryParameters.containsKey(parameterName))
			{
				if (value.indexOf('/') == -1 && targetResourceTypeNames.size() == 1)
					return Optional.of(new ReferenceValueAndSearchType(targetResourceTypeNames.get(0), value, null,
							null, ReferenceSearchType.ID));
				else if (value.indexOf('/') == -1 && targetResourceTypeNames.size() > 1)
					return Optional
							.of(new ReferenceValueAndSearchType(null, value, null, null, ReferenceSearchType.ID));
				else if (value.startsWith("http"))
					return Optional
							.of(new ReferenceValueAndSearchType(null, null, value, null, ReferenceSearchType.URL));
				else if (value.indexOf('/') >= 0)
				{
					String[] splitAtSlash = value.split("/");
					if (splitAtSlash.length == 2
							&& targetResourceTypeNames.stream().anyMatch(name -> name.equals(splitAtSlash[0])))
						return Optional.of(new ReferenceValueAndSearchType(splitAtSlash[0], splitAtSlash[1], null, null,
								ReferenceSearchType.RESOURCE_NAME_AND_ID));
					else
						errors.accept(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE,
								parameterName, queryParameters.get(parameterName),
								"Unsupported target resource type name " + splitAtSlash[0] + ", not one of "
										+ targetResourceTypeNames));
				}

				else
					errors.accept(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE,
							parameterName, queryParameters.get(parameterName)));
			}
			// typed parameter
			else if (targetResourceTypeNames.stream()
					.anyMatch(type -> queryParameters.containsKey(parameterName + ":" + type)))
			{
				final String paramType = targetResourceTypeNames.stream()
						.filter(type -> queryParameters.containsKey(parameterName + ":" + type)).findFirst().get();

				if (value.indexOf('/') == -1 && targetResourceTypeNames.size() == 1)
				{
					if (paramType.equals(targetResourceTypeNames.get(0)))
						return Optional.of(new ReferenceValueAndSearchType(paramType, value, null, null,
								ReferenceSearchType.TYPE_AND_ID));
					else
						errors.accept(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE,
								parameterName, queryParameters.get(parameterName),
								"Unsupported target resource type name " + paramType + ", not equal to "
										+ targetResourceTypeNames.get(0)));
				}
				else if (value.indexOf('/') >= 0)
				{
					String[] splitAtSlash = value.split("/");
					if (splitAtSlash.length == 2
							&& targetResourceTypeNames.stream().anyMatch(name -> name.equals(splitAtSlash[0])))
					{
						if (paramType.equals(splitAtSlash[0]))
							return Optional.of(new ReferenceValueAndSearchType(splitAtSlash[0], splitAtSlash[1], null,
									null, ReferenceSearchType.TYPE_AND_RESOURCE_NAME_AND_ID));
						else
							errors.accept(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE,
									parameterName, queryParameters.get(parameterName),
									"Inconsistent target resource type name " + paramType + " vs. " + splitAtSlash[0]));
					}
					else
						errors.accept(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE,
								parameterName, queryParameters.get(parameterName),
								"Unsupported target resource type name " + splitAtSlash[0] + ", not one of "
										+ targetResourceTypeNames));
				}
			}
			// identifier
			else if (queryParameters.containsKey(parameterName + PARAMETER_NAME_IDENTIFIER_MODIFIER))
			{
				if (value != null && !value.isBlank())
				{
					return TokenValueAndSearchType
							.fromParamValue(parameterName + PARAMETER_NAME_IDENTIFIER_MODIFIER, queryParameters, errors)
							.map(identifier -> new ReferenceValueAndSearchType(null, null, null, identifier,
									ReferenceSearchType.IDENTIFIER));
				}
				else if (value == null || value.isBlank())
				{
					errors.accept(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE,
							parameterName, queryParameters.get(parameterName), "Value empty"));
				}
			}

			return Optional.empty();
		}
	}

	private final Class<R> resourceType;
	private final String resourceTypeName;
	private final List<String> targetResourceTypeNames;

	private final List<SearchQueryIncludeParameter> includeParameters = new ArrayList<>();

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

		for (IncludeParts ip : includeParts)
		{
			String includeSql = getIncludeSql(ip);
			if (includeSql != null)
				includeParameters.add(new SearchQueryIncludeParameter(includeSql, ip,
						(resource, connection) -> modifyIncludeResource(ip, resource, connection)));
		}
	}

	private List<IncludeParts> getIncludeParts(Map<String, List<String>> queryParameters)
	{
		List<String> includeParameterValues = queryParameters.getOrDefault(SearchQuery.PARAMETER_INCLUDE,
				Collections.emptyList());

		List<IncludeParts> includeParts = includeParameterValues.stream().map(IncludeParts::fromString)
				.filter(p -> resourceTypeName.equals(p.getSourceResourceTypeName())
						&& parameterName.equals(p.getSearchParameterName())
						&& ((targetResourceTypeNames.size() == 1 && p.getTargetResourceTypeName() == null)
								|| targetResourceTypeNames.contains(p.getTargetResourceTypeName())))
				.collect(Collectors.toList());

		return includeParts;
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
				bundleUri.replaceQueryParam(parameterName, valueAndType.id);
				break;
			case URL:
				bundleUri.replaceQueryParam(parameterName, valueAndType.url);
				break;
			case RESOURCE_NAME_AND_ID:
				bundleUri.replaceQueryParam(parameterName, valueAndType.resourceName + "/" + valueAndType.id);
				break;

			case TYPE_AND_ID:
				bundleUri.replaceQueryParam(parameterName + ":" + valueAndType.resourceName, valueAndType.id);
				break;
			case TYPE_AND_RESOURCE_NAME_AND_ID:
				bundleUri.replaceQueryParam(parameterName + ":" + valueAndType.resourceName,
						valueAndType.resourceName + "/" + valueAndType.id);
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
	public List<SearchQueryIncludeParameter> getIncludeParameters()
	{
		return Collections.unmodifiableList(includeParameters);
	}

	protected abstract String getIncludeSql(IncludeParts includeParts);

	@Override
	public void resolveReferencesForMatching(Resource resource, DaoProvider daoProvider) throws SQLException
	{
		if (resourceType.isInstance(resource))
			doResolveReferencesForMatching(resourceType.cast(resource), daoProvider);
	}

	protected abstract void doResolveReferencesForMatching(R resource, DaoProvider daoProvider) throws SQLException;

	/**
	 * Use this method to modify the include resources. This method can be used if the resources returned by the include
	 * SQL are not complete and additional content needs to be retrieved from a not included column. For example the
	 * content of a {@link Binary} resource might not be stored in the json column.
	 *
	 * @param includeParts
	 *            not <code>null</code>
	 * @param resource
	 *            not <code>null</code>
	 * @param connection
	 *            not <code>null</code>
	 */
	protected abstract void modifyIncludeResource(IncludeParts includeParts, Resource resource, Connection connection);
}
