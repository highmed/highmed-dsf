package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.dao.OrganizationDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganizationAuthorizationRule extends AbstractMetaTagAuthorizationRule<Organization, OrganizationDao>
{
	private static final Logger logger = LoggerFactory.getLogger(OrganizationAuthorizationRule.class);

	private static final String HIGHMED_ORGANIZATION = "http://highmed.org/fhir/StructureDefinition/organization";
	private static final String EXTENSION_THUMBPRINT_URL = "http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint";
	private static final String EXTENSION_THUMBPRINT_VALUE_PATTERN_STRING = "[a-f0-9]{128}";
	private static final Pattern EXTENSION_THUMBPRINT_VALUE_PATTERN = Pattern
			.compile(EXTENSION_THUMBPRINT_VALUE_PATTERN_STRING);

	public OrganizationAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider,
			ReadAccessHelper readAccessHelper, ParameterConverter parameterConverter)
	{
		super(Organization.class, daoProvider, serverBase, referenceResolver, organizationProvider, readAccessHelper,
				parameterConverter);
	}

	@Override
	protected Optional<String> newResourceOkForCreate(Connection connection, User user, Organization newResource)
	{
		return newResourceOk(connection, user, newResource);
	}

	@Override
	protected Optional<String> newResourceOkForUpdate(Connection connection, User user, Organization newResource)
	{
		return newResourceOk(connection, user, newResource);
	}

	private Optional<String> newResourceOk(Connection connection, User user, Organization newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (newResource.hasIdentifier())
		{
			if (newResource.getIdentifier().stream()
					.filter(i -> i.hasSystem() && i.hasValue() && ORGANIZATION_IDENTIFIER_SYSTEM.equals(i.getSystem()))
					.count() != 1)
			{
				errors.add("Organization.identifier one with system '" + ORGANIZATION_IDENTIFIER_SYSTEM
						+ "' and non empty value expected");
			}
		}
		else
		{
			errors.add("Organization.identifier missing");
		}

		if (newResource.hasExtension() && newResource.getExtension().stream().filter(Extension::hasUrl)
				.map(Extension::getUrl).anyMatch(url -> EXTENSION_THUMBPRINT_URL.equals(url)))
		{
			if (!newResource.getExtension().stream().filter(Extension::hasUrl)
					.filter(e -> EXTENSION_THUMBPRINT_URL.equals(e.getUrl()))
					.allMatch(e -> e.hasValue() && e.getValue() instanceof StringType
							&& EXTENSION_THUMBPRINT_VALUE_PATTERN.matcher(((StringType) e.getValue()).getValue())
									.matches()))
			{
				errors.add("Organization with '" + EXTENSION_THUMBPRINT_URL + "' has value not matching pattern: "
						+ EXTENSION_THUMBPRINT_VALUE_PATTERN_STRING);
			}
		}

		if (!hasValidReadAccessTag(connection, newResource))
		{
			errors.add("Organization is missing authorization tag");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	@Override
	protected boolean resourceExists(Connection connection, Organization newResource)
	{
		Identifier organizationIdentifier = newResource.getIdentifier().stream()
				.filter(i -> i.hasSystem() && i.hasValue() && ORGANIZATION_IDENTIFIER_SYSTEM.equals(i.getSystem()))
				.findFirst().orElseThrow();

		return (newResource.getMeta().hasProfile(HIGHMED_ORGANIZATION)
				&& resourceExistsWithThumbprint(connection, newResource, Collections.emptyList()))
				|| organizationWithIdentifierExists(connection, organizationIdentifier);
	}

	private Stream<String> getThumbprints(Organization organization)
	{
		return organization.getExtension().stream().filter(e -> EXTENSION_THUMBPRINT_URL.equals(e.getUrl()))
				.map(e -> ((StringType) e.getValue()).getValue());
	}

	private boolean resourceExistsWithThumbprint(Connection connection, Organization newResource,
			List<String> noCheckNeeded)
	{
		return getThumbprints(newResource).filter(thumbprint -> !noCheckNeeded.contains(thumbprint))
				.map(thumbprint -> organizationWithThumbprintExists(connection, thumbprint)).anyMatch(b -> b);
	}

	private boolean organizationWithThumbprintExists(Connection connection, String thumbprint)
	{
		try
		{
			OrganizationDao dao = getDao();
			return dao.existsNotDeletedByThumbprintWithTransaction(connection, thumbprint);
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for Organization with thumbprint", e);
			return false;
		}
	}

	@Override
	protected boolean modificationsOk(Connection connection, Organization oldResource, Organization newResource)
	{
		return isIdentifierSame(oldResource, newResource) && !resourceExistsWithThumbprint(connection, newResource,
				getThumbprints(oldResource).collect(Collectors.toList()));
	}

	private boolean isIdentifierSame(Organization oldResource, Organization newResource)
	{
		String oldIdentifierValue = oldResource.getIdentifier().stream()
				.filter(i -> ORGANIZATION_IDENTIFIER_SYSTEM.equals(i.getSystem())).map(i -> i.getValue()).findFirst()
				.orElseThrow();

		String newIdentifierValue = newResource.getIdentifier().stream()
				.filter(i -> ORGANIZATION_IDENTIFIER_SYSTEM.equals(i.getSystem())).map(i -> i.getValue()).findFirst()
				.orElseThrow();

		return oldIdentifierValue.equals(newIdentifierValue);
	}
}
