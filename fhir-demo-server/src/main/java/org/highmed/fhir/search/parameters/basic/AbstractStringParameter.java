package org.highmed.fhir.search.parameters.basic;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.hl7.fhir.r4.model.DomainResource;

public abstract class AbstractStringParameter<R extends DomainResource> extends AbstractSearchParameter<R>
{
	public static enum StringSearchType
	{
		STARTS_WITH(""), EXACT(":exact"), CONTAINS(":contains");

		public final String sufix;

		private StringSearchType(String sufix)
		{
			this.sufix = sufix;
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
	protected final void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		String startsWith = getFirst(queryParameters, parameterName);
		if (startsWith != null && !startsWith.isBlank())
		{
			valueAndType = new StringValueAndSearchType(startsWith, StringSearchType.STARTS_WITH);
			return;
		}

		String exact = getFirst(queryParameters, parameterName + StringSearchType.EXACT.sufix);
		if (exact != null && !exact.isBlank())
		{
			valueAndType = new StringValueAndSearchType(exact, StringSearchType.EXACT);
			return;
		}

		String contains = getFirst(queryParameters, parameterName + StringSearchType.CONTAINS.sufix);
		if (contains != null && !contains.isBlank())
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
		bundleUri.replaceQueryParam(parameterName + valueAndType.type.sufix, valueAndType.value);
	}
}
