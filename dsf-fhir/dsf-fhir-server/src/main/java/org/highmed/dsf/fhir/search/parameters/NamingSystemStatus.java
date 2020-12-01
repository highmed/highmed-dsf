package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractStatusParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.NamingSystem;

@SearchParameterDefinition(name = NamingSystemStatus.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/NamingSystem-status", type = SearchParamType.TOKEN, documentation = "The current status of the naming system")
public class NamingSystemStatus extends AbstractStatusParameter<NamingSystem>
{
	public NamingSystemStatus()
	{
		super("naming_system", NamingSystem.class);
	}
}
