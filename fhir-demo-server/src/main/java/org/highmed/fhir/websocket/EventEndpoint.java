package org.highmed.fhir.websocket;

import java.io.IOException;
import java.util.Objects;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.highmed.fhir.event.EventManager;
import org.hl7.fhir.r4.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class EventEndpoint extends Endpoint implements InitializingBean
{
	public static final String PATH = "/ws";
	public static final String ORGANIZATION_PROPERTY = EventEndpoint.class.getName() + ".organization";

	private static final Logger logger = LoggerFactory.getLogger(EventEndpoint.class);

	private static final String BIND_MESSAGE_START = "bind ";

	private final EventManager eventManager;

	public EventEndpoint(EventManager eventManager)
	{
		this.eventManager = eventManager;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(eventManager, "eventManager");
	}

	@Override
	public void onOpen(Session session, EndpointConfig config)
	{
		logger.trace("onOpen");

		Organization organization = getOrganization(session);
		// TODO check role || !UserRole.userHasOneOfRoles(user, UserRole.WEBSOCKET, UserRole.WEBSOCKET_AND_WEBSERVICE)
		if (organization == null)
		{
			logger.warn("No organization in session");
			// TODO
			// or organization has not one of roles {{}, {}}, closing websocket",
			// UserRole.WEBSOCKET.getValue(), UserRole.WEBSOCKET_AND_WEBSERVICE.getValue());
			try
			{
				session.close(new CloseReason(CloseCodes.VIOLATED_POLICY,
						organization == null ? "No organization" : "Forbidden"));
			}
			catch (IOException e)
			{
				logger.warn("Error while closing websocket", e);
			}

			return;
		}

		// don't use lambda
		session.addMessageHandler(new MessageHandler.Whole<String>()
		{
			@Override
			public void onMessage(String message)
			{
				if (message != null && !message.isBlank() && message.startsWith(BIND_MESSAGE_START))
					eventManager.bind(message.substring(BIND_MESSAGE_START.length()), session.getAsyncRemote());
			}
		});
	}

	private Organization getOrganization(Session session)
	{
		Object object = session.getUserProperties().get(ORGANIZATION_PROPERTY);
		if (object != null && object instanceof Organization)
			return (Organization) object;
		else
		{
			logger.warn("User property {} not a {}", ORGANIZATION_PROPERTY, Organization.class.getName());
			return null;
		}
	}

	@Override
	public void onClose(Session session, CloseReason closeReason)
	{
		logger.trace("onClose");
		eventManager.close(session.getAsyncRemote());
	}

	@Override
	public void onError(Session session, Throwable thr)
	{
		logger.trace("onError");
		eventManager.close(session.getAsyncRemote());
	}
}