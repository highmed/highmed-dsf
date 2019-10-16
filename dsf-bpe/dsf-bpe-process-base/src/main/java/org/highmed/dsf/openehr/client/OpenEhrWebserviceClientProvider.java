package org.highmed.dsf.openehr.client;

import org.highmed.openehr.client.OpenehrWebserviceClient;

public interface OpenEhrWebserviceClientProvider
{
	String getBaseUrl();

	OpenehrWebserviceClient getWebserviceClient();
}
