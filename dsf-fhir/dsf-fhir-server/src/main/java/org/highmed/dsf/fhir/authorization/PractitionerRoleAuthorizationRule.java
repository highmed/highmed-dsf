package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.PractitionerRoleDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ResourceReference;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PractitionerRoleAuthorizationRule extends AbstractAuthorizationRule<PractitionerRole, PractitionerRoleDao>
{
	private static final Logger logger = LoggerFactory.getLogger(PractitionerRoleAuthorizationRule.class);

	public PractitionerRoleAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider)
	{
		super(PractitionerRole.class, daoProvider, serverBase, referenceResolver, organizationProvider);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, PractitionerRole newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(connection, user, newResource);
			if (errors.isEmpty())
			{
				logger.info(
						"Create of PractitionerRole authorized for local user '{}', practitioner and organization references resolved",
						user.getName());
				return Optional.of("local user; practitioner and organization resolved");
			}
			else
			{
				logger.warn("Create of PractitionerRole unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Create of PractitionerRole unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private Optional<String> newResourceOk(Connection connection, User user, PractitionerRole newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (newResource.hasOrganization())
		{
			Optional<ResourceReference> organizationReference = createIfLiteralInternalOrLogicalReference(
					"PractitionerRole.organization", newResource.getOrganization(), Organization.class);
			if (organizationReference.isPresent())
			{
				Optional<Resource> organization = resolveReference(connection, user, organizationReference);
				if (!organization.isPresent())
				{
					errors.add("PractitionerRole.organization could not be resolved");
				}
			}
			else
			{
				errors.add("PractitionerRole.organization not a literal internal or logical reference");
			}
		}
		else
		{
			errors.add("PractitionerRole.organization missing");
		}

		if (newResource.hasPractitioner())
		{
			Optional<ResourceReference> practitionerReference = createIfLiteralInternalOrLogicalReference(
					"PractitionerRole.practitioner", newResource.getPractitioner(), Practitioner.class);
			if (practitionerReference.isPresent())
			{
				Optional<Resource> practitioner = resolveReference(connection, user, practitionerReference);
				if (!practitioner.isPresent())
				{
					errors.add("PractitionerRole.practitioner could not be resolved");
				}
			}
			else
			{
				errors.add("PractitionerRole.practitioner not a literal internal or logical reference");
			}
		}
		else
		{
			errors.add("PractitionerRole.practitioner missing");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, PractitionerRole existingResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Read of PractitionerRole authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else if (isRemoteUser(user))
		{
			if (isCurrentUserPartOfReferencedOrganization(connection, user, "PractitionerRole.organization",
					existingResource.getOrganization()))
			{
				logger.info(
						"Read of PractitionerRole authorized, PractitionerRole.organization reference could be resolved and user '{}' is part of referenced organization",
						user.getName());
				return Optional.of(
						"remote user, PractitionerRole.organization resolved and user part of referenced organization");
			}
			else if (researchStudyWithPrincipalInvestigatorAndUsersOrganizationExists(connection, user,
					existingResource.getIdElement()))
			{
				logger.warn(
						"Read of PractitionerRole authorized, remote user '{}' part of ResearchStudy with this PractitionerRole as principal investigator",
						user.getName());
				return Optional.of(
						"remote user, users organization part of ResearchStudies principal investigator equal to this PractitionerRole");

			}
			else if (researchStudyWithPrincipalInvestigatorAndUsersOrganizationExists(connection, user,
					new IdType(existingResource.getPractitioner().getReference())))
			{
				logger.warn(
						"Read of PractitionerRole authorized, remote user '{}' part of ResearchStudy with this PractitionerRoles Practitioner as principal investigator",
						user.getName());
				return Optional.of(
						"remote user, users organization part of ResearchStudy and principal investigator equal to this PractitionerRoles Practitioner");
			}
			else
			{
				logger.warn(
						"Read of PractitionerRole unauthorized, remote user '{}' not part of PractitionerRoles organization or not part of ResearchStudy with this PractitionerRole as principal investigator or not part of ResearchStudy with this PractitionerRoles Practitioner as principal investigator",
						user.getName());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Read of PractitionerRole unauthorized, not a local or remote user");
			return Optional.empty();
		}
	}

	private boolean researchStudyWithPrincipalInvestigatorAndUsersOrganizationExists(Connection connection, User user,
			IdType principalInvestigatorId)
	{
		try
		{
			List<ResearchStudy> studies = daoProvider.getResearchStudyDao()
					.readByPrincipalInvestigatorIdAndOrganizationTypeAndOrganizationIdWithTransaction(connection,
							principalInvestigatorId, user.getOrganizationType(), user.getOrganization().getIdElement());
			return !studies.isEmpty();
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for research studies", e);
			return false;
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, PractitionerRole oldResource,
			PractitionerRole newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(connection, user, newResource);
			if (errors.isEmpty())
			{
				logger.info(
						"Update of PractitionerRole authorized for local user '{}', practitioner and organization references resolved",
						user.getName());
				return Optional.of("local user; practitioner and organization resolved");
			}
			else
			{
				logger.warn("Update of PractitionerRole unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Update of PractitionerRole unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, PractitionerRole oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of PractitionerRole authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of PractitionerRole unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(Connection connection, User user)
	{
		logger.info("Search of PractitionerRole authorized for {} user '{}', will be fitered by users organization {}",
				user.getRole(), user.getName(), user.getOrganization().getIdElement().getValueAsString());
		return Optional.of("Allowed for all, filtered by users organization");
	}
}
