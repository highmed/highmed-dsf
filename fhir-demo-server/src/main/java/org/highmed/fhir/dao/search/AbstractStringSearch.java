package org.highmed.fhir.dao.search;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

public abstract class AbstractStringSearch implements SearchParameter
{
	protected enum StringSearchType
	{
		STARTS_WITH, EXACT, CONTAINS
	}

	private final String parameterName;

	protected String value;
	protected StringSearchType type;

	public AbstractStringSearch(String parameterName)
	{
		this.parameterName = parameterName;
	}

	public void configure(MultivaluedMap<String, String> queryParameters)
	{
		String startsWith = queryParameters.getFirst(parameterName);
		if (startsWith != null && !startsWith.isBlank())
		{
			this.value = startsWith;
			this.type = StringSearchType.STARTS_WITH;
		}
		String exact = queryParameters.getFirst(parameterName + ":exact");
		if (exact != null && !exact.isBlank())
		{
			this.value = exact;
			this.type = StringSearchType.EXACT;
		}
		String contains = queryParameters.getFirst(parameterName + ":contains");
		if (contains != null && !contains.isBlank())
		{
			this.value = contains;
			this.type = StringSearchType.CONTAINS;
		}
	}

	@Override
	public boolean isDefined()
	{
		return value != null && type != null;
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		if (value != null && type != null)
			switch (type)
			{
				case STARTS_WITH:
					bundleUri = bundleUri.replaceQueryParam(parameterName, value);
					return;
				case CONTAINS:
					bundleUri = bundleUri.replaceQueryParam(parameterName + ":contains", value);
					return;
				case EXACT:
					bundleUri = bundleUri.replaceQueryParam(parameterName + ":exact", value);
					return;
			}
	}
}
