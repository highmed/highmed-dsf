package org.highmed.fhir.client;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler.Whole;
import javax.websocket.Session;

import org.hl7.fhir.r4.model.DomainResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.parser.IParser;

public class WebsocketEndpoint extends Endpoint
{
	private static final Logger logger = LoggerFactory.getLogger(WebsocketEndpoint.class);

	private final String subscriptionIdPart;

	public WebsocketEndpoint(String subscriptionIdPart)
	{
		this.subscriptionIdPart = subscriptionIdPart;
	}

	private Consumer<DomainResource> domainResourceHandler;
	private Supplier<IParser> parserFactory;
	private Consumer<String> pingHandler;

	@Override
	public void onOpen(Session session, EndpointConfig config)
	{
		logger.debug("Websocket onOpen");

		// don't use lambda expression for handler
		session.addMessageHandler(new Whole<String>()
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
				}

				if (boundReceived)
				{
					if (pingHandler != null)
						pingHandler.accept(message);
					else if (domainResourceHandler != null && parserFactory != null)
						domainResourceHandler.accept((DomainResource) parserFactory.get().parseResource(message));
				}
			}
		});
	}

	@Override
	public void onClose(Session session, CloseReason closeReason)
	{
		logger.info("Websocket onClose {}", closeReason.getReasonPhrase());
	}

	@Override
	public void onError(Session session, Throwable throwable)
	{
		logger.warn("Websocket onError", throwable);
	}

	public void setDomainResourceHandler(Consumer<DomainResource> handler, Supplier<IParser> parserFactory)
	{
		domainResourceHandler = handler;
		this.parserFactory = parserFactory;
		pingHandler = null;
	}

	public void setPingHandler(Consumer<String> handler)
	{
		domainResourceHandler = null;
		parserFactory = null;
		pingHandler = handler;
	}
}
