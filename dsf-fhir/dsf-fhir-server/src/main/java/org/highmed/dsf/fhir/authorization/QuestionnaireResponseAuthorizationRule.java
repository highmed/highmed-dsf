package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.dao.QuestionnaireResponseDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.QuestionnaireResponse;

public class QuestionnaireResponseAuthorizationRule
		extends AbstractMetaTagAuthorizationRule<QuestionnaireResponse, QuestionnaireResponseDao>
{
	public QuestionnaireResponseAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider,
			ReadAccessHelper readAccessHelper, ParameterConverter parameterConverter)
	{
		super(QuestionnaireResponse.class, daoProvider, serverBase, referenceResolver, organizationProvider,
				readAccessHelper, parameterConverter);
	}

	protected Optional<String> newResourceOk(Connection connection, User user, QuestionnaireResponse newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (!hasValidReadAccessTag(connection, newResource))
		{
			errors.add("QuestionnaireResponse is missing valid read access tag");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	@Override
	protected boolean resourceExists(Connection connection, QuestionnaireResponse newResource)
	{
		// no unique criteria for QuestionnaireResponse
		return false;
	}

	@Override
	protected boolean modificationsOk(Connection connection, QuestionnaireResponse oldResource,
			QuestionnaireResponse newResource)
	{
		// no unique criteria for QuestionnaireResponse
		return true;
	}
}
