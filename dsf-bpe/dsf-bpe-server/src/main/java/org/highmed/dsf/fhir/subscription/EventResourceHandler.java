package org.highmed.dsf.fhir.subscription;

import org.hl7.fhir.r4.model.Resource;

public interface EventResourceHandler<R extends Resource>
{
	void onResource(Resource resource);
}
