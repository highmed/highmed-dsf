package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.dao.NamingSystemDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.NamingSystem.NamingSystemIdentifierType;
import org.hl7.fhir.r4.model.NamingSystem.NamingSystemUniqueIdComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamingSystemAuthorizationRule extends AbstractMetaTagAuthorizationRule<NamingSystem, NamingSystemDao>
{
	private static final Logger logger = LoggerFactory.getLogger(NamingSystemAuthorizationRule.class);

	public NamingSystemAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider,
			ReadAccessHelper readAccessHelper, ParameterConverter parameterConverter)
	{
		super(NamingSystem.class, daoProvider, serverBase, referenceResolver, organizationProvider, readAccessHelper,
				parameterConverter);
	}

	@Override
	protected Optional<String> newResourceOkForCreate(Connection connection, User user, NamingSystem newResource)
	{
		return newResourceOk(connection, user, newResource);
	}

	@Override
	protected Optional<String> newResourceOkForUpdate(Connection connection, User user, NamingSystem newResource)
	{
		return newResourceOk(connection, user, newResource);
	}

	private Optional<String> newResourceOk(Connection connection, User user, NamingSystem newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (newResource.hasStatus())
		{
			if (!EnumSet.of(PublicationStatus.DRAFT, PublicationStatus.ACTIVE, PublicationStatus.RETIRED)
					.contains(newResource.getStatus()))
			{
				errors.add("NamingSystem.status not one of DRAFT, ACTIVE or RETIRED");
			}
		}
		else
		{
			errors.add("NamingSystem.status not defined");
		}

		if (!newResource.hasName())
		{
			errors.add("NamingSystem.name not defined");
		}

		if (!hasValidReadAccessTag(connection, newResource))
		{
			errors.add("NamingSystem is missing valid read access tag");
		}

		if (!hasOnlyUniqueUriUniqueIds(newResource))
		{
			errors.add("NamingSystem has non unique ids");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	private boolean hasOnlyUniqueUriUniqueIds(NamingSystem newResource)
	{
		final long uriCount = newResource.getUniqueId().stream().filter(NamingSystemUniqueIdComponent::hasType)
				.filter(id -> NamingSystemIdentifierType.URI.equals(id.getType())).count();
		final long distinctUriCount = newResource.getUniqueId().stream().filter(NamingSystemUniqueIdComponent::hasType)
				.filter(id -> NamingSystemIdentifierType.URI.equals(id.getType()))
				.map(NamingSystemUniqueIdComponent::getValue).distinct().count();

		return uriCount == distinctUriCount;
	}

	@Override
	protected boolean resourceExists(Connection connection, NamingSystem newResource)
	{
		try
		{
			boolean withNameExists = getDao().readByNameWithTransaction(connection, newResource.getName()).isPresent();

			boolean withUniqueIdUriExists = newResource.getUniqueId().stream()
					.filter(NamingSystemUniqueIdComponent::hasType)
					.filter(id -> NamingSystemIdentifierType.URI.equals(id.getType()))
					.filter(NamingSystemUniqueIdComponent::hasValue).map(NamingSystemUniqueIdComponent::getValue)
					.anyMatch(resourceWithUniquIdUriEntryExists(connection));

			return withNameExists || withUniqueIdUriExists;
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for NamingSystem", e);
			return false;
		}
	}

	private Predicate<String> resourceWithUniquIdUriEntryExists(Connection connection)
	{
		return uniqueIdValue ->
		{
			try
			{
				return getDao().existsWithUniqueIdUriEntry(connection, uniqueIdValue);
			}
			catch (SQLException e)
			{
				logger.warn("Error while searching for NamingSystem", e);
				return false;
			}
		};
	}

	@Override
	protected boolean modificationsOk(Connection connection, NamingSystem oldResource, NamingSystem newResource)
	{
		return oldResource.getName().equals(newResource.getName());
	}
}
