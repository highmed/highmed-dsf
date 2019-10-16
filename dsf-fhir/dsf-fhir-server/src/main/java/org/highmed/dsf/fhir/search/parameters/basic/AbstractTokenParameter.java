package org.highmed.dsf.fhir.search.parameters.basic;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.search.SearchQueryParameterError.SearchQueryParameterErrorType;
import org.hl7.fhir.r4.model.Resource;

public abstract class AbstractTokenParameter<R extends Resource> extends AbstractSearchParameter<R>
{
	protected TokenValueAndSearchType valueAndType;

	public AbstractTokenParameter(String parameterName)
	{
		super(parameterName);
	}

	@Override
	protected void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		valueAndType = TokenValueAndSearchType.fromParamValue(parameterName, queryParameters, this::addError)
				.orElse(null);

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
