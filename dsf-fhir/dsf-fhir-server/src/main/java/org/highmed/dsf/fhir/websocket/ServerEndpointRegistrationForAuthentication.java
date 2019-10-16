package org.highmed.dsf.fhir.websocket;

import javax.servlet.http.HttpSession;
import javax.websocket.Endpoint;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.highmed.dsf.fhir.authentication.AuthenticationFilter;
import org.highmed.dsf.fhir.authentication.User;
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
		User user = (User) httpSession.getAttribute(AuthenticationFilter.USER_PROPERTY);

		// don't use ServerEndpointRegistration#getUserProperties()
		sec.getUserProperties().put(ServerEndpoint.USER_PROPERTY, user);
	}
}
