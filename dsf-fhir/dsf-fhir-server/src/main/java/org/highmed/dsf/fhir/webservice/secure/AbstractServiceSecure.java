package org.highmed.dsf.fhir.webservice.secure;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserProvider;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ResourceReference;
import org.highmed.dsf.fhir.service.ResourceReference.ReferenceType;
import org.highmed.dsf.fhir.webservice.specification.BasicService;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractServiceSecure<S extends BasicService> implements BasicService, InitializingBean
{
	protected static final Logger audit = LoggerFactory.getLogger("dsf-audit-logger");
	private static final Logger logger = LoggerFactory.getLogger(AbstractServiceSecure.class);

	protected final S delegate;
	protected final String serverBase;
	protected final ResponseGenerator responseGenerator;
	protected final ReferenceResolver referenceResolver;

	protected UserProvider userProvider;

	public AbstractServiceSecure(S delegate, String serverBase, ResponseGenerator responseGenerator,
			ReferenceResolver referenceResolver)
	{
		this.serverBase = serverBase;
		this.delegate = delegate;
		this.referenceResolver = referenceResolver;
		this.responseGenerator = responseGenerator;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(delegate, "delegate");
		Objects.requireNonNull(serverBase, "serverBase");
		Objects.requireNonNull(responseGenerator, "responseGenerator");
		Objects.requireNonNull(referenceResolver, "referenceResolver");
	}

	@Override
	public final void setUserProvider(UserProvider userProvider)
	{
		delegate.setUserProvider(userProvider);

		this.userProvider = userProvider;
	}

	protected final User getCurrentUser()
	{
		return userProvider.getCurrentUser();
	}

	protected final boolean isCurrentUserPartOfReferencedOrganizations(String referenceLocation,
			Collection<? extends Reference> references)
	{
		return references.stream().anyMatch(r -> isCurrentUserPartOfReferencedOrganization(referenceLocation, r));
	}

	protected final boolean isCurrentUserPartOfReferencedOrganization(String referenceLocation, Reference reference)
	{
		if (userProvider.getCurrentUser() == null || userProvider.getCurrentUser().getOrganization() == null)
		{
			logger.warn(
					"No current user or current user without organization while checking if user part of referenced organization");
			return false;
		}
		else if (reference == null)
		{
			logger.warn("Null reference while checking if user part of referenced organization");
			return false;
		}
		else
		{
			ResourceReference resReference = new ResourceReference(referenceLocation, reference, Organization.class);

			if (!EnumSet.of(ReferenceType.LITERAL_INTERNAL, ReferenceType.LOGICAL)
					.contains(resReference.getType(serverBase)))
			{
				logger.warn("Reference of type {} not supported while checking if user part of referenced organization",
						resReference.getType(serverBase));
				return false;
			}

			Optional<Resource> resource = referenceResolver.resolveReference(resReference);
			if (resource.isPresent() && resource.get() instanceof Organization)
			{
				boolean sameOrganization = userProvider.getCurrentUser().getOrganization().getIdElement()
						.equals(resource.get().getIdElement());
				if (!sameOrganization)
					logger.warn(
							"Current user not part organization {} while checking if user part of referenced organization",
							resource.get().getIdElement().getValue());

				return sameOrganization;
			}
			else
			{
				logger.warn(
						"Reference to organization could not be resolved while checking if user part of referenced organization");
				return false;
			}
		}
	}

	@Override
	public final String getPath()
	{
		return delegate.getPath();
	}

	protected final Response forbidden(String operation)
	{
		return responseGenerator.forbiddenNotAllowed(operation, userProvider.getCurrentUser());
	}
}
