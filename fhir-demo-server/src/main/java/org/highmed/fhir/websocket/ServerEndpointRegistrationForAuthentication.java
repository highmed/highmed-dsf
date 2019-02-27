package org.highmed.fhir.websocket;

import javax.servlet.http.HttpSession;
import javax.websocket.Endpoint;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.highmed.fhir.authentication.AuthenticationFilter;
import org.hl7.fhir.r4.model.Organization;
import org.springframework.web.socket.server.standard.ServerEndpointRegistration;

public class ServerEndpointRegistrationForAuthentication extends ServerEndpointRegistration
{
	public ServerEndpointRegistrationForAuthentication(String path, Endpoint endpoint)
	{
		super(path, endpoint);
	}

	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response)
	{
		HttpSession httpSession = (HttpSession) request.getHttpSession();
		Organization organization = (Organization) httpSession.getAttribute(AuthenticationFilter.ORGANIZATION_PROPERTY);

		// don't use ServerEndpointRegistration#getUserProperties()
		sec.getUserProperties().put(ServerEndpoint.ORGANIZATION_PROPERTY, organization);
	}
}
