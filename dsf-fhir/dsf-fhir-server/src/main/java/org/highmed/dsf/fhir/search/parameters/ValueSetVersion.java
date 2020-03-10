package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractVersionParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ValueSet;

@SearchParameterDefinition(name = ValueSetVersion.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/ValueSet-version", type = SearchParamType.TOKEN, documentation = "The business version of the value set")
public class ValueSetVersion extends AbstractVersionParameter<ValueSet>
{
	public static final String RESOURCE_COLUMN = "value_set";

	public ValueSetVersion()
	{
		this(RESOURCE_COLUMN);
	}

	public ValueSetVersion(String resourceColumn)
	{
		super(resourceColumn);
	}

	@Override
	protected boolean instanceOf(Resource resource)
	{
		return resource instanceof ValueSet;
	}
}
