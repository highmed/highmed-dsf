package org.highmed.bpe.message;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.bpe.Constants;
import org.highmed.fhir.client.WebserviceClientProvider;
import org.highmed.fhir.organization.OrganizationProvider;
import org.highmed.fhir.task.AbstractTaskMessageSend;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendRequest extends AbstractTaskMessageSend
{
	private static final Logger logger = LoggerFactory.getLogger(SendRequest.class);

	private static final String PARAMETER_RESOURCE_CRITERIA = "resource-criteria";

	public SendRequest(OrganizationProvider organizationProvider, WebserviceClientProvider clientProvider)
	{
		super(organizationProvider, clientProvider);
	}

	@Override
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		@SuppressWarnings("unchecked")
		Map<String, List<String>> queryParameters = (Map<String, List<String>>) execution
				.getVariable(Constants.VARIABLE_QUERY_PARAMETERS);

		List<String> resourceCriteria = queryParameters.get(PARAMETER_RESOURCE_CRITERIA);
		logger.debug(PARAMETER_RESOURCE_CRITERIA + ": {}", resourceCriteria);

		return resourceCriteria.stream().map(this::toInputParameter);
	}

	private ParameterComponent toInputParameter(String resourcecriteria)
	{
		ParameterComponent input = new ParameterComponent(
				new CodeableConcept(new Coding(Constants.CODESYSTEM_HIGHMED_BPMN,
						Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_RESOURCE_SEARCH_CRITERIA, null)),
				new StringType(resourcecriteria));

		return input;
	}
}
