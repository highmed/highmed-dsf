package org.highmed.fhir.search.parameters.basic;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.hl7.fhir.r4.model.DomainResource;

public abstract class AbstractTokenParameter<R extends DomainResource> extends AbstractSearchParameter<R>
{
	protected TokenValueAndSearchType valueAndType;

	public AbstractTokenParameter(String parameterName)
	{
		super(parameterName);
	}

	@Override
	protected void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		String param = getFirst(queryParameters, parameterName);
		valueAndType = TokenValueAndSearchType.fromParamValue(param).orElse(null);
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
				bundleUri.replaceQueryParam(parameterName, valueAndType.systemValue + "|");
				break;
		}
	}
}
