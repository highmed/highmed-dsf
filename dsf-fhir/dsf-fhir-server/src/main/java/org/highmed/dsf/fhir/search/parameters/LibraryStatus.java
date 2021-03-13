package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractStatusParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Library;

@SearchParameterDefinition(name = LibraryStatus.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Library-status", type = SearchParamType.TOKEN, documentation = "The current status of the library")
public class LibraryStatus extends AbstractStatusParameter<Library>
{
	public LibraryStatus()
	{
		super("library", Library.class);
	}
}
