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
	public ServerEndpoint eventEndpoint()
	{
		return new ServerEndpoint(eventConfig.eventManager());
	}

	@Bean
	public ServerEndpointRegistration eventEndpointRegistration()
	{
		return new ServerEndpointRegistrationForAuthentication(ServerEndpoint.PATH, eventEndpoint());
	}

	@Bean
	public ServerEndpointExporter endpointExporter()
	{
		return new ServerEndpointExporter();
	}
}
