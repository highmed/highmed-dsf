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

public abstract class AbstractCanonicalUrlParameter<R extends DomainResource> extends AbstractSearchParameter<R>
{
	public static enum UriSearchType
	{
		PRECISE(""), BELOW(":below"); // TODO, ABOVE(":above");

		public final String modifier;

		private UriSearchType(String modifier)
		{
			this.modifier = modifier;
		}
	}

	protected static class CanonicalUrlAndSearchType
	{
		public final String url;
		public final String version;
		public final UriSearchType type;

		private CanonicalUrlAndSearchType(String url, String version, UriSearchType type)
		{
			this.url = url;
			this.version = version;
			this.type = type;
		}
	}

	protected CanonicalUrlAndSearchType valueAndType;

	public AbstractCanonicalUrlParameter(String parameterName)
	{
		super(parameterName);
	}

	@Override
	protected Stream<String> getModifiedParameterNames()
	{
		return Stream.of(getParameterName() + UriSearchType.BELOW.modifier);
	}

	@Override
	protected final void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		List<String> allValues = new ArrayList<>();
		allValues.addAll(
				queryParameters.getOrDefault(parameterName + UriSearchType.PRECISE.modifier, Collections.emptyList()));
		allValues.addAll(
				queryParameters.getOrDefault(parameterName + UriSearchType.BELOW.modifier, Collections.emptyList()));
		if (allValues.size() > 1)
			addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNSUPPORTED_NUMBER_OF_VALUES,
					parameterName, allValues));

		String precise = getFirst(queryParameters, parameterName + UriSearchType.PRECISE.modifier);
		if (precise != null)
		{
			valueAndType = toValueAndType(precise, UriSearchType.PRECISE);
			return;
		}

		String below = getFirst(queryParameters, parameterName + UriSearchType.BELOW.modifier);
		if (below != null)
		{
			valueAndType = toValueAndType(below, UriSearchType.BELOW);
			return;
		}

		// TODO
		// String above = queryParameters.getFirst(parameterName + UriSearchType.ABOVE.modifier);
		// if (above != null && !above.isBlank())
		// {
		// valueAndType = new UriValueAndSearchType(above, UriSearchType.ABOVE);
		// return;
		// }
	}

	protected static CanonicalUrlAndSearchType toValueAndType(String parameter, UriSearchType type)
	{
		if (parameter != null && !parameter.isBlank())
		{
			String[] split = parameter.split("[|]");
			if (split.length == 1)
				return new CanonicalUrlAndSearchType(split[0], null, type);
			else if (split.length == 2)
				return new CanonicalUrlAndSearchType(split[0], split[1], type);
		}

		return null;
	}

	@Override
	public boolean isDefined()
	{
		return valueAndType != null;
	}

	protected boolean hasVersion()
	{
		return isDefined() && valueAndType.version != null;
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		bundleUri.replaceQueryParam(parameterName + valueAndType.type.modifier,
				valueAndType.url + (hasVersion() ? ("|" + valueAndType.version) : ""));
	}
}
