package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractIdentifierParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = AbstractIdentifierParameter.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Library-identifier", type = SearchParamType.TOKEN, documentation = "External identifier for the library")
public class LibraryIdentifier extends AbstractIdentifierParameter<Library>
{
	public static final String RESOURCE_COLUMN = "library";

	public LibraryIdentifier()
	{
		super(RESOURCE_COLUMN);
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Library))
			return false;

		Library l = (Library) resource;

		return identifierMatches(l.getIdentifier());
	}
}
