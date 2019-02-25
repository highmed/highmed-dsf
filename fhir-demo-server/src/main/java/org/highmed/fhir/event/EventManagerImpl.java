package org.highmed.fhir.event;

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
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.websocket.RemoteEndpoint.Async;

import org.highmed.fhir.dao.SubscriptionDao;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.search.SearchQuery;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.rest.api.Constants;

public class EventManagerImpl implements EventManager, InitializingBean, DisposableBean
{
	private static final Logger logger = LoggerFactory.getLogger(EventManagerImpl.class);

	private static class SubscriptionAndMatcher
	{
		final Subscription subscription;
		final SearchQuery<?> query;

		SubscriptionAndMatcher(Subscription subscription, SearchQuery<?> query)
		{
			this.subscription = subscription;
			this.query = query;
		}

		boolean matches(DomainResource resource)
		{
			return query.matches(resource);
		}
	}

	private static class SessionIdAndRemoteAsync
	{
		final String sessionId;
		final Async removeAsync;

		SessionIdAndRemoteAsync(String sessionId, Async removeAsync)
		{
			this.sessionId = sessionId;
			this.removeAsync = removeAsync;
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

	private final SubscriptionDao subscriptionDao;
	private final ExceptionHandler exceptionHandler;
	private final MatcherFactory matcherFactory;
	private final FhirContext fhirContext;

	private final ReadWriteMap<String, Subscription> subscriptionsByIdPart = new ReadWriteMap<>();
	private final ReadWriteMap<Class<? extends DomainResource>, List<SubscriptionAndMatcher>> searchQueriesByResource = new ReadWriteMap<>();
	private final ReadWriteMap<String, List<SessionIdAndRemoteAsync>> asyncRemotesBySubscriptionIdPart = new ReadWriteMap<>();

	public EventManagerImpl(SubscriptionDao subscriptionDao, ExceptionHandler exceptionHandler,
			MatcherFactory matcherFactory, FhirContext fhirContext)
	{
		this.subscriptionDao = subscriptionDao;
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
	}

	@EventListener({ ContextRefreshedEvent.class })
	public void onContextRefreshedEvent(ContextRefreshedEvent event)
	{
		refreshQueries();
	}

	private void refreshQueries()
	{
		logger.info("Refreshing subscriptions");

		try
		{
			List<Subscription> subscriptions = subscriptionDao.readByStatus(SubscriptionStatus.ACTIVE);
			Map<Class<? extends DomainResource>, List<SubscriptionAndMatcher>> queries = new HashMap<>();
			for (Subscription subscription : subscriptions)
			{
				Optional<SearchQuery<? extends DomainResource>> query = matcherFactory
						.createQuery(subscription.getCriteria());
				if (query.isPresent())
				{
					if (queries.containsKey(query.get().getResourceType()))
					{

						queries.get(query.get().getResourceType())
								.add(new SubscriptionAndMatcher(subscription, query.get()));
					}
					else
					{
						queries.put(query.get().getResourceType(), new ArrayList<>(
								Collections.singletonList(new SubscriptionAndMatcher(subscription, query.get()))));
					}
				}
			}
			searchQueriesByResource.replaceAll(queries);
			subscriptionsByIdPart.replaceAll(subscriptions.stream()
					.collect(Collectors.toMap(s -> s.getIdElement().getIdPart(), Function.identity())));
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
	public void handleEvent(Event<?> event)
	{
		executor.execute(() -> handleEventAsync(event));
	}

	private void handleEventAsync(Event<?> event)
	{
		logger.debug("handling event {} for resource of type {} with id {}", event.getClass().getName(),
				event.getResourceType().getAnnotation(ResourceDef.class).name(), event.getId());

		if (Subscription.class.equals(event.getResourceType()))
			refreshQueries();

		Optional<List<SubscriptionAndMatcher>> optMatchers = searchQueriesByResource.get(event.getResourceType());
		if (optMatchers.isEmpty())
		{
			logger.debug("No subscriptions for event {} for resource of type {} with id {}", event.getClass().getName(),
					event.getResourceType().getAnnotation(ResourceDef.class).name(), event.getId());
			return;
		}

		List<SubscriptionAndMatcher> matchingSubscriptions = optMatchers.get().stream()
				.filter(sAndM -> sAndM.matches(event.getResource())).collect(Collectors.toList());

		if (matchingSubscriptions.isEmpty())
		{
			logger.debug("No matching subscriptions for event {} for resource of type {} with id {}",
					event.getClass().getName(), event.getResourceType().getAnnotation(ResourceDef.class).name(),
					event.getId());
			return;
		}

		matchingSubscriptions.forEach(sAndM -> this.handleEvent(sAndM.subscription, event));
	}

	private void handleEvent(Subscription s, Event<?> event)
	{
		logger.debug("handling event {} for resource of type {} with id {} based on active subscription with id {}",
				event.getClass().getName(), event.getResourceType().getAnnotation(ResourceDef.class).name(),
				event.getId(), s.getIdElement().getIdPart());

		Optional<List<SessionIdAndRemoteAsync>> optRemotes = asyncRemotesBySubscriptionIdPart
				.get(s.getIdElement().getIdPart());
		if (optRemotes.isEmpty())
		{
			logger.debug("No remotes connected for event {} for resource of type {} with id {}",
					event.getClass().getName(), event.getResourceType().getAnnotation(ResourceDef.class).name(),
					event.getId());
			return;
		}

		final String text;
		if (Constants.CT_FHIR_JSON_NEW.equals(s.getChannel().getPayload()))
			text = fhirContext.newJsonParser().encodeResourceToString(event.getResource());
		else if (Constants.CT_FHIR_XML_NEW.contentEquals(s.getChannel().getPayload()))
			text = fhirContext.newXmlParser().encodeResourceToString(event.getResource());
		else
			text = "ping " + s.getIdElement().getIdPart();

		logger.debug("Calling {} remote{} connected for event {} for resource of type {} with id {}",
				optRemotes.get().size(), optRemotes.get().size() != 1 ? "s" : "", event.getClass().getName(),
				event.getResourceType().getAnnotation(ResourceDef.class).name(), event.getId());

		optRemotes.get().forEach(r -> r.removeAsync.sendText(text));
	}

	@Override
	public void bind(String sessionId, Async asyncRemote, String subscriptionIdPart)
	{
		if (subscriptionsByIdPart.containsKey(subscriptionIdPart))
		{
			logger.debug("Binding websocket session {} to subscription {}", sessionId, subscriptionIdPart);
			asyncRemotesBySubscriptionIdPart.putWithOldValue(subscriptionIdPart, list ->
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
			asyncRemote.sendText("Not Found");
	}

	@Override
	public void close(String sessionId)
	{
		logger.debug("Removing websocket session {}", sessionId);
		asyncRemotesBySubscriptionIdPart.removeWhereValueMatches(list -> list.isEmpty(),
				list -> list.remove(new SessionIdAndRemoteAsync(sessionId, null)));
	}
}
