package org.highmed.dsf.fhir.spring.config;

import org.highmed.dsf.fhir.websocket.ServerEndpoint;
import org.highmed.dsf.fhir.websocket.ServerEndpointRegistrationForAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.standard.ServerEndpointRegistration;

@Configuration
public class WebsocketConfig
{
	@Autowired
	private EventConfig eventConfig;

	@Bean
	public ServerEndpoint subscriptionEndpoint()
	{
		return new ServerEndpoint(eventConfig.webSocketSubscriptionManager());
	}

	@Bean
	public ServerEndpointRegistration subscriptionEndpointRegistration()
	{
		return new ServerEndpointRegistrationForAuthentication(ServerEndpoint.PATH, subscriptionEndpoint());
	}

	@Bean
	public ServerEndpointExporter endpointExporter()
	{
		return new ServerEndpointExporter();
	}
}
