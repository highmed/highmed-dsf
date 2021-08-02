package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.dao.CodeSystemDao;
import org.highmed.dsf.fhir.dao.OrganizationDao;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ResourceReference;
import org.highmed.dsf.fhir.service.ResourceReference.ReferenceType;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractAuthorizationRule<R extends Resource, D extends ResourceDao<R>>
		implements AuthorizationRule<R>, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractAuthorizationRule.class);

	protected static final String ORGANIZATION_IDENTIFIER_SYSTEM = "http://highmed.org/sid/organization-identifier";

	protected final Class<R> resourceType;
	protected final DaoProvider daoProvider;
	protected final String serverBase;
	protected final ReferenceResolver referenceResolver;
	protected final OrganizationProvider organizationProvider;
	protected final ReadAccessHelper readAccessHelper;

	public AbstractAuthorizationRule(Class<R> resourceType, DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider,
			ReadAccessHelper readAccessHelper)
	{
		this.resourceType = resourceType;
		this.daoProvider = daoProvider;
		this.serverBase = serverBase;
		this.referenceResolver = referenceResolver;
		this.organizationProvider = organizationProvider;
		this.readAccessHelper = readAccessHelper;
	}

	@Override
	public Class<R> getResourceType()
	{
		return resourceType;
	}

	@SuppressWarnings("unchecked")
	protected final D getDao()
	{
		return (D) daoProvider.getDao(resourceType).orElseThrow();
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(resourceType, "resourceType");
		Objects.requireNonNull(daoProvider, "daoProvider");
		Objects.requireNonNull(serverBase, "serverBase");
		Objects.requireNonNull(referenceResolver, "referenceResolver");
		Objects.requireNonNull(organizationProvider, "organizationProvider");
		Objects.requireNonNull(readAccessHelper, "readAccessHelper");
	}

	@Override
	public final Optional<String> reasonCreateAllowed(User user, R newResource)
	{
		try (Connection connection = daoProvider.newReadOnlyAutoCommitTransaction())
		{
			return reasonCreateAllowed(connection, user, newResource);
		}
		catch (SQLException e)
		{
			logger.warn("Error while accessing database", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public final Optional<String> reasonReadAllowed(User user, R existingResource)
	{
		try (Connection connection = daoProvider.newReadOnlyAutoCommitTransaction())
		{
			return reasonReadAllowed(connection, user, existingResource);
		}
		catch (SQLException e)
		{
			logger.warn("Error while accessing database", e);
			throw new RuntimeException(e);
		}
	}

	protected List<OrganizationAffiliation> getAffiliations(Connection connection, User user)
	{
		if (user == null)
			return Collections.emptyList();

		Optional<String> identifierValue = user.getOrganization().getIdentifier().stream().filter(i -> i != null)
				.filter(Identifier::hasSystem).filter(i -> ORGANIZATION_IDENTIFIER_SYSTEM.equals(i.getSystem()))
				.filter(Identifier::hasValue).findFirst().map(Identifier::getValue);

		if (identifierValue.isEmpty())
			return Collections.emptyList();

		try
		{
			return daoProvider.getOrganizationAffiliationDao()
					.readActiveNotDeletedByMemberOrganizationIdentifierIncludingOrganizationIdentifiersWithTransaction(
							connection, identifierValue.get());
		}
		catch (SQLException e)
		{
			logger.warn("Error while accessing database", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public final Optional<String> reasonUpdateAllowed(User user, R oldResource, R newResource)
	{
		try (Connection connection = daoProvider.newReadOnlyAutoCommitTransaction())
		{
			return reasonUpdateAllowed(connection, user, oldResource, newResource);
		}
		catch (SQLException e)
		{
			logger.warn("Error while accessing database", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public final Optional<String> reasonDeleteAllowed(User user, R oldResource)
	{
		try (Connection connection = daoProvider.newReadOnlyAutoCommitTransaction())
		{
			return reasonDeleteAllowed(connection, user, oldResource);
		}
		catch (SQLException e)
		{
			logger.warn("Error while accessing database", e);
			throw new RuntimeException(e);
		}
	}

	protected final boolean isLocalUser(User user)
	{
		return user != null && UserRole.LOCAL.equals(user.getRole());
	}

	protected final boolean isLocalPermanentDeleteUser(User user)
	{
		return isLocalUser(user) && user.isPermanentDeleteAllowed();
	}

	protected final boolean isRemoteUser(User user)
	{
		return user != null && UserRole.REMOTE.equals(user.getRole());
	}

	protected final boolean organizationWithIdentifierExists(Connection connection, Identifier organizationIdentifier)
	{
		String iSystem = organizationIdentifier.getSystem();
		String iValue = organizationIdentifier.getValue();

		Map<String, List<String>> queryParameters = Map.of("identifier",
				Collections.singletonList(iSystem + "|" + iValue));
		OrganizationDao dao = daoProvider.getOrganizationDao();
		SearchQuery<Organization> query = dao.createSearchQueryWithoutUserFilter(0, 0)
				.configureParameters(queryParameters);

		List<SearchQueryParameterError> uQp = query.getUnsupportedQueryParameters(queryParameters);
		if (!uQp.isEmpty())
		{
			logger.warn("Unsupported query parameters {} while searching for Organization", uQp);
			return false;
		}

		try
		{
			PartialResult<Organization> result = dao.searchWithTransaction(connection, query);
			return result.getTotal() >= 1;
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for Organization with identifier", e);
			return false;
		}
	}

	protected final boolean roleExists(Connection connection, Coding coding)
	{
		String cSystem = coding.getSystem();
		String cVersion = coding.getVersion();
		String cCode = coding.getCode();

		Map<String, List<String>> queryParameters = Map.of("url",
				Collections.singletonList(cSystem + (coding.hasVersion() ? "|" + cVersion : "")));
		CodeSystemDao dao = daoProvider.getCodeSystemDao();
		SearchQuery<CodeSystem> query = dao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(queryParameters);

		List<SearchQueryParameterError> uQp = query.getUnsupportedQueryParameters(queryParameters);
		if (!uQp.isEmpty())
		{
			logger.warn("Unsupported query parameters {} while searching for CodeSystem", uQp);
			return false;
		}

		try
		{
			PartialResult<CodeSystem> result = dao.searchWithTransaction(connection, query);
			return result.getTotal() >= 1 && hasCode(result.getPartialResult().get(0), cCode);
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for Organization with identifier", e);
			return false;
		}
	}

	private boolean hasCode(CodeSystem codeSystem, String cCode)
	{
		return codeSystem.getConcept().stream().filter(ConceptDefinitionComponent::hasCode)
				.map(ConceptDefinitionComponent::getCode).anyMatch(c -> c.equals(cCode));
	}

	protected final boolean isCurrentUserPartOfReferencedOrganizations(Connection connection, User user,
			String referenceLocation, Collection<? extends Reference> references)
	{
		return isCurrentUserPartOfReferencedOrganizations(connection, user, referenceLocation, references.stream());
	}

	protected final boolean isCurrentUserPartOfReferencedOrganizations(Connection connection, User user,
			String referenceLocation, Stream<? extends Reference> references)
	{
		return references
				.anyMatch(r -> isCurrentUserPartOfReferencedOrganization(connection, user, referenceLocation, r));
	}

	protected final boolean isCurrentUserPartOfReferencedOrganization(Connection connection, User user,
			String referenceLocation, Reference reference)
	{
		if (reference == null)
		{
			logger.warn("Null reference while checking if user part of referenced organization");
			return false;
		}
		else
		{
			ResourceReference resReference = new ResourceReference(referenceLocation, reference, Organization.class);

			ReferenceType type = resReference.getType(serverBase);
			if (!EnumSet.of(ReferenceType.LITERAL_INTERNAL, ReferenceType.LOGICAL).contains(type))
			{
				logger.warn("Reference of type {} not supported while checking if user part of referenced organization",
						type);
				return false;
			}

			Optional<Resource> resource = referenceResolver.resolveReference(user, resReference, connection);
			if (resource.isPresent() && resource.get() instanceof Organization)
			{
				// ignoring updates (version changes) to the organization id
				boolean sameOrganization = user.getOrganization().getIdElement().getIdPart()
						.equals(resource.get().getIdElement().getIdPart());
				if (!sameOrganization)
					logger.warn(
							"Current user not part of organization {} while checking if user part of referenced organization",
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

	protected final boolean isLocalOrganization(Organization organization)
	{
		if (organization == null)
			return false;
		if (!organization.hasIdElement())
			return false;

		return organizationProvider.getLocalOrganization()
				.map(localOrg -> localOrg.getIdElement().equals(organization.getIdElement())).orElse(false);
	}

	@SafeVarargs
	protected final Optional<ResourceReference> createIfLiteralInternalOrLogicalReference(String referenceLocation,
			Reference reference, Class<? extends Resource>... referenceTypes)
	{
		ResourceReference r = new ResourceReference(referenceLocation, reference, referenceTypes);
		ReferenceType type = r.getType(serverBase);
		if (EnumSet.of(ReferenceType.LITERAL_INTERNAL, ReferenceType.LOGICAL).contains(type))
			return Optional.of(r);
		else
			return Optional.empty();
	}

	protected final Optional<Resource> resolveReference(Connection connection, User user,
			Optional<ResourceReference> reference)
	{
		return reference.flatMap(ref -> referenceResolver.resolveReference(user, ref, connection));
	}

	@Override
	public Optional<String> reasonPermanentDeleteAllowed(User user, R oldResource)
	{
		try (Connection connection = daoProvider.newReadOnlyAutoCommitTransaction())
		{
			return reasonPermanentDeleteAllowed(connection, user, oldResource);
		}
		catch (SQLException e)
		{
			logger.warn("Error while accessing database", e);
			throw new RuntimeException(e);
		}
	}
}
