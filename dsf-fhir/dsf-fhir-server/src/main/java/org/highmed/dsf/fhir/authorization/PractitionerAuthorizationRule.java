package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.PractitionerDao;
import org.highmed.dsf.fhir.dao.PractitionerRoleDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PractitionerAuthorizationRule extends AbstractAuthorizationRule<Practitioner, PractitionerDao>
{
	private static final Logger logger = LoggerFactory.getLogger(PractitionerAuthorizationRule.class);

	public PractitionerAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider)
	{
		super(Practitioner.class, daoProvider, serverBase, referenceResolver, organizationProvider);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, Practitioner newResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Create of Practitioner authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Create of Practitioner unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, Practitioner existingResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Read of Practitioner authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else if (isRemoteUser(user))
		{
			if (researchStudyWithPrincipalInvestigatorAndUsersOrganizationExists(connection, user, existingResource))
			{
				logger.info(
						"Read of Practitioner authorized for remote user '{}', ResearchStudy with principalInvestigator equal to this Practitioner and users organization part of ResearchStudy",
						user.getName());
				return Optional.of(
						"remote user, users organization part of ResearchStudy with principal investigator equal to this Practitioner");
			}
			// TODO ResearchStudy with users organization and principalInvestigator set to PractitionerRole and
			// PractitionerRoles practitioner set to this Practitioner
			else if (practitionerRoleWithPractitionerAndUsersOrganizationExists(connection, user, existingResource))
			{
				logger.info(
						"Read of Practitioner authorized for remote user '{}', PractitionerRole with organization equal to users organization and practitioner equal to this Practitioner");
				return Optional.of("remote user, PractitionerRole with users organizatio and this Practitioner");
			}
			else
			{
				logger.warn(
						"PractitionerRole or ResearchStudy with this Practitioner and users Organization not found");
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Read of Practitioner unauthorized, not a local or remote user");
			return Optional.empty();
		}
	}

	private boolean researchStudyWithPrincipalInvestigatorAndUsersOrganizationExists(Connection connection, User user,
			Practitioner existingResource)
	{
		try
		{
			return daoProvider.getResearchStudyDao()
					.existsByPrincipalInvestigatorIdAndOrganizationTypeAndOrganizationIdWithTransaction(connection,
							existingResource.getIdElement(), user.getOrganizationType(),
							user.getOrganization().getIdElement());
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for research studies", e);
			return false;
		}
	}

	private boolean practitionerRoleWithPractitionerAndUsersOrganizationExists(Connection connection, User user,
			Practitioner existingResource)
	{
		try
		{
			PractitionerRoleDao dao = daoProvider.getPractitionerRoleDao();
			SearchQuery<PractitionerRole> query = dao.createSearchQueryWithoutUserFilter(0, 0)
					.configureParameters(Map.of("practitioner",
							Collections
									.singletonList(existingResource.getIdElement().toVersionless().getValueAsString()),
							"organization", Collections.singletonList(
									user.getOrganization().getIdElement().toVersionless().getValueAsString())));
			PartialResult<PractitionerRole> result = dao.searchWithTransaction(connection, query);
			return result.getOverallCount() > 0;
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for research studies", e);
			return false;
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, Practitioner oldResource,
			Practitioner newResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Update of Practitioner authorized for local user '{}'", user.getName());
			return Optional.of("local user");

		}
		else
		{
			logger.warn("Update of Practitioner unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, Practitioner oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of Practitioner authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of Practitioner unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(Connection connection, User user)
	{
		logger.info("Search of Practitioner authorized for {} user '{}', will be fitered by users organization {}",
				user.getRole(), user.getName(), user.getOrganization().getIdElement().getValueAsString());
		return Optional.of("Allowed for all, filtered by users organization");
	}
}
