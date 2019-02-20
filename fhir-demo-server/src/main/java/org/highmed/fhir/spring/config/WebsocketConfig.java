package org.highmed.fhir.spring.config;

import org.highmed.fhir.websocket.EventEndpoint;
import org.highmed.fhir.websocket.ServerEndpointRegistrationForAuthentication;
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
	public EventEndpoint eventEndpoint()
	{
		return new EventEndpoint(eventConfig.eventManager());
	}

	@Bean
	public ServerEndpointRegistration eventEndpointRegistration()
	{
		return new ServerEndpointRegistrationForAuthentication(EventEndpoint.PATH, eventEndpoint());
	}

	@Bean
	public ServerEndpointExporter endpointExporter()
	{
		return new ServerEndpointExporter();
	}
}
