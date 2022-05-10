package org.highmed.dsf.fhir.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler.Whole;
import javax.websocket.PongMessage;
import javax.websocket.Session;

import org.apache.commons.codec.binary.Hex;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.highmed.dsf.fhir.subscription.WebSocketSubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class ServerEndpoint extends Endpoint implements InitializingBean, DisposableBean
{
	private static final Logger logger = LoggerFactory.getLogger(ServerEndpoint.class);

	public static final String PATH = "/ws";
	public static final String USER_PROPERTY = ServerEndpoint.class.getName() + ".user";
	private static final String PINGER_PROPERTY = ServerEndpoint.class.getName() + ".pinger";
	private static final String BIND_MESSAGE_START = "bind ";

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

	private final WebSocketSubscriptionManager subscriptionManager;

	public ServerEndpoint(WebSocketSubscriptionManager subscriptionManager)
	{
		this.subscriptionManager = subscriptionManager;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(subscriptionManager, "subscriptionManager");
	}

	@Override
	public void onOpen(Session session, EndpointConfig config)
	{
		logger.debug("onOpen session: {}", session.getId());

		User user = getUser(session);
		if (user == null || !UserRole.userHasOneOfRoles(user, UserRole.LOCAL))
		{
			logger.warn("No user in session or user is missing role {}, closing websocket: {}", UserRole.LOCAL,
					session.getId());
			try
			{
				session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, user == null ? "No user" : "Forbidden"));
			}
			catch (IOException e)
			{
				logger.warn("Error while closing websocket", e);
			}

			return;
		}

		session.addMessageHandler(new Whole<String>() // don't use lambda
		{
			@Override
			public void onMessage(String message)
			{
				logger.debug("onMessage session: {}, message: {}", session.getId(), message);

				if (message != null && !message.isBlank() && message.startsWith(BIND_MESSAGE_START))
				{
					logger.debug("Websocket bind message received: {}", message);
					subscriptionManager.bind(user, session, message.substring(BIND_MESSAGE_START.length()));
				}
			}
		});

		ScheduledFuture<?> pinger = scheduler.scheduleWithFixedDelay(() -> ping(session), 2, 2, TimeUnit.MINUTES);
		session.getUserProperties().put(PINGER_PROPERTY, pinger);
	}

	private void ping(Session session)
	{
		byte[] send = new byte[32];
		ThreadLocalRandom.current().nextBytes(send);

		session.addMessageHandler(new Whole<PongMessage>()
		{
			@Override
			public void onMessage(PongMessage message)
			{
				byte[] read = new byte[32];
				message.getApplicationData().get(read);
				logger.trace("onPongMessage {} from session {}", Hex.encodeHexString(read), session.getId());

				if (!Arrays.equals(send, read))
					logger.warn("ping data not equal to pong data {} != {}", Hex.encodeHexString(send),
							Hex.encodeHexString(read));

				session.removeMessageHandler(this);
			}
		});

		try
		{
			logger.trace("sending ping {} to session {}", Hex.encodeHexString(send), session.getId());
			session.getAsyncRemote().sendPing(ByteBuffer.wrap(send));
		}
		catch (IllegalArgumentException | IOException e)
		{
			logger.warn("Error while sending ping to session with id " + session.getId(), e);
		}
	}

	private User getUser(Session session)
	{
		Object object = session.getUserProperties().get(USER_PROPERTY);
		if (object != null && object instanceof User)
			return (User) object;
		else
		{
			logger.warn("User property {} not a {}", USER_PROPERTY, User.class.getName());
			return null;
		}
	}

	@Override
	public void onClose(Session session, CloseReason closeReason)
	{
		logger.debug("onClose " + session.getId());
		subscriptionManager.close(session.getId());

		ScheduledFuture<?> pinger = (ScheduledFuture<?>) session.getUserProperties().get(PINGER_PROPERTY);
		if (pinger != null)
			pinger.cancel(true);
	}

	@Override
	public void onError(Session session, Throwable thr)
	{
		logger.info("onError {} - {}", session.getId(),
				thr != null ? (thr.getClass().getName() + ": " + thr.getMessage()) : "");
	}

	@Override
	public void destroy() throws Exception
	{
		scheduler.shutdown();
		try
		{
			if (!scheduler.awaitTermination(60, TimeUnit.SECONDS))
			{
				scheduler.shutdownNow();
				if (!scheduler.awaitTermination(60, TimeUnit.SECONDS))
					logger.warn("EventEndpoint scheduler did not terminate");
			}
		}
		catch (InterruptedException ie)
		{
			scheduler.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}