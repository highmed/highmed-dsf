package org.highmed.dsf.fhir.event;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.websocket.RemoteEndpoint.Async;

import org.highmed.dsf.fhir.dao.SubscriptionDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.search.Matcher;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.rest.api.Constants;

public class EventManagerImpl implements EventManager, InitializingBean, DisposableBean
{
	private static final Logger logger = LoggerFactory.getLogger(EventManagerImpl.class);

	private static class SubscriptionAndMatcher
	{
		final Subscription subscription;
		final Matcher matcher;

		SubscriptionAndMatcher(Subscription subscription, Matcher matcher)
		{
			this.subscription = subscription;
			this.matcher = matcher;
		}

		boolean matches(Resource resource, DaoProvider daoProvider)
		{
			try
			{
				matcher.resloveReferencesForMatching(resource, daoProvider);
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}

			return matcher.matches(resource);
		}
	}

	private static class SessionIdAndRemoteAsync
	{
		final String sessionId;
		final Async remoteAsync;

		SessionIdAndRemoteAsync(String sessionId, Async remoteAsync)
		{
			this.sessionId = sessionId;
			this.remoteAsync = remoteAsync;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SessionIdAndRemoteAsync other = (SessionIdAndRemoteAsync) obj;
			if (sessionId == null)
			{
				if (other.sessionId != null)
					return false;
			}
			else if (!sessionId.equals(other.sessionId))
				return false;
			return true;
		}
	}

	private final ExecutorService executor = Executors.newCachedThreadPool();

	private final DaoProvider daoProvider;
	private final SubscriptionDao subscriptionDao;
	private final ExceptionHandler exceptionHandler;
	private final MatcherFactory matcherFactory;
	private final FhirContext fhirContext;

	private final AtomicBoolean firstCall = new AtomicBoolean(true);
	private final ReadWriteMap<String, Subscription> subscriptionsByIdPart = new ReadWriteMap<>();
	private final ReadWriteMap<Class<? extends Resource>, List<SubscriptionAndMatcher>> matchersByResource = new ReadWriteMap<>();
	private final ReadWriteMap<String, List<SessionIdAndRemoteAsync>> asyncRemotesBySubscriptionIdPart = new ReadWriteMap<>();

	public EventManagerImpl(DaoProvider daoProvider, ExceptionHandler exceptionHandler, MatcherFactory matcherFactory,
			FhirContext fhirContext)
	{
		this.daoProvider = daoProvider;
		this.subscriptionDao = daoProvider.getSubscriptionDao();
		this.exceptionHandler = exceptionHandler;
		this.matcherFactory = matcherFactory;
		this.fhirContext = fhirContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(subscriptionDao, "subscriptionDao");
		Objects.requireNonNull(exceptionHandler, "exceptionHandler");
		Objects.requireNonNull(matcherFactory, "matcherFactory");
		Objects.requireNonNull(fhirContext, "fhirContext");
		Objects.requireNonNull(daoProvider, "daoProvider");
	}

	private void refreshMatchers()
	{
		logger.info("Refreshing subscriptions");
		firstCall.set(false);

		try
		{
			List<Subscription> subscriptions = subscriptionDao.readByStatus(SubscriptionStatus.ACTIVE);
			Map<Class<? extends Resource>, List<SubscriptionAndMatcher>> matchers = new HashMap<>();
			for (Subscription subscription : subscriptions)
			{
				Optional<Matcher> matcher = matcherFactory.createMatcher(subscription.getCriteria());
				if (matcher.isPresent())
				{
					if (matchers.containsKey(matcher.get().getResourceType()))
					{
						matchers.get(matcher.get().getResourceType())
								.add(new SubscriptionAndMatcher(subscription, matcher.get()));
					}
					else
					{
						matchers.put(matcher.get().getResourceType(), new ArrayList<>(
								Collections.singletonList(new SubscriptionAndMatcher(subscription, matcher.get()))));
					}
				}
			}
			matchersByResource.replaceAll(matchers);
			subscriptionsByIdPart.replaceAll(subscriptions.stream()
					.collect(Collectors.toMap(s -> s.getIdElement().getIdPart(), Function.identity())));

			logger.debug("Current active subscription-ids (after refreshing): {}", subscriptionsByIdPart.getAllKeys());
		}
		catch (SQLException e)
		{
			logger.error("Error while accessing DB", e);
		}
	}

	@Override
	public void destroy() throws Exception
	{
		executor.shutdown();
		try
		{
			if (!executor.awaitTermination(60, TimeUnit.SECONDS))
			{
				executor.shutdownNow();
				if (!executor.awaitTermination(60, TimeUnit.SECONDS))
					logger.warn("EventManager executor did not terminate");
			}
		}
		catch (InterruptedException ie)
		{
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void handleEvents(List<Event> events)
	{
		executor.execute(() -> doHandleEventsAndRefreshMatchers(events));
	}

	private void doHandleEventsAndRefreshMatchers(List<Event> events)
	{
		if (events.stream().anyMatch(e -> e.getResource() instanceof Subscription || firstCall.get()))
			refreshMatchers();

		events.stream().forEach(this::doHandleEvent);
	}

	@Override
	public void handleEvent(Event event)
	{
		executor.execute(() -> doHandleEventAndRefreshMatchers(event));
	}

	private void doHandleEventAndRefreshMatchers(Event event)
	{
		if (event.getResource() instanceof Subscription || firstCall.get())
			refreshMatchers();

		doHandleEvent(event);
	}

	private void doHandleEvent(Event event)
	{
		logger.debug("handling event {} for resource of type {} with id {}", event.getClass().getName(),
				event.getResourceType().getAnnotation(ResourceDef.class).name(), event.getId());

		Optional<List<SubscriptionAndMatcher>> optMatchers = matchersByResource.get(event.getResourceType());
		if (optMatchers.isEmpty())
		{
			logger.debug("No subscriptions for event {} for resource of type {} with id {}", event.getClass().getName(),
					event.getResourceType().getAnnotation(ResourceDef.class).name(), event.getId());
			return;
		}

		List<SubscriptionAndMatcher> matchingSubscriptions = optMatchers.get().stream()
				.filter(sAndM -> sAndM.matches(event.getResource(), daoProvider)).collect(Collectors.toList());

		if (matchingSubscriptions.isEmpty())
		{
			logger.debug("No matching subscriptions for event {} for resource of type {} with id {}",
					event.getClass().getName(), event.getResourceType().getAnnotation(ResourceDef.class).name(),
					event.getId());
			return;
		}

		matchingSubscriptions.forEach(sAndM -> doHandleEventWithSubscription(sAndM.subscription, event));
	}

	private void doHandleEventWithSubscription(Subscription s, Event event)
	{
		Optional<List<SessionIdAndRemoteAsync>> optRemotes = asyncRemotesBySubscriptionIdPart
				.get(s.getIdElement().getIdPart());

		if (optRemotes.isEmpty())
		{
			logger.debug("No remotes connected to subscription with id {}", s.getIdElement().getIdPart());
			return;
		}

		final String text;
		if (Constants.CT_FHIR_JSON_NEW.equals(s.getChannel().getPayload()))
			text = fhirContext.newJsonParser().encodeResourceToString(event.getResource());
		else if (Constants.CT_FHIR_XML_NEW.contentEquals(s.getChannel().getPayload()))
			text = fhirContext.newXmlParser().encodeResourceToString(event.getResource());
		else
			text = "ping " + s.getIdElement().getIdPart();

		logger.debug("Calling {} remote{} connected to subscription with id {}", optRemotes.get().size(),
				optRemotes.get().size() != 1 ? "s" : "", s.getIdElement().getIdPart());

		// defensive copy since since list could be changed by other threads while we are reading
		List<SessionIdAndRemoteAsync> remotes = new ArrayList<>(optRemotes.get());
		remotes.forEach(r -> send(r, text));
	}

	private void send(SessionIdAndRemoteAsync sessionAndRemote, String text)
	{
		try
		{
			sessionAndRemote.remoteAsync.sendText(text);
		}
		catch (Exception e)
		{
			logger.warn("Error while sending event to remote with session id {}", sessionAndRemote.sessionId);
		}
	}

	@Override
	public void bind(String sessionId, Async asyncRemote, String subscriptionIdPart)
	{
		if (firstCall.get())
			refreshMatchers();

		if (subscriptionsByIdPart.containsKey(subscriptionIdPart))
		{
			logger.debug("Binding websocket session {} to subscription {}", sessionId, subscriptionIdPart);
			asyncRemotesBySubscriptionIdPart.replace(subscriptionIdPart, list ->
			{
				if (list == null)
				{
					List<SessionIdAndRemoteAsync> newList = new ArrayList<>();
					newList.add(new SessionIdAndRemoteAsync(sessionId, asyncRemote));
					return newList;
				}
				else
				{
					list.add(new SessionIdAndRemoteAsync(sessionId, asyncRemote));
					return list;
				}
			});
			asyncRemote.sendText("bound " + subscriptionIdPart);
		}
		else
		{
			logger.warn("Could not bind websocket session {} to subscription {}, subscription not found", sessionId,
					subscriptionIdPart);
			logger.debug("Current active subscription-ids: {}", subscriptionsByIdPart.getAllKeys());
			asyncRemote.sendText("not-found " + subscriptionIdPart); // TODO not part of FHIR specification
		}
	}

	@Override
	public void close(String sessionId)
	{
		logger.debug("Removing websocket session {}", sessionId);
		asyncRemotesBySubscriptionIdPart.removeWhereValueMatches(list -> list.isEmpty(),
				list -> list.remove(new SessionIdAndRemoteAsync(sessionId, null)));
	}
}
