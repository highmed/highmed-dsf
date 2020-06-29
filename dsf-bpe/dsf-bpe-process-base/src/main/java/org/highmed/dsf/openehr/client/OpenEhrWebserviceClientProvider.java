package org.highmed.dsf.openehr.client;

import org.highmed.openehr.client.OpenEhrWebserviceClient;

public interface OpenEhrWebserviceClientProvider
{
	String getBaseUrl();

	OpenEhrWebserviceClient getWebserviceClient();
}
