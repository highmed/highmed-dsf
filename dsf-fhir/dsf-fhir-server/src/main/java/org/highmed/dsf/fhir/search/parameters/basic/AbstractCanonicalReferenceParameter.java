package org.highmed.dsf.fhir.search.parameters.basic;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.search.SearchQueryParameterError.SearchQueryParameterErrorType;
import org.hl7.fhir.r4.model.DomainResource;

public abstract class AbstractCanonicalReferenceParameter<R extends DomainResource>
		extends AbstractReferenceParameter<R>
{
	public AbstractCanonicalReferenceParameter(Class<R> resourceType, String resourceTypeName, String parameterName,
			String... targetResourceTypeNames)
	{
		super(resourceType, resourceTypeName, parameterName, targetResourceTypeNames);
	}

	@Override
	protected void configureSearchParameter(Map<String, List<String>> queryParameters)
	{
		super.configureSearchParameter(queryParameters);

		if (valueAndType != null && valueAndType.type != null)
			switch (valueAndType.type)
			{
				// only URL supported for canonical
				case URL:
					return;

				case ID:
				case TYPE_AND_ID:
					addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE,
							parameterName, Collections.singletonList(valueAndType.id)));
					return;
				case IDENTIFIER:
				{
					if (valueAndType.identifier != null && valueAndType.identifier.type != null)
						switch (valueAndType.identifier.type)
						{
							case CODE:
								addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE,
										parameterName, Collections.singletonList(valueAndType.identifier.codeValue)));
								return;
							case CODE_AND_NO_SYSTEM_PROPERTY:
								addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE,
										parameterName,
										Collections.singletonList("|" + valueAndType.identifier.codeValue)));
								return;
							case CODE_AND_SYSTEM:
								addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE,
										parameterName, Collections.singletonList(valueAndType.identifier.systemValue
												+ "|" + valueAndType.identifier.codeValue)));
								return;
							case SYSTEM:
								addError(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE,
										parameterName,
										Collections.singletonList(valueAndType.identifier.systemValue + "|")));
								return;
							default:
								return;
						}
					return;
				}
				case RESOURCE_NAME_AND_ID:
				case TYPE_AND_RESOURCE_NAME_AND_ID:
					addError(
							new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE, parameterName,
									Collections.singletonList(valueAndType.resourceName + "/" + valueAndType.id)));
					return;
				default:
					return;
			}
	}
}
