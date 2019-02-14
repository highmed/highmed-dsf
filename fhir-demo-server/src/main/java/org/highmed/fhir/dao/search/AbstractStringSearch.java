package org.highmed.fhir.dao.search;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

public abstract class AbstractStringSearch implements SearchParameter
{
	protected static enum StringSearchType
	{
		STARTS_WITH(""), EXACT(":exact"), CONTAINS(":contains");

		final String sufix;

		StringSearchType(String sufix)
		{
			this.sufix = sufix;
		}
	}

	protected static class StringValueAndSearchType
	{
		final String value;
		final StringSearchType type;

		StringValueAndSearchType(String value, StringSearchType type)
		{
			this.value = value;
			this.type = type;
		}
	}

	private final String parameterName;

	protected StringValueAndSearchType valueAndType;

	public AbstractStringSearch(String parameterName)
	{
		this.parameterName = parameterName;
	}

	public void configure(MultivaluedMap<String, String> queryParameters)
	{
		String startsWith = queryParameters.getFirst(parameterName);
		if (startsWith != null && !startsWith.isBlank())
		{
			valueAndType = new StringValueAndSearchType(startsWith, StringSearchType.STARTS_WITH);
			return;
		}

		String exact = queryParameters.getFirst(parameterName + StringSearchType.EXACT.sufix);
		if (exact != null && !exact.isBlank())
		{
			valueAndType = new StringValueAndSearchType(exact, StringSearchType.EXACT);
			return;
		}

		String contains = queryParameters.getFirst(parameterName + StringSearchType.CONTAINS.sufix);
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
		if (valueAndType != null)
			bundleUri = bundleUri.replaceQueryParam(parameterName + valueAndType.type.sufix, valueAndType.value);
	}
}
