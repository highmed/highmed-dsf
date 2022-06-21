package org.highmed.fhir.client;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.hl7.fhir.r4.model.DomainResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.parser.IParser;

public class ClientEndpoint extends Endpoint
{
	private static final Logger logger = LoggerFactory.getLogger(ClientEndpoint.class);

	private final Runnable reconnector;
	private final String subscriptionIdPart;

	public ClientEndpoint(Runnable reconnector, String subscriptionIdPart)
	{
		this.reconnector = reconnector;
		this.subscriptionIdPart = subscriptionIdPart;
	}

	private Supplier<IParser> parserFactory;
	private Consumer<DomainResource> domainResourceHandler;
	private Consumer<String> pingHandler;

	@Override
	public void onOpen(Session session, EndpointConfig config)
	{
		logger.info("Websocket connected {uri: {}, session-id: {}}", session.getRequestURI().toString(),
				session.getId());

		session.addMessageHandler(new MessageHandler.Whole<String>() // don't use lambda
		{
			private boolean boundReceived;

			@Override
			public void onMessage(String message)
			{
				logger.debug("onMessage {}", message);

				if (("bound " + subscriptionIdPart).equals(message))
				{
					logger.debug("Bound received");
					boundReceived = true;
					return;
				}

				if (boundReceived)
				{
					try
					{
						if (pingHandler != null && ("ping " + subscriptionIdPart).equals(message))
							pingHandler.accept(message);
						else if (domainResourceHandler != null && parserFactory != null)
							domainResourceHandler.accept((DomainResource) parserFactory.get().parseResource(message));
					}
					catch (Throwable e)
					{
						logger.error("Error while handling message, caught {}: {}", e.getClass().getName(),
								e.getMessage());
					}
				}
			}
		});

		session.getAsyncRemote().sendText("bind " + subscriptionIdPart);
	}

	@Override
	public void onClose(Session session, CloseReason closeReason)
	{
		logger.info("Websocket closed {uri: {}, session-id: {}}: {}", session.getRequestURI().toString(),
				session.getId(), closeReason.getReasonPhrase());

		if (CloseReason.CloseCodes.CANNOT_ACCEPT.equals(closeReason.getCloseCode()))
		{
			logger.info("Trying to reconnect websocket");
			reconnector.run();
		}
	}

	@Override
	public void onError(Session session, Throwable throwable)
	{
		logger.warn("Websocket closed with error {uri: " + session.getRequestURI().toString() + ", session-id: "
				+ session.getId() + "}: {}", throwable);
	}

	public void setDomainResourceHandler(Consumer<DomainResource> handler, Supplier<IParser> parser)
	{
		domainResourceHandler = handler;
		parserFactory = parser;
		pingHandler = null;
	}

	public void setPingHandler(Consumer<String> handler)
	{
		domainResourceHandler = null;
		parserFactory = null;
		pingHandler = handler;
	}
}
