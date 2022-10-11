package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.SubscriptionDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.help.ParameterConverter;
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

public class SubscriptionAuthorizationRule extends AbstractMetaTagAuthorizationRule<Subscription, SubscriptionDao>
{
	private static final Logger logger = LoggerFactory.getLogger(SubscriptionAuthorizationRule.class);

	public SubscriptionAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider,
			ReadAccessHelper readAccessHelper, ParameterConverter parameterConverter)
	{
		super(Subscription.class, daoProvider, serverBase, referenceResolver, organizationProvider, readAccessHelper,
				parameterConverter);
	}

	@Override
	protected Optional<String> newResourceOkForCreate(Connection connection, User user, Subscription newResource)
	{
		return newResourceOk(connection, user, newResource);
	}

	@Override
	protected Optional<String> newResourceOkForUpdate(Connection connection, User user, Subscription newResource)
	{
		return newResourceOk(connection, user, newResource);
	}

	private Optional<String> newResourceOk(Connection connection, User user, Subscription newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (newResource.hasChannel())
		{
			if (newResource.getChannel().hasPayload()
					&& !(Constants.CT_FHIR_JSON_NEW.equals(newResource.getChannel().getPayload())
							|| Constants.CT_FHIR_XML_NEW.equals(newResource.getChannel().getPayload())))
			{
				errors.add("Subscription.channel.payload not " + Constants.CT_FHIR_JSON_NEW + " or "
						+ Constants.CT_FHIR_XML_NEW);
			}

			if (!SubscriptionChannelType.WEBSOCKET.equals(newResource.getChannel().getType()))
			{
				errors.add("Subscription.channel.type not " + SubscriptionChannelType.WEBSOCKET);
			}
		}
		else
		{
			errors.add("Subscription.channel not defined");
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
						errors.add("Subscription.criteria invalid (parameters '" + unsupportedQueryParameters.stream()
								.map(SearchQueryParameterError::toString).collect(Collectors.joining(", "))
								+ "' not supported)");
					}
				}
				else
				{
					errors.add(
							"Subscription.criteria invalid (resource '" + cComponentes.getPath() + "' not supported)");
				}
			}
			else
			{
				errors.add("Subscription.criteria invalid ('" + cComponentes.getPath() + "')");
			}
		}
		else
		{
			errors.add("Subscription.criteria not defined");
		}

		if (!hasValidReadAccessTag(connection, newResource))
		{
			errors.add("Subscription is missing valid read access tag");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	@Override
	protected boolean resourceExists(Connection connection, Subscription newResource)
	{
		Map<String, List<String>> queryParameters = Map.of("criteria",
				Collections.singletonList(newResource.getCriteria()), "type",
				Collections.singletonList(newResource.getChannel().getType().toCode()), "payload",
				Collections.singletonList(newResource.getChannel().getPayload()));
		SubscriptionDao dao = getDao();
		SearchQuery<Subscription> query = dao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(queryParameters);

		if (!query.getUnsupportedQueryParameters(queryParameters).isEmpty())
			return false;

		try
		{
			PartialResult<Subscription> result = dao.searchWithTransaction(connection, query);
			return result.getTotal() >= 1;
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for Subscriptions", e);
			return false;
		}
	}

	@Override
	protected boolean modificationsOk(Connection connection, Subscription oldResource, Subscription newResource)
	{
		return oldResource.getCriteria().equals(newResource.getCriteria())
				&& oldResource.getChannel().getType().equals(newResource.getChannel().getType())
				&& (oldResource.getChannel().getPayload().equals(newResource.getChannel().getPayload())
						|| (oldResource.getChannel().getPayload() == null
								&& newResource.getChannel().getPayload() == null));
	}
}
