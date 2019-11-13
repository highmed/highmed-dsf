package org.highmed.dsf.fhir.search.parameters.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.ws.rs.core.UriBuilder;

import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.search.SearchQueryParameterError.SearchQueryParameterErrorType;
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
		List<String> allValues = new ArrayList<>();
		allValues.addAll(queryParameters.getOrDefault(parameterName + StringSearchType.STARTS_WITH.modifier,
				Collections.emptyList()));
		allValues.addAll(
				queryParameters.getOrDefault(parameterName + StringSearchType.EXACT.modifier, Collections.emptyList()));
		allValues.addAll(queryParameters.getOrDefault(parameterName + StringSearchType.CONTAINS.modifier,
				Collections.emptyList()));
		if (allValues.size() > 1)
			addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNSUPPORTED_NUMBER_OF_VALUES,
					parameterName, allValues));

		String startsWith = getFirst(queryParameters, parameterName + StringSearchType.STARTS_WITH.modifier);
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
