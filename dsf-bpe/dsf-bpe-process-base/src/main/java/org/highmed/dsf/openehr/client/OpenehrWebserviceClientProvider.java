package org.highmed.dsf.openehr.client;

import org.highmed.openehr.client.OpenehrWebserviceClient;

public interface OpenehrWebserviceClientProvider
{
	String getBaseUrl();

	OpenehrWebserviceClient getWebserviceClient();
}
