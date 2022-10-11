package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.dao.EndpointDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointAuthorizationRule extends AbstractMetaTagAuthorizationRule<Endpoint, EndpointDao>
{
	private static final Logger logger = LoggerFactory.getLogger(EndpointAuthorizationRule.class);

	private static final String ENDPOINT_IDENTIFIER_SYSTEM = "http://highmed.org/sid/endpoint-identifier";
	private static final String ENDPOINT_ADDRESS_PATTERN_STRING = "https://([0-9a-zA-Z\\.-]+)+(:\\d{1,4})?([-\\w/]*)";
	private static final Pattern ENDPOINT_ADDRESS_PATTERN = Pattern.compile(ENDPOINT_ADDRESS_PATTERN_STRING);

	public EndpointAuthorizationRule(DaoProvider daoProvider, String serverBase, ReferenceResolver referenceResolver,
			OrganizationProvider organizationProvider, ReadAccessHelper readAccessHelper,
			ParameterConverter parameterConverter)
	{
		super(Endpoint.class, daoProvider, serverBase, referenceResolver, organizationProvider, readAccessHelper,
				parameterConverter);
	}

	@Override
	protected Optional<String> newResourceOkForCreate(Connection connection, User user, Endpoint newResource)
	{
		return newResourceOk(connection, user, newResource);
	}

	@Override
	protected Optional<String> newResourceOkForUpdate(Connection connection, User user, Endpoint newResource)
	{
		return newResourceOk(connection, user, newResource);
	}

	private Optional<String> newResourceOk(Connection connection, User user, Endpoint newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (newResource.hasIdentifier())
		{
			if (newResource.getIdentifier().stream()
					.filter(i -> i.hasSystem() && i.hasValue() && ENDPOINT_IDENTIFIER_SYSTEM.equals(i.getSystem()))
					.count() != 1)
			{
				errors.add("Endpoint.identifier one with system '" + ENDPOINT_IDENTIFIER_SYSTEM
						+ "' and non empty value expected");
			}
		}
		else
		{
			errors.add("Endpoint.identifier missing");
		}

		if (newResource.hasAddress())
		{
			if (!ENDPOINT_ADDRESS_PATTERN.matcher(newResource.getAddress()).matches())
			{
				errors.add("Endpoint.address not matching " + ENDPOINT_ADDRESS_PATTERN_STRING + " pattern");
			}
		}
		else
		{
			errors.add("Endpoint.address missing");
		}

		if (!hasValidReadAccessTag(connection, newResource))
		{
			errors.add("Endpoint is missing valid read access tag");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	@Override
	protected boolean resourceExists(Connection connection, Endpoint newResource)
	{
		String identifierValue = newResource.getIdentifier().stream()
				.filter(i -> i.hasSystem() && i.hasValue() && ENDPOINT_IDENTIFIER_SYSTEM.equals(i.getSystem()))
				.map(i -> i.getValue()).findFirst().orElseThrow();

		return endpointWithAddressExists(connection, newResource.getAddress())
				|| endpointWithIdentifierExists(connection, identifierValue);
	}

	private boolean endpointWithAddressExists(Connection connection, String address)
	{
		Map<String, List<String>> queryParameters = Map.of("address", Collections.singletonList(address));
		EndpointDao dao = getDao();
		SearchQuery<Endpoint> query = dao.createSearchQueryWithoutUserFilter(0, 0).configureParameters(queryParameters);

		if (!query.getUnsupportedQueryParameters(queryParameters).isEmpty())
			return false;

		try
		{
			PartialResult<Endpoint> result = dao.searchWithTransaction(connection, query);
			return result.getTotal() >= 1;
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for Endpoint with address", e);
			return false;
		}
	}

	private boolean endpointWithIdentifierExists(Connection connection, String identifierValue)
	{
		Map<String, List<String>> queryParameters = Map.of("identifier",
				Collections.singletonList(ENDPOINT_IDENTIFIER_SYSTEM + "|" + identifierValue));
		EndpointDao dao = getDao();
		SearchQuery<Endpoint> query = dao.createSearchQueryWithoutUserFilter(0, 0).configureParameters(queryParameters);

		if (!query.getUnsupportedQueryParameters(queryParameters).isEmpty())
			return false;

		try
		{
			PartialResult<Endpoint> result = dao.searchWithTransaction(connection, query);
			return result.getTotal() >= 1;
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for Endpoint with identifier", e);
			return false;
		}
	}

	@Override
	protected boolean modificationsOk(Connection connection, Endpoint oldResource, Endpoint newResource)
	{
		String oldIdentifierValue = oldResource.getIdentifier().stream()
				.filter(i -> ENDPOINT_IDENTIFIER_SYSTEM.equals(i.getSystem())).map(i -> i.getValue()).findFirst()
				.orElseThrow();
		String newIdentifierValue = newResource.getIdentifier().stream()
				.filter(i -> ENDPOINT_IDENTIFIER_SYSTEM.equals(i.getSystem())).map(i -> i.getValue()).findFirst()
				.orElseThrow();

		return oldResource.getAddress().equals(newResource.getAddress())
				&& oldIdentifierValue.equals(newIdentifierValue);
	}
}
