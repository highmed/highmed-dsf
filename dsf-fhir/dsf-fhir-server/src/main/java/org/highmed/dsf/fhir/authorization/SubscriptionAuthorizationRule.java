package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.SubscriptionDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import ca.uhn.fhir.rest.api.Constants;

public class SubscriptionAuthorizationRule extends AbstractAuthorizationRule<Subscription, SubscriptionDao>
{
	private static final Logger logger = LoggerFactory.getLogger(SubscriptionAuthorizationRule.class);

	public SubscriptionAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver)
	{
		super(Subscription.class, daoProvider, serverBase, referenceResolver);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, Subscription newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(newResource);
			if (errors.isEmpty())
			{
				if (!subscriptionExists(connection, newResource))
				{
					logger.info(
							"Create of Subscription authorized for local user '{}', Subscription with criteria, type and payload does not exist",
							user.getName());
					return Optional.of("local user, Subscription with criteria, type and payload does not exist yet");
				}
				else
				{
					logger.warn(
							"Create of Subscription unauthorized, Subscription with criteria, type and payload already exists");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Create of Subscription unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Create of Subscription unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private Optional<String> newResourceOk(Subscription newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (newResource.hasChannel())
		{
			if (newResource.getChannel().hasPayload()
					&& !(Constants.CT_FHIR_JSON_NEW.equals(newResource.getChannel().getPayload())
							|| Constants.CT_FHIR_XML_NEW.equals(newResource.getChannel().getPayload())))
			{
				errors.add("subscription.channel.payload not " + Constants.CT_FHIR_JSON_NEW + " or " + Constants.CT_FHIR_XML_NEW);
			}

			if (!SubscriptionChannelType.WEBSOCKET.equals(newResource.getChannel().getType()))
			{
				errors.add("subscription.channel.type not " + SubscriptionChannelType.WEBSOCKET);
			}
		}
		else
		{
			errors.add("subscription.channel not defined");
		}

		if (newResource.hasCriteria())
		{
			UriComponents cComponentes = UriComponentsBuilder.fromUriString(newResource.getCriteria()).build();
			if (cComponentes.getPathSegments().size() == 1)
			{
				Optional<ResourceDao<?>> optDao = daoProvider.getDao(cComponentes.getPathSegments().get(0));
				if (optDao.isPresent())
				{
					SearchQuery<?> searchQuery = optDao.get().createSearchQueryWithoutUserFilter(1, 1);
					List<SearchQueryParameterError> unsupportedQueryParameters = searchQuery
							.getUnsupportedQueryParameters(cComponentes.getQueryParams());

					if (!unsupportedQueryParameters.isEmpty())
					{
						errors.add("subscription.criteria invalid (parameters '" + unsupportedQueryParameters.stream()
								.map(SearchQueryParameterError::toString).collect(Collectors.joining(", "))
								+ "' not supported)");
					}
				}
				else
				{
					errors.add("subscription.criteria invalid (resource '" + cComponentes.getPath() + "' not supported)");
				}
			}
			else
			{
				errors.add("subscription.criteria invalid ('" + cComponentes.getPath() + "')");
			}
		}
		else
		{
			errors.add("subscription.criteria not defined");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	private boolean subscriptionExists(Connection connection, Subscription newResource)
	{
		Map<String, List<String>> queryParameters = Map.of("criteria",
				Collections.singletonList(newResource.getCriteria()), "type",
				Collections.singletonList(newResource.getChannel().getType().toCode()), "payload",
				Collections.singletonList(newResource.getChannel().getPayload()));
		SubscriptionDao dao = getDao();
		SearchQuery<Subscription> query = dao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(queryParameters);
		try
		{
			PartialResult<Subscription> result = dao.searchWithTransaction(connection, query);
			return result.getOverallCount() >= 1;
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for Subscriptions", e);
			return false;
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, Subscription existingResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, Subscription oldResource,
			Subscription newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, Subscription oldResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonSearchAllowed(Connection connection, User user)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}
}
