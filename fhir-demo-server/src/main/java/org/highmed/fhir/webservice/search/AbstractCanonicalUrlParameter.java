package org.highmed.fhir.webservice.search;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

public abstract class AbstractCanonicalUrlParameter implements WsSearchParameter
{
	protected static enum UriSearchType
	{
		PRECISE(""), BELOW(":below"); // TODO, ABOVE(":above");

		public final String sufix;

		private UriSearchType(String sufix)
		{
			this.sufix = sufix;
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

	private final String parameterName;

	protected CanonicalUrlAndSearchType valueAndType;

	public AbstractCanonicalUrlParameter(String parameterName)
	{
		this.parameterName = parameterName;
	}

	public void configure(MultivaluedMap<String, String> queryParameters)
	{
		String precise = queryParameters.getFirst(parameterName);
		if (precise != null && !precise.isBlank())
		{
			String[] preciseSplit = splitAtPipe(precise);
			if (preciseSplit.length == 1)
				valueAndType = new CanonicalUrlAndSearchType(preciseSplit[0], null, UriSearchType.PRECISE);
			else if (preciseSplit.length == 2)
				valueAndType = new CanonicalUrlAndSearchType(preciseSplit[0], preciseSplit[1], UriSearchType.PRECISE);
			return;
		}

		String below = queryParameters.getFirst(parameterName + UriSearchType.BELOW.sufix);
		if (below != null && !below.isBlank())
		{
			String[] belowSplit = splitAtPipe(below);
			if (belowSplit.length == 1)
				valueAndType = new CanonicalUrlAndSearchType(belowSplit[0], null, UriSearchType.BELOW);
			else if (belowSplit.length == 2)
				valueAndType = new CanonicalUrlAndSearchType(belowSplit[0], belowSplit[1], UriSearchType.BELOW);
			return;
		}

		// TODO
		// String above = queryParameters.getFirst(parameterName + UriSearchType.ABOVE.sufix);
		// if (above != null && !above.isBlank())
		// {
		// valueAndType = new UriValueAndSearchType(above, UriSearchType.ABOVE);
		// return;
		// }
	}

	private String[] splitAtPipe(String value)
	{
		return value.split("[|]");
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
		bundleUri.replaceQueryParam(parameterName + valueAndType.type.sufix,
				valueAndType.url + (hasVersion() ? ("|" + valueAndType.version) : ""));
	}
}
