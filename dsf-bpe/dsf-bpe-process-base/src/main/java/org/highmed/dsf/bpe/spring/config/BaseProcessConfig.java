package org.highmed.dsf.bpe.spring.config;

import org.highmed.dsf.bpe.listener.DefaultBpmnParseListener;
import org.highmed.dsf.bpe.listener.EndListener;
import org.highmed.dsf.bpe.listener.StartListener;
import org.highmed.dsf.fhir.client.WebserviceClientProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaseProcessConfig
{
	@Autowired
	private WebserviceClientProvider clientProvider;

	public StartListener startListener() {
		return new StartListener(clientProvider.getLocalWebserviceClient());
	}

	public EndListener endListener() {
		return new EndListener(clientProvider.getLocalWebserviceClient());
	}

	public DefaultBpmnParseListener defaultBpmnParseListener() {
		return new DefaultBpmnParseListener(startListener(), endListener());
	}
}
