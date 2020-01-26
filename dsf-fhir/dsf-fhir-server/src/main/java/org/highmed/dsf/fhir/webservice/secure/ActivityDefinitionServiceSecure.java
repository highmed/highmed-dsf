package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.ActivityDefinitionService;
import org.hl7.fhir.r4.model.ActivityDefinition;

public class ActivityDefinitionServiceSecure extends
		AbstractServiceSecure<ActivityDefinition, ActivityDefinitionService> implements ActivityDefinitionService
{
	public ActivityDefinitionServiceSecure(ActivityDefinitionService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}
