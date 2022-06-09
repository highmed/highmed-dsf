package org.highmed.dsf.bpe.listener;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_URL;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_TASK_ID;

import java.util.Date;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractTaskListener implements TaskListener, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractTaskListener.class);

	private final FhirWebserviceClientProvider clientProvider;
	private final OrganizationProvider organizationProvider;
	private final ReadAccessHelper readAccessHelper;

	public AbstractTaskListener(FhirWebserviceClientProvider clientProvider, OrganizationProvider organizationProvider,
			ReadAccessHelper readAccessHelper)
	{
		this.clientProvider = clientProvider;
		this.organizationProvider = organizationProvider;
		this.readAccessHelper = readAccessHelper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(clientProvider, "clientProvider");
		Objects.requireNonNull(organizationProvider, "organizationProvider");
		Objects.requireNonNull(readAccessHelper, "readAccessHelper");
	}

	@Override
	public void notify(DelegateTask task)
	{
		try
		{
			String questionnaireUrl = (String) task.getExecution()
					.getVariable(BPMN_EXECUTION_VARIABLE_QUESTIONNAIRE_URL);
			String businessKey = task.getExecution().getBusinessKey();
			String taskId = task.getId();

			QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
			questionnaireResponse.setQuestionnaire(questionnaireUrl);
			questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.INPROGRESS);
			questionnaireResponse.setAuthored(new Date());

			questionnaireResponse.setSubject(new Reference().setType(ResourceType.Organization.name())
					.setIdentifier(organizationProvider.getLocalIdentifier()));

			questionnaireResponse.addItem().setLinkId(CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY).addAnswer()
					.setValue(new StringType(businessKey));
			questionnaireResponse.addItem().setLinkId(CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_TASK_ID).addAnswer()
					.setValue(new StringType(taskId));

			readAccessHelper.addLocal(questionnaireResponse);

			modifyQuestionnaireResponse(task, questionnaireResponse);

			clientProvider.getLocalWebserviceClient().create(questionnaireResponse);
		}
		catch (Exception exception)
		{
			// TODO implement
		}
	}

	protected void modifyQuestionnaireResponse(DelegateTask task, QuestionnaireResponse questionnaireResponse)
	{
	}
}
