package org.highmed.dsf.fhir.authorization;

import static org.highmed.dsf.bpe.ConstantsBase.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
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
import org.hl7.fhir.r4.model.QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent;
import org.hl7.fhir.r4.model.QuestionnaireResponse.QuestionnaireResponseItemComponent;
import org.hl7.fhir.r4.model.QuestionnaireResponse.QuestionnaireResponseStatus;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestionnaireResponseAuthorizationRule
		extends AbstractAuthorizationRule<QuestionnaireResponse, QuestionnaireResponseDao>
{
	private static final Logger logger = LoggerFactory.getLogger(QuestionnaireResponseAuthorizationRule.class);

	private final ParameterConverter parameterConverter;

	public QuestionnaireResponseAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider,
			ReadAccessHelper readAccessHelper, ParameterConverter parameterConverter)
	{
		super(QuestionnaireResponse.class, daoProvider, serverBase, referenceResolver, organizationProvider,
				readAccessHelper);
		this.parameterConverter = parameterConverter;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(parameterConverter, "parameterConverter");
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, QuestionnaireResponse newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(connection, user, newResource,
					EnumSet.of(QuestionnaireResponseStatus.INPROGRESS));
			if (errors.isEmpty())
			{
				if (!resourceExists(connection, newResource))
				{
					logger.info(
							"Create of QuestionnaireResponse authorized for local user '{}', QuestionnaireResponse does not exist",
							user.getName());
					return Optional.of("local user, QuestionnaireResponse does not exist yet");
				}
				else
				{
					logger.warn("Create of QuestionnaireResponse unauthorized, QuestionnaireResponse already exists");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Create of QuestionnaireResponse unauthorized, {}", errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Create of QuestionnaireResponse unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private Optional<String> newResourceOk(Connection connection, User user, QuestionnaireResponse newResource,
			EnumSet<QuestionnaireResponseStatus> allowedStatus)
	{
		List<String> errors = new ArrayList<String>();

		if (newResource.hasStatus())
		{
			if (!allowedStatus.contains(newResource.getStatus()))
			{
				errors.add("QuestionnaireResponse.status not one of " + allowedStatus);
			}
		}
		else
		{
			errors.add("QuestionnaireResponse.status missing");
		}

		getItemAndValidate(newResource, CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID, errors);

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	private Optional<String> getItemAndValidate(QuestionnaireResponse newResource, String linkId, List<String> errors)
	{
		List<QuestionnaireResponseItemComponent> userTaskIds = newResource.getItem().stream()
				.filter(QuestionnaireResponseItemComponent::hasLinkId).filter(i -> linkId.equals(i.getLinkId()))
				.collect(Collectors.toList());

		if (userTaskIds.size() != 1)
		{
			if (errors != null)
				errors.add("QuestionnaireResponse.item('user-task-id') missing or more than one");

			return Optional.empty();
		}

		QuestionnaireResponseItemComponent item = userTaskIds.get(0);

		if (!item.hasAnswer() || item.getAnswer().size() != 1)
		{
			if (errors != null)
				errors.add("QuestionnaireResponse.item('user-task-id').answer missing or more than one");

			return Optional.empty();
		}

		QuestionnaireResponseItemAnswerComponent answer = item.getAnswerFirstRep();

		if (!answer.hasValue() || !(answer.getValue() instanceof StringType))
		{
			if (errors != null)
				errors.add("QuestionnaireResponse.item('user-task-id').answer.value missing or not a string");

			return Optional.empty();
		}

		StringType value = (StringType) answer.getValue();

		if (!value.hasValue())
		{
			if (errors != null)
				errors.add("QuestionnaireResponse.item('user-task-id').answer.value is blank");

			return Optional.empty();
		}

		return Optional.of(value.getValue());
	}

	private boolean resourceExists(Connection connection, QuestionnaireResponse newResource)
	{
		// TODO implement unique criteria based on UserTask.id when implemented as identifier
		return false;
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, QuestionnaireResponse existingResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Read of QuestionnaireResponse authorized for local user '{}'", user.getName());
			return Optional.of("task.restriction.recipient resolved and local user part of referenced organization");
		}
		else
		{
			logger.warn("Read of QuestionnaireResponse unauthorized, not a local user", user.getName());
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, QuestionnaireResponse oldResource,
			QuestionnaireResponse newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(connection, user, newResource,
					EnumSet.of(QuestionnaireResponseStatus.COMPLETED, QuestionnaireResponseStatus.STOPPED));
			if (errors.isEmpty())
			{
				if (modificationsOk(connection, oldResource, newResource))
				{
					logger.info("Update of QuestionnaireResponse authorized for local user '{}', modification allowed",
							user.getName());
					return Optional.of("local user; modification allowed");
				}
				else
				{
					logger.warn("Update of QuestionnaireResponse unauthorized, modification not allowed");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Update of QuestionnaireResponse unauthorized, {}", errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Update of QuestionnaireResponse unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private boolean modificationsOk(Connection connection, QuestionnaireResponse oldResource,
			QuestionnaireResponse newResource)
	{
		boolean statusModificationOk = QuestionnaireResponseStatus.INPROGRESS.equals(oldResource.getStatus())
				&& (QuestionnaireResponseStatus.COMPLETED.equals(newResource.getStatus())
						|| QuestionnaireResponseStatus.STOPPED.equals(newResource.getStatus()));

		if (!statusModificationOk)
			logger.warn(
					"Modifications only allowed if status changes from '{}' to '{}', current status of old resource is '{}' and of new resource is '{}'",
					QuestionnaireResponseStatus.INPROGRESS,
					QuestionnaireResponseStatus.COMPLETED + "|" + QuestionnaireResponseStatus.STOPPED,
					oldResource.getStatus(), newResource.getStatus());

		String oldUserTaskId = getItemAndValidate(oldResource, CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID,
				new ArrayList<>()).orElse(null);
		String newUserTaskId = getItemAndValidate(newResource, CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID,
				new ArrayList<>()).orElse(null);

		boolean userTaskIdOk = Objects.equals(oldUserTaskId, newUserTaskId);

		if (!userTaskIdOk)
			logger.warn(
					"Modifications only allowed if item.answer with linkId '{}' not changed, change from '{}' to '{}' not allowed",
					CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID, oldUserTaskId, newUserTaskId);

		String oldBusinessKey = getItemAndValidate(oldResource, CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY,
				new ArrayList<>()).orElse(null);
		String newBusinessKey = getItemAndValidate(newResource, CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY,
				new ArrayList<>()).orElse(null);

		boolean businesssKeyOk = Objects.equals(oldBusinessKey, newBusinessKey);

		if (!userTaskIdOk)
			logger.warn(
					"Modifications only allowed if item.answer with linkId '{}' not changed, change from '{}' to '{}' not allowed",
					CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY, oldUserTaskId, newUserTaskId);

		return statusModificationOk && userTaskIdOk && businesssKeyOk;
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, QuestionnaireResponse oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of QuestionnaireResponse authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of QuestionnaireResponse unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public final Optional<String> reasonSearchAllowed(User user)
	{
		logger.info("Search of QuestionnaireResponse authorized for {} user '{}', will be filtered by user role",
				user.getRole(), user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}

	@Override
	public final Optional<String> reasonHistoryAllowed(User user)
	{
		logger.info("History of {} authorized for {} user '{}', will be filtered by user role", user.getRole(),
				user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}

	@Override
	public Optional<String> reasonPermanentDeleteAllowed(Connection connection, User user,
			QuestionnaireResponse oldResource)
	{
		if (isLocalPermanentDeleteUser(user))
		{
			logger.info("Permanent delete of QuestionnaireResponse authorized for local delete user '{}'", resourceType,
					user.getName());
			return Optional.of("local delete user");
		}
		else
		{
			logger.warn("Permanent delete of QuestionnaireResponse unauthorized, not a local delete user");
			return Optional.empty();
		}
	}
}
