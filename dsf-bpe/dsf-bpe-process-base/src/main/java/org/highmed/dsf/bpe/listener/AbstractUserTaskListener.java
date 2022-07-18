package org.highmed.dsf.bpe.listener;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_URL;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.questionnaire.QuestionnaireResponseHelper;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractUserTaskListener implements TaskListener, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractUserTaskListener.class);

	private final FhirWebserviceClientProvider clientProvider;
	private final OrganizationProvider organizationProvider;
	private final QuestionnaireResponseHelper questionnaireResponseHelper;
	private final ReadAccessHelper readAccessHelper;

	public AbstractUserTaskListener(FhirWebserviceClientProvider clientProvider,
			OrganizationProvider organizationProvider, QuestionnaireResponseHelper questionnaireResponseHelper,
			ReadAccessHelper readAccessHelper)
	{
		this.clientProvider = clientProvider;
		this.organizationProvider = organizationProvider;
		this.questionnaireResponseHelper = questionnaireResponseHelper;
		this.readAccessHelper = readAccessHelper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(clientProvider, "clientProvider");
		Objects.requireNonNull(organizationProvider, "organizationProvider");
		Objects.requireNonNull(questionnaireResponseHelper, "questionnaireResponseHelper");
		Objects.requireNonNull(readAccessHelper, "readAccessHelper");
	}

	@Override
	public void notify(DelegateTask userTask)
	{
		try
		{
			String questionnaireUrl = (String) userTask.getExecution()
					.getVariable(BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_URL);
			String businessKey = userTask.getExecution().getBusinessKey();
			String taskId = userTask.getId();

			QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
			questionnaireResponse.setQuestionnaire(questionnaireUrl);
			questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.INPROGRESS);

			questionnaireResponse.setSubject(new Reference().setType(ResourceType.Organization.name())
					.setIdentifier(organizationProvider.getLocalIdentifier()));

			questionnaireResponseHelper.addItemLeave(questionnaireResponse,
					CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY, new StringType(businessKey));
			questionnaireResponseHelper.addItemLeave(questionnaireResponse,
					CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID, new StringType(taskId));

			// TODO: read questionnaire and add items

			readAccessHelper.addLocal(questionnaireResponse);

			modifyQuestionnaireResponse(userTask, questionnaireResponse);

			clientProvider.getLocalWebserviceClient().create(questionnaireResponse);
		}
		catch (Exception exception)
		{
			// TODO implement
		}
	}

	protected void modifyQuestionnaireResponse(DelegateTask userTask, QuestionnaireResponse questionnaireResponse)
	{
	}
}
