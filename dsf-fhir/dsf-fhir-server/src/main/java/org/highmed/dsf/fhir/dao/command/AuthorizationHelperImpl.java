package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authorization.AuthorizationRule;
import org.highmed.dsf.fhir.authorization.AuthorizationRuleProvider;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class AuthorizationHelperImpl implements AuthorizationHelper
{
	private static final Logger logger = LoggerFactory.getLogger(AuthorizationHelperImpl.class);
	private static final Logger audit = LoggerFactory.getLogger("dsf-audit-logger");

	private final AuthorizationRuleProvider authorizationRuleProvider;
	private final ResponseGenerator responseGenerator;

	public AuthorizationHelperImpl(AuthorizationRuleProvider authorizationRuleProvider,
			ResponseGenerator responseGenerator)
	{
		this.authorizationRuleProvider = authorizationRuleProvider;
		this.responseGenerator = responseGenerator;
	}

	@SuppressWarnings("unchecked")
	private Optional<AuthorizationRule<Resource>> getAuthorizationRule(Class<?> resourceClass)
	{
		return authorizationRuleProvider.getAuthorizationRule(resourceClass)
				.map(rule -> (AuthorizationRule<Resource>) rule);
	}

	@SuppressWarnings("unchecked")
	private Optional<AuthorizationRule<Resource>> getAuthorizationRule(String resourceTypeName)
	{
		return authorizationRuleProvider.getAuthorizationRule(resourceTypeName)
				.map(rule -> (AuthorizationRule<Resource>) rule);
	}

	private WebApplicationException forbidden(String operation, User user) throws WebApplicationException
	{
		return new WebApplicationException(responseGenerator.forbiddenNotAllowed(operation, user));
	}

	@Override
	public void checkCreateAllowed(Connection connection, User user, Resource newResource)
	{
		Optional<AuthorizationRule<Resource>> optRule = getAuthorizationRule(newResource.getClass());
		optRule.flatMap(rule -> rule.reasonCreateAllowed(connection, user, newResource)).ifPresentOrElse(reason ->
		{
			audit.info("Create of resource {} allowed for user '{}' ({}) via bundle, reason: {}",
					newResource.getResourceType().name(), user.getName(), user.getSubjectDn(), reason);
		}, () ->
		{
			audit.info("Create of resource {} denied for user '{}' ({}) via bundle",
					newResource.getResourceType().name(), user.getName(), user.getSubjectDn());
			throw forbidden("create", user);
		});
	}

	@Override
	public void checkReadAllowed(Connection connection, User user, Resource existingResource)
	{
		Optional<AuthorizationRule<Resource>> optRule = getAuthorizationRule(existingResource.getClass());
		optRule.flatMap(rule -> rule.reasonReadAllowed(connection, user, existingResource)).ifPresentOrElse(reason ->
		{
			audit.info("Read of resource {} allowed for user '{}' ({}) via bundle, reason: {}",
					existingResource.getIdElement().getValue(), user.getName(), user.getSubjectDn(), reason);
		}, () ->
		{
			audit.info("Read of resource {} denied for user '{}' ({}) via bundle",
					existingResource.getIdElement().getValue(), user.getName(), user.getSubjectDn());
			throw forbidden("read", user);
		});
	}

	@Override
	public void checkUpdateAllowed(Connection connection, User user, Resource oldResource, Resource newResource)
	{
		Optional<AuthorizationRule<Resource>> optRule = getAuthorizationRule(oldResource.getClass());
		optRule.flatMap(rule -> rule.reasonUpdateAllowed(connection, user, oldResource, newResource))
				.ifPresentOrElse(reason ->
				{
					audit.info("Update of resource {} allowed for user '{}' ({}), reason: {}",
							oldResource.getIdElement().getValue(), user.getName(), user.getSubjectDn(), reason);
				}, () ->
				{
					audit.info("Update of resource {} denied for user '{}' ({})", oldResource.getIdElement().getValue(),
							user.getName(), user.getSubjectDn());
					throw forbidden("update", user);
				});
	}

	@Override
	public void checkDeleteAllowed(Connection connection, User user, Resource oldResource)
	{
		Optional<AuthorizationRule<Resource>> optRule = getAuthorizationRule(oldResource.getClass());
		optRule.flatMap(rule -> rule.reasonDeleteAllowed(user, oldResource)).ifPresentOrElse(reason ->
		{
			audit.info("Delete of resource {} allowed for user '{}' ({}), reason: {}",
					oldResource.getIdElement().getValue(), user.getName(), user.getSubjectDn(), reason);
		}, () ->
		{
			audit.info("Delete of resource {} denied for user '{}' ({})", oldResource.getIdElement().getValue(),
					user.getName(), user.getSubjectDn());
			throw forbidden("delete", user);
		});
	}

	@Override
	public void checkSearchAllowed(User user, String resourceTypeName)
	{
		Optional<AuthorizationRule<Resource>> optRule = getAuthorizationRule(resourceTypeName);
		optRule.flatMap(rule -> rule.reasonSearchAllowed(user)).ifPresentOrElse(reason ->
		{
			audit.info("Search of resource {} allowed for user '{}' ({}), reason: {}", resourceTypeName, user.getName(),
					user.getSubjectDn(), reason);
		}, () ->
		{
			audit.info("Search of resource {} denied for user '{}' ({})", resourceTypeName, user.getName(),
					user.getSubjectDn());
			throw forbidden("search", user);
		});
	}

	@Override
	public void filterIncludeResults(Connection connection, User user, Bundle multipleResult)
	{
		List<BundleEntryComponent> filteredEntries = multipleResult.getEntry().stream()
				.filter(c -> SearchEntryMode.MATCH.equals(c.getSearch().getMode())
						|| (SearchEntryMode.INCLUDE.equals(c.getSearch().getMode())
								&& filterIncludeResource(user, c.getResource())))
				.collect(Collectors.toList());
		multipleResult.setEntry(filteredEntries);
	}

	private boolean filterIncludeResource(User user, Resource include)
	{
		Optional<AuthorizationRule<Resource>> optRule = getAuthorizationRule(include.getClass());
		return optRule.flatMap(rule -> rule.reasonReadAllowed(user, include)).map(reason ->
		{
			logger.debug("Include resource of type {} with id {}, allowed - {}",
					include.getClass().getAnnotation(ResourceDef.class).name(), include.getIdElement().getValue(),
					reason);
			return true;
		}).orElseGet(() ->
		{
			logger.debug("Include resource of type {} with id {}, filtered (read not allowed)",
					include.getClass().getAnnotation(ResourceDef.class).name(), include.getIdElement().getValue());
			return false;
		});
	}
}
