package org.highmed.fhir.search.parameters.basic;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.ws.rs.core.UriBuilder;

import org.highmed.fhir.search.SearchQueryParameterError;
import org.highmed.fhir.search.SearchQueryParameterError.SearchQueryParameterErrorType;
import org.hl7.fhir.r4.model.DomainResource;

public abstract class AbstractStringParameter<R extends DomainResource> extends AbstractSearchParameter<R>
{
	public static enum StringSearchType
	{
		STARTS_WITH(""), EXACT(":exact"), CONTAINS(":contains");

		public final String modifier;

		private StringSearchType(String modifier)
		{
			this.modifier = modifier;
		}
	}

	protected static class StringValueAndSearchType
	{
		public final String value;
		public final StringSearchType type;

		private StringValueAndSearchType(String value, StringSearchType type)
		{
			this.value = value;
			this.type = type;
		}
	}

	protected StringValueAndSearchType valueAndType;

	public AbstractStringParameter(String parameterName)
	{
		super(parameterName);
	}

	@Override
	protected Stream<String> getModifiedParameterNames()
	{
		return Stream.of(getParameterName() + StringSearchType.EXACT.modifier,
				getParameterName() + StringSearchType.CONTAINS.modifier);
	}

	@Override
	protected final void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		String startsWith = getFirst(queryParameters, parameterName);
		if (startsWith != null)
		{
			valueAndType = new StringValueAndSearchType(startsWith, StringSearchType.STARTS_WITH);
			return;
		}

		String exact = getFirst(queryParameters, parameterName + StringSearchType.EXACT.modifier);
		if (exact != null)
		{
			valueAndType = new StringValueAndSearchType(exact, StringSearchType.EXACT);
			return;
		}

		String contains = getFirst(queryParameters, parameterName + StringSearchType.CONTAINS.modifier);
		if (contains != null)
		{
			valueAndType = new StringValueAndSearchType(contains, StringSearchType.CONTAINS);
			return;
		}

		if (queryParameters.get(parameterName) != null && queryParameters.get(parameterName).size() > 1)
			addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNSUPPORTED_NUMBER_OF_VALUES,
					parameterName, queryParameters.get(parameterName)));
	}

	@Override
	public boolean isDefined()
	{
		return valueAndType != null;
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		bundleUri.replaceQueryParam(parameterName + valueAndType.type.modifier, valueAndType.value);
	}
}
