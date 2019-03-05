package org.highmed.fhir.search.parameters.basic;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.hl7.fhir.r4.model.DomainResource;

public abstract class AbstractReferenceParameter<R extends DomainResource> extends AbstractSearchParameter<R>
{
	protected enum ReferenceSearchType
	{
		ID, RESOURCE_NAME_AND_ID, URL
	}

	protected class ReferenceValueAndSearchType
	{
		public final String resourceName;
		public final String id;
		public final String url;

		public final ReferenceSearchType type;

		ReferenceValueAndSearchType(String resourceName, String id, String url, ReferenceSearchType type)
		{
			this.resourceName = resourceName;
			this.id = id;
			this.url = url;
			this.type = type;
		}
	}

	private final String[] resourceNames;

	protected ReferenceValueAndSearchType valueAndType;

	public AbstractReferenceParameter(String parameterName, String... resourceNames)
	{
		super(parameterName);
		this.resourceNames = resourceNames;
	}

	@Override
	protected void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		String param = getFirst(queryParameters, parameterName);
		if (param != null && !param.isEmpty())
		{
			if (param.indexOf('/') == -1 && resourceNames.length == 1)
				valueAndType = new ReferenceValueAndSearchType(null, param, null, ReferenceSearchType.ID);
			else if (param.indexOf('/') >= 0)
			{
				String[] splitAtSlash = param.split("/");
				if (splitAtSlash.length == 2
						&& Arrays.stream(resourceNames).map(n -> n.equals(splitAtSlash[0])).anyMatch(b -> b))
					valueAndType = new ReferenceValueAndSearchType(splitAtSlash[0], splitAtSlash[1], null,
							ReferenceSearchType.RESOURCE_NAME_AND_ID);
			}
			else if (param.startsWith("http")
					&& Arrays.stream(resourceNames).map(n -> param.contains("/" + n + "/")).anyMatch(b -> b))
				valueAndType = new ReferenceValueAndSearchType(null, null, param, ReferenceSearchType.URL);
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
			case ID:
			case URL:
				bundleUri.replaceQueryParam(parameterName, valueAndType.id);
				break;

			case RESOURCE_NAME_AND_ID:
				bundleUri.replaceQueryParam(parameterName, valueAndType.resourceName + "/" + valueAndType.id);
				break;
		}
	}

	public static void main(String[] args)
	{
		String param = "http://foo.bar/baz/Patient/1234";
		String[] resourceNames = { "Patient" };
		System.out.println(Arrays.stream(resourceNames).map(n -> param.contains(n)).anyMatch(b -> b));

	}
}
