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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestionnaireResponseAuthorizationRule
		extends AbstractMetaTagAuthorizationRule<QuestionnaireResponse, QuestionnaireResponseDao>
{
	private static final Logger logger = LoggerFactory.getLogger(QuestionnaireResponseAuthorizationRule.class);

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

		if (newResource.hasStatus())
		{
			boolean newResourceOk = QuestionnaireResponse.QuestionnaireResponseStatus.INPROGRESS
					.equals(newResource.getStatus()) && "1".equals(newResource.getMeta().getVersionId());

			if (newResourceOk)
			{
				errors.add("QuestionnaireResponse.status not in-progress and version 1");
			}

			boolean updateResourceOk = (QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED
					.equals(newResource.getStatus())
					|| QuestionnaireResponse.QuestionnaireResponseStatus.STOPPED.equals(newResource.getStatus()))
					&& "2".equals(newResource.getMeta().getVersionId());

			if (updateResourceOk)
			{
				errors.add("QuestionnaireResponse.status not (completed or stopped) and version 2");
			}
		}
		else
		{
			errors.add("QuestionnaireResponse.status missing");
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
		if (QuestionnaireResponse.QuestionnaireResponseStatus.INPROGRESS.equals(oldResource.getStatus())
				&& (QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED.equals(newResource.getStatus())
						|| QuestionnaireResponse.QuestionnaireResponseStatus.STOPPED.equals(newResource.getStatus())))
		{
			return true;
		}
		else
		{
			logger.warn(
					"Modifications only allowed if status changes from '{}' to '{}', current status of old resource is '{}' and of new resource is '{}'",
					QuestionnaireResponse.QuestionnaireResponseStatus.INPROGRESS,
					QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED + "|"
							+ QuestionnaireResponse.QuestionnaireResponseStatus.STOPPED,
					oldResource.getStatus(), newResource.getStatus());
			return false;
		}
	}
}
