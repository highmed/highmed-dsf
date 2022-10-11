package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.dao.BundleDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Bundle;

public class BundleAuthorizationRule extends AbstractMetaTagAuthorizationRule<Bundle, BundleDao>
{
	public BundleAuthorizationRule(DaoProvider daoProvider, String serverBase, ReferenceResolver referenceResolver,
			OrganizationProvider organizationProvider, ReadAccessHelper readAccessHelper,
			ParameterConverter parameterConverter)
	{
		super(Bundle.class, daoProvider, serverBase, referenceResolver, organizationProvider, readAccessHelper,
				parameterConverter);
	}

	@Override
	protected Optional<String> newResourceOkForCreate(Connection connection, User user, Bundle newResource)
	{
		return newResourceOk(connection, user, newResource);
	}

	@Override
	protected Optional<String> newResourceOkForUpdate(Connection connection, User user, Bundle newResource)
	{
		return newResourceOk(connection, user, newResource);
	}

	private Optional<String> newResourceOk(Connection connection, User user, Bundle newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (!hasValidReadAccessTag(connection, newResource))
		{
			errors.add("Bundle is missing valid read access tag");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	@Override
	protected boolean resourceExists(Connection connection, Bundle newResource)
	{
		// no unique criteria for Bundle
		return false;
	}

	@Override
	protected boolean modificationsOk(Connection connection, Bundle oldResource, Bundle newResource)
	{
		// no unique criteria for Bundle
		return true;
	}
}
