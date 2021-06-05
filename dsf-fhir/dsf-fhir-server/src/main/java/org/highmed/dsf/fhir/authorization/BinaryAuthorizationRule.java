package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.dao.BinaryDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ResourceReference;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class BinaryAuthorizationRule extends AbstractMetaTagAuthorizationRule<Binary, BinaryDao>
{
	private static final Logger logger = LoggerFactory.getLogger(BinaryAuthorizationRule.class);

	private final Map<Class<? extends Resource>, AuthorizationRule<?>> rules;

	public BinaryAuthorizationRule(DaoProvider daoProvider, String serverBase, ReferenceResolver referenceResolver,
			OrganizationProvider organizationProvider, ReadAccessHelper readAccessHelper,
			AuthorizationRule<?>... supportedSecurityContextRules)
	{
		super(Binary.class, daoProvider, serverBase, referenceResolver, organizationProvider, readAccessHelper);

		this.rules = Arrays.stream(supportedSecurityContextRules)
				.collect(Collectors.toMap(AuthorizationRule::getResourceType, Function.identity()));
	}

	protected Optional<String> newResourceOk(Connection connection, User user, Binary newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (!hasValidReadAccessTag(connection, newResource) && !hasValidSecurityContext(connection, user, newResource))
		{
			errors.add("Binary is missing valid read access tag or securityContext");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	private boolean hasValidSecurityContext(Connection connection, User user, Binary newResource)
	{
		if (newResource != null && newResource.hasSecurityContext())
		{
			Optional<ResourceReference> ref = createIfLiteralInternalOrLogicalReference("Binary.securityContext",
					newResource.getSecurityContext());
			Optional<Resource> securityContextOpt = ref
					.flatMap(r -> referenceResolver.resolveReference(user, r, connection));

			return securityContextOpt.isPresent();
		}
		else
		{
			return false;
		}
	}

	@Override
	protected boolean resourceExists(Connection connection, Binary newResource)
	{
		// no unique criteria for Binary
		return false;
	}

	@Override
	protected boolean modificationsOk(Connection connection, Binary oldResource, Binary newResource)
	{
		// no unique criteria for Binary
		return true;
	}

	@Override
	protected Optional<String> reasonReadAllowedByAdditionalCriteria(Connection connection, User user,
			Binary existingResource)
	{
		if (existingResource != null && existingResource.hasSecurityContext())
		{
			Optional<Resource> securityContextOpt = referenceResolver.resolveReference(user,
					new ResourceReference("Binary.securityContext", existingResource.getSecurityContext()), connection);

			if (securityContextOpt.isPresent())
			{
				Resource securityContextResource = securityContextOpt.get();

				@SuppressWarnings("unchecked")
				AuthorizationRule<Resource> rule = (AuthorizationRule<Resource>) rules
						.get(securityContextResource.getClass());

				if (rule != null)
				{
					Optional<String> readAllowed = rule.reasonReadAllowed(connection, user, securityContextResource);

					if (readAllowed.isPresent())
						logger.info("Read of Binary authorized by securityContex");
					else
						logger.warn("Read of Binary not authorized by securityContext");

					return readAllowed.map(reason -> "securityContext allowed by " + reason);
				}
				else
				{
					logger.warn(
							"Read of Binary unauthorized, no authorization rule found for securityContext of type {}",
							securityContextResource.getClass().getAnnotation(ResourceDef.class).name());
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Read of Binary unauthorized, securityContext reference could no be resolved");
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Read of Binary unauthorized, no securityContext reference to check");
			return Optional.empty();
		}
	}
}
