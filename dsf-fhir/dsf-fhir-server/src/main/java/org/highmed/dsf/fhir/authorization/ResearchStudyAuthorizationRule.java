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
import java.util.stream.Stream;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.PractitionerRoleDao;
import org.highmed.dsf.fhir.dao.ResearchStudyDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResearchStudyAuthorizationRule extends AbstractAuthorizationRule<ResearchStudy, ResearchStudyDao>
{
	private static final Logger logger = LoggerFactory.getLogger(ResearchStudyAuthorizationRule.class);

	private static final String RESEARCH_STUDY_IDENTIFIER = "http://highmed.org/fhir/NamingSystem/research-study-identifier";
	private static final String RESEARCH_STUDY_IDENTIFIER_PATTERN_STRING = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
	private static final Pattern RESEARCH_STUDY_IDENTIFIER_PATTERN = Pattern
			.compile(RESEARCH_STUDY_IDENTIFIER_PATTERN_STRING);

	public ResearchStudyAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider)
	{
		super(ResearchStudy.class, daoProvider, serverBase, referenceResolver, organizationProvider);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, ResearchStudy newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(connection, user, newResource);
			if (errors.isEmpty())
			{
				if (!resourceExists(connection, newResource))
				{
					logger.info(
							"Create of ResearchStudy authorized for local user '{}', ResearchStudy with identifier does not exist",
							user.getName());
					return Optional.of("local user, ResearchStudy with identifier not exist yet");
				}
				else
				{
					logger.warn("Create of ResearchStudy unauthorized, ResearchStudy with identifier already exists");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Create of ResearchStudy unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Create of ResearchStudy unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private Optional<String> newResourceOk(Connection connection, User user, ResearchStudy newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (newResource.hasIdentifier())
		{
			if (newResource.getIdentifier().stream()
					.filter(i -> i.hasSystem() && i.hasValue() && RESEARCH_STUDY_IDENTIFIER.equals(i.getSystem())
							&& RESEARCH_STUDY_IDENTIFIER_PATTERN.matcher(i.getValue()).matches())
					.count() != 1)
			{
				errors.add("ResearchStudy.identifier one with system '" + RESEARCH_STUDY_IDENTIFIER
						+ "' and non empty value matching " + RESEARCH_STUDY_IDENTIFIER_PATTERN_STRING + " expected");
			}
		}
		else
		{
			errors.add("ResearchStudy.identifier missing");
		}

		Stream<Reference> participatingMedicReferences = ResearchStudyHelper
				.getParticipatingMedicReferences(newResource);
		if (participatingMedicReferences.count() >= 0)
		{
			if (!organizationsResolvable(connection, user,
					"ResearchStudy.extension(url:http://highmed.org/fhir/StructureDefinition/participating-medic)",
					participatingMedicReferences).allMatch(t -> t))
			{
				errors.add(
						"ResearchStudy.extension(url:http://highmed.org/fhir/StructureDefinition/participating-medic) one or more participating-medic Organizations not resolved");
			}
		}
		else
		{
			errors.add(
					"ResearchStudy.extension(url:http://highmed.org/fhir/StructureDefinition/participating-medic) one or more participating-medic Organization references missing");
		}

		Optional<Reference> participatingTtpReference = ResearchStudyHelper.getParticipatingTtpReference(newResource);
		if (participatingTtpReference.isPresent())
		{
			if (!organizationResolvable(connection, user,
					"ResearchStudy.extension(url:http://highmed.org/fhir/StructureDefinition/participating-ttp)",
					participatingTtpReference.get()))
			{
				errors.add(
						"ResearchStudy.extension(url:http://highmed.org/fhir/StructureDefinition/participating-ttp) participating-ttp Organization not resolved");
			}
		}
		else
		{
			errors.add(
					"ResearchStudy.extension(url:http://highmed.org/fhir/StructureDefinition/participating-ttp) participating-ttp Organization references missing");
		}

		if (newResource.getEnrollment().size() >= 0)
		{
			if (!enrollmentsResolvable(connection, user, "ResearchStudy.enrollment",
					newResource.getEnrollment().stream()).allMatch(t -> t))
			{
				errors.add("ResearchStudy.enrollment one or more Groups not resolved");
			}
		}
		else
		{
			errors.add("ResearchStudy.enrollment one or more Group references missing");
		}

		if (newResource.hasPrincipalInvestigator())
		{
			Optional<Resource> practitioner = resolvePractitioner(connection, user,
					"ResearchStudy.principalInvestigator", newResource.getPrincipalInvestigator());
			if (practitioner.isPresent())
			{
				if (!practitionerRoleExists(connection, user, practitioner.get().getIdElement()))
				{
					errors.add(
							"ResearchStudy.principalInvestigator corresponding PractitionerRole.practitioner not found");
				}
			}
			else
			{
				errors.add("ResearchStudy.principalInvestigator not resolved");
			}
		}
		else
		{
			errors.add("ResearchStudy.principalInvestigator missing");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	private Stream<Boolean> organizationsResolvable(Connection connection, User user, String referenceLocation,
			Stream<Reference> references)
	{
		return references.map(r -> organizationResolvable(connection, user, referenceLocation, r));
	}

	private boolean organizationResolvable(Connection connection, User user, String referenceLocation,
			Reference reference)
	{
		var ref = createIfLiteralInternalOrLogicalReference(referenceLocation, reference, Organization.class);
		return resolveReference(connection, user, ref).isPresent();
	}

	private Stream<Boolean> enrollmentsResolvable(Connection connection, User user, String referenceLocation,
			Stream<Reference> references)
	{
		return references.map(r -> enrollmentResolvable(connection, user, referenceLocation, r));
	}

	private boolean enrollmentResolvable(Connection connection, User user, String referenceLocation,
			Reference reference)
	{
		var ref = createIfLiteralInternalOrLogicalReference(referenceLocation, reference, Group.class);
		return resolveReference(connection, user, ref).isPresent();
	}

	private Optional<Resource> resolvePractitioner(Connection connection, User user, String referenceLocation,
			Reference reference)
	{
		var ref = createIfLiteralInternalOrLogicalReference(referenceLocation, reference, Practitioner.class);
		return resolveReference(connection, user, ref);
	}

	private boolean practitionerRoleExists(Connection connection, User user, IdType practitionerId)
	{
		PractitionerRoleDao dao = daoProvider.getPractitionerRoleDao();
		SearchQuery<PractitionerRole> query = dao.createSearchQuery(user, 0, 0).configureParameters(
				Map.of("practitioner", Collections.singletonList("Practitioner/" + practitionerId.getIdPart()),
						"active", Collections.singletonList("true")));

		try
		{
			return dao.search(query).getOverallCount() == 1;
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for PractitionerRole", e);
			return false;
		}
	}

	private boolean resourceExists(Connection connection, ResearchStudy newResource)
	{
		String identifierValue = newResource.getIdentifier().stream()
				.filter(i -> i.hasSystem() && i.hasValue() && RESEARCH_STUDY_IDENTIFIER.equals(i.getSystem()))
				.map(i -> i.getValue()).findFirst().orElseThrow();

		return researchStudyWithIdentifierExists(connection, identifierValue);
	}

	private boolean researchStudyWithIdentifierExists(Connection connection, String identifierValue)
	{
		Map<String, List<String>> queryParameters = Map.of("identifier",
				Collections.singletonList(RESEARCH_STUDY_IDENTIFIER + "|" + identifierValue));
		ResearchStudyDao dao = getDao();
		SearchQuery<ResearchStudy> query = dao.createSearchQueryWithoutUserFilter(0, 0)
				.configureParameters(queryParameters);
		try
		{
			PartialResult<ResearchStudy> result = dao.searchWithTransaction(connection, query);
			return result.getOverallCount() >= 1;
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for ResearchStudy with identifier", e);
			return false;
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, ResearchStudy existingResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Read of ResearchStudy authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else if (isRemoteUser(user))
		{
			if (isUserPartOfMeDic(user))
			{
				if (isCurrentUserPartOfReferencedOrganizations(connection, user,
						"ResearchStudy.extension(url:" + ResearchStudyHelper.PARTICIPATING_MEDIC_EXTENSION_URL + ")",
						ResearchStudyHelper.getParticipatingMedicReferences(existingResource)))
				{
					logger.info("Read of ResearchStudy authorized, ResearchStudy.extension(url:"
							+ ResearchStudyHelper.PARTICIPATING_MEDIC_EXTENSION_URL
							+ ") reference could be resolved and remote user '{}' part of referenced MeDIC organization",
							user.getName());
					return Optional.of("remote user, ResearchStudy.extension(url:"
							+ ResearchStudyHelper.PARTICIPATING_MEDIC_EXTENSION_URL
							+ ") resolved and user part of referenced MeDIC organization");
				}
				else
				{
					logger.warn(
							"Read of ResearchStudy unauthorized, user not part of referenced MeDIC or reference in extension could not be resolved");
					return Optional.empty();
				}
			}
			else if (isUserPartOfTtp(user))
			{
				if (isCurrentUserPartOfReferencedOrganization(connection, user,
						"ResearchStudy.extension(url:" + ResearchStudyHelper.PARTICIPATING_TTP_EXTENSION_URL + ")",
						ResearchStudyHelper.getParticipatingTtpReference(existingResource).orElse(null)))
				{
					logger.info("Read of ResearchStudy authorized, ResearchStudy.extension(url:"
							+ ResearchStudyHelper.PARTICIPATING_TTP_EXTENSION_URL
							+ ") reference could be resolved and remote user '{}' part of referenced TTP organization",
							user.getName());
					return Optional.of("remote user, ResearchStudy.extension(url:"
							+ ResearchStudyHelper.PARTICIPATING_TTP_EXTENSION_URL
							+ ") resolved and user part of referenced TTP organization");
				}
				else
				{
					logger.warn(
							"Read of ResearchStudy unauthorized, user not part of referenced TTP or reference in extension could not be resolved");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Read of ResearchStudy unauthorized, user not part of MeDIC or TTP");
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Read of ResearchStudy unauthorized, not a local or remote user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, ResearchStudy oldResource,
			ResearchStudy newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(connection, user, newResource);
			if (errors.isEmpty())
			{
				if (isSame(oldResource, newResource))
				{
					logger.info(
							"Update of ResearchStudy authorized for local user '{}', identifier same as existing ResearchStudy",
							user.getName());
					return Optional.of("local user; identifier same as existing ResearchStudy");

				}
				else if (!resourceExists(connection, newResource))
				{
					logger.info(
							"Update of ResearchStudy authorized for local user '{}', other ResearchStudy with identifier does not exist",
							user.getName());
					return Optional.of("local user; other ResearchStudy with identifier does not exist yet");
				}
				else
				{
					logger.warn(
							"Update of ResearchStudy unauthorized, other ResearchStudy with identifier already exists");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Update of ResearchStudy unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Update of ResearchStudy unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private boolean isSame(ResearchStudy oldResource, ResearchStudy newResource)
	{
		String oldIdentifierValue = oldResource.getIdentifier().stream()
				.filter(i -> RESEARCH_STUDY_IDENTIFIER.equals(i.getSystem())).map(i -> i.getValue()).findFirst()
				.orElseThrow();

		String newIdentifierValue = newResource.getIdentifier().stream()
				.filter(i -> RESEARCH_STUDY_IDENTIFIER.equals(i.getSystem())).map(i -> i.getValue()).findFirst()
				.orElseThrow();

		return oldIdentifierValue.equals(newIdentifierValue);
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, ResearchStudy oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of ResearchStudy authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of ResearchStudy unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(Connection connection, User user)
	{
		logger.info("Search of ResearchStudy authorized for {} user '{}', will be fitered by users organization {}",
				user.getRole(), user.getName(), user.getOrganization().getIdElement().getValueAsString());
		return Optional.of("Allowed for all, filtered by users organization");
	}
}
