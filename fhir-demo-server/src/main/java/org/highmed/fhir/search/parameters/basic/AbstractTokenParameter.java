package org.highmed.fhir.search.parameters.basic;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.hl7.fhir.r4.model.DomainResource;

public abstract class AbstractTokenParameter<R extends DomainResource> extends AbstractSearchParameter<R>
{
	protected static enum TokenSearchType
	{
		CODE, CODE_AND_SYSTEM, CODE_AND_NO_SYSTEM_PROPERTY, SYSTEM
	}

	protected static class TokenValueAndSearchType
	{
		public final String codeValue;
		public final String systemValue;
		public final TokenSearchType type;

		public TokenValueAndSearchType(String codeValue, String systemValue, TokenSearchType type)
		{
			this.codeValue = codeValue;
			this.systemValue = systemValue;
			this.type = type;
		}
	}

	protected TokenValueAndSearchType valueAndType;

	public AbstractTokenParameter(String parameterName)
	{
		super(parameterName);
	}

	@Override
	protected void configureSearchParameter(MultivaluedMap<String, String> queryParameters)
	{
		String param = queryParameters.getFirst(parameterName);
		if (param != null && !param.isEmpty())
		{
			if (param.indexOf('|') == -1)
				valueAndType = new TokenValueAndSearchType(param, null, TokenSearchType.CODE);
			else if (param.charAt(0) == '|')
				valueAndType = new TokenValueAndSearchType(param.substring(1), null,
						TokenSearchType.CODE_AND_NO_SYSTEM_PROPERTY);
			else if (param.charAt(param.length() - 1) == '|')
				valueAndType = new TokenValueAndSearchType(param.substring(0, param.length() - 1), null,
						TokenSearchType.CODE_AND_NO_SYSTEM_PROPERTY);
			else
			{
				String[] splitAtPipe = param.split("[|]");
				valueAndType = new TokenValueAndSearchType(splitAtPipe[0], splitAtPipe[1],
						TokenSearchType.CODE_AND_SYSTEM);
			}
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
		switch (valueAndType.type)
		{
			case CODE:
				bundleUri.replaceQueryParam(parameterName, valueAndType.codeValue);
				break;

			case CODE_AND_SYSTEM:
				bundleUri.replaceQueryParam(parameterName, valueAndType.systemValue + "|" + valueAndType.codeValue);
				break;

			case CODE_AND_NO_SYSTEM_PROPERTY:
				bundleUri.replaceQueryParam(parameterName, "|" + valueAndType.codeValue);
				break;

			case SYSTEM:
				bundleUri.replaceQueryParam(parameterName, valueAndType + "|");
				break;
		}
	}

	public static void main(String[] args)
	{

	}
}
