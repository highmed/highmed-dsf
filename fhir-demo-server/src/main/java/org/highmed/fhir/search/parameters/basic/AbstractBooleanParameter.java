package org.highmed.fhir.search.parameters.basic;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.hl7.fhir.r4.model.DomainResource;

public abstract class AbstractBooleanParameter<R extends DomainResource> extends AbstractSearchParameter<R>
{
	protected Boolean value;

	public AbstractBooleanParameter(String parameterName)
	{
		super(parameterName);
	}

	@Override
	protected void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		String param = getFirst(queryParameters, parameterName);
		if (param != null && !param.isEmpty())
		{
			switch (param)
			{
				case "true":
					value = true;
					break;
				case "false":
					value = false;
					break;
				default:
					value = null;
					break;
			}
		}
	}

	@Override
	public boolean isDefined()
	{
		return value != null;
	}

	@Override
	public void modifyBundleUri(UriBuilder bundleUri)
	{
		if (isDefined())
			bundleUri.replaceQueryParam(parameterName, String.valueOf(value));
	}
}
