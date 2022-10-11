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
import org.highmed.dsf.fhir.dao.OrganizationAffiliationDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganizationAffiliationAuthorizationRule
		extends AbstractMetaTagAuthorizationRule<OrganizationAffiliation, OrganizationAffiliationDao>
{
	private static final Logger logger = LoggerFactory.getLogger(OrganizationAffiliationAuthorizationRule.class);

	public OrganizationAffiliationAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider,
			ReadAccessHelper readAccessHelper, ParameterConverter parameterConverter)
	{
		super(OrganizationAffiliation.class, daoProvider, serverBase, referenceResolver, organizationProvider,
				readAccessHelper, parameterConverter);
	}

	@Override
	protected Optional<String> newResourceOkForCreate(Connection connection, User user,
			OrganizationAffiliation newResource)
	{
		return newResourceOk(connection, user, newResource);
	}

	@Override
	protected Optional<String> newResourceOkForUpdate(Connection connection, User user,
			OrganizationAffiliation newResource)
	{
		return newResourceOk(connection, user, newResource);
	}

	private Optional<String> newResourceOk(Connection connection, User user, OrganizationAffiliation newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (newResource.hasOrganization())
		{
			if (!newResource.getOrganization().hasReference())
			{
				errors.add("OrganizationAffiliation.organization.reference missing");
			}
		}
		else
		{
			errors.add("OrganizationAffiliation.organization missing");
		}

		if (newResource.hasParticipatingOrganization())
		{
			if (!newResource.getParticipatingOrganization().hasReference())
			{
				errors.add("OrganizationAffiliation.participatingOrganization.reference missing");
			}
		}
		else
		{
			errors.add("OrganizationAffiliation.participatingOrganization missing");
		}

		if (!hasValidReadAccessTag(connection, newResource))
		{
			errors.add("OrganizationAffiliation is missing valid read access tag");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	@Override
	protected boolean resourceExists(Connection connection, OrganizationAffiliation newResource)
	{
		return organizationAffiliationWithParentAndMemberExists(connection, newResource);
	}

	private boolean organizationAffiliationWithParentAndMemberExists(Connection connection,
			OrganizationAffiliation newResource)
	{
		Map<String, List<String>> queryParameters = Map.of("primary-organization",
				Collections.singletonList(newResource.getOrganization().getReference()), "participating-organization",
				Collections.singletonList(newResource.getParticipatingOrganization().getReference()));
		OrganizationAffiliationDao dao = getDao();
		SearchQuery<OrganizationAffiliation> query = dao.createSearchQueryWithoutUserFilter(0, 0)
				.configureParameters(queryParameters);

		if (!query.getUnsupportedQueryParameters(queryParameters).isEmpty())
			return false;

		try
		{
			PartialResult<OrganizationAffiliation> result = dao.searchWithTransaction(connection, query);
			return result.getTotal() >= 1;
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for Endpoint with address", e);
			return false;
		}
	}

	@Override
	protected boolean modificationsOk(Connection connection, OrganizationAffiliation oldResource,
			OrganizationAffiliation newResource)
	{
		return isParentSame(oldResource, newResource) && isMemberSame(oldResource, newResource);
	}

	private boolean isParentSame(OrganizationAffiliation oldResource, OrganizationAffiliation newResource)
	{
		return oldResource.getOrganization().getReference().equals(newResource.getOrganization().getReference());
	}

	private boolean isMemberSame(OrganizationAffiliation oldResource, OrganizationAffiliation newResource)
	{
		return oldResource.getParticipatingOrganization().getReference()
				.equals(newResource.getParticipatingOrganization().getReference());
	}
}
