package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.ActivityDefinitionService;
import org.hl7.fhir.r4.model.ActivityDefinition;

@Path(ActivityDefinitionServiceJaxrs.PATH)
public class ActivityDefinitionServiceJaxrs extends
		AbstractResourceServiceJaxrs<ActivityDefinition, ActivityDefinitionService> implements ActivityDefinitionService
{
	public static final String PATH = "ActivityDefinition";

	public ActivityDefinitionServiceJaxrs(ActivityDefinitionService delegate)
	{
		super(delegate);
	}
}
