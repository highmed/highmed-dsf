package org.highmed.dsf.fhir.websocket;

import org.hl7.fhir.r4.model.Resource;

public interface ResourceHandler<R extends Resource>
{
	void onResource(R resource);
}
