package org.highmed.fhir.websocket;

import java.util.Objects;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.highmed.fhir.event.EventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class EventEndpoint extends Endpoint implements InitializingBean
{
	public static final String PATH = "/ws";
	public static final String ORGANIZATION_PROPERTY = EventEndpoint.class.getName() + ".organization";

	private static final Logger logger = LoggerFactory.getLogger(EventEndpoint.class);

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

		session.getUserProperties().get(ORGANIZATION_PROPERTY);
		// if (user == null || !UserRole.userHasOneOfRoles(user, UserRole.WEBSOCKET, UserRole.WEBSOCKET_AND_WEBSERVICE))
		// {
		// logger.warn("No user in session or user has not one of roles {{}, {}}, closing websocket",
		// UserRole.WEBSOCKET.getValue(), UserRole.WEBSOCKET_AND_WEBSERVICE.getValue());
		// try
		// {
		// session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, user == null ? "No User" : "Forbidden"));
		// }
		// catch (IOException e)
		// {
		// logger.warn("Error while closing websocket", e);
		// }
		//
		// return;
		// }

		// EventHandler<E> handler = new EventHandler<E>()
		// {
		// @Override
		// public void handleEvent(E event)
		// {
		// if (!eventFilter.test(session.getRequestParameterMap(), event))
		// {
		// logger.debug("{} event filtered", event.getClass().getSimpleName());
		// return;
		// }
		//
		// try
		// {
		// if (session.isOpen())
		// {
		// logger.debug("Sending {} to user {}", event.getClass().getSimpleName(),
		// (user != null ? user.getSubjectDn() : "?"));
		// session.getAsyncRemote().sendText(objectMapper.writeValueAsString(event));
		// }
		// else
		// logger.warn("Session closed, can't send message");
		// }
		// catch (JsonProcessingException e)
		// {
		// logger.error("Error while converting object of type " + event.getClass().getName() + " to json", e);
		// }
		// }
		// };

		// logger.info("Websocket connection for user {} opend, adding event handler",
		// (user != null ? user.getSubjectDn() : "?"));
		//
		// eventHandlers.put(session, handler);
		// eventManager.addEventHandler(user, handler);
	}

	@Override
	public void onClose(Session session, CloseReason closeReason)
	{
		logger.trace("onClose");

		// User user = (User) session.getUserProperties().get(USER_PROPERTY);
		// logger.warn("Websocket connection for user {} closed", (user != null ? user.getSubjectDn() : "?"));

		// if (user != null)
		// {
		// EventHandler<E> handler = eventHandlers.remove(session);
		// eventManager.removeEventHandler(user, handler);
		// }
	}

	@Override
	public void onError(Session session, Throwable thr)
	{
		logger.trace("onError");

		// User user = (User) session.getUserProperties().get(USER_PROPERTY);
		//
		// if (thr != null && thr instanceof SocketTimeoutException)
		// logger.warn("Error in websocket connection for user " + (user != null ? user.getSubjectDn() : "?") + ": "
		// + thr.getMessage());
		// else
		// logger.warn("Error in websocket connection for user " + (user != null ? user.getSubjectDn() : "?"), thr);
		//
		// if (user != null)
		// {
		// EventHandler<E> handler = eventHandlers.remove(session);
		// eventManager.removeEventHandler(user, handler);
		// }
	}
}