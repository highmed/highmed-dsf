package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;
import static org.highmed.dsf.bpe.ConstantsFeasibility.ERROR_CODE_MULTI_MEDIC_RESULT;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.ConstantsFeasibility;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResults;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckTtpComputedMultiMedicResults extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckTtpComputedMultiMedicResults.class);

	public CheckTtpComputedMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task leadingTask = getLeadingTaskFromExecutionVariables();
		FinalFeasibilityQueryResults results = (FinalFeasibilityQueryResults) execution
				.getVariable(ConstantsFeasibility.VARIABLE_FINAL_QUERY_RESULTS);

		List<FinalFeasibilityQueryResult> resultsWithEnoughParticipatingMedics = filterResultsByParticipatingMedics(
				leadingTask, results);

		execution.setVariable(ConstantsFeasibility.VARIABLE_FINAL_QUERY_RESULTS, FinalFeasibilityQueryResultsValues
				.create(new FinalFeasibilityQueryResults(resultsWithEnoughParticipatingMedics)));

		boolean existsAtLeastOneResult = checkIfAtLeastOneResultExists(leadingTask,
				resultsWithEnoughParticipatingMedics);

		if (!existsAtLeastOneResult)
			throw new BpmnError(ERROR_CODE_MULTI_MEDIC_RESULT);
	}

	private List<FinalFeasibilityQueryResult> filterResultsByParticipatingMedics(Task leadingTask,
			FinalFeasibilityQueryResults results)
	{
		String taskId = leadingTask.getId();
		String businessKey = getTaskHelper().getFirstInputParameterStringValue(leadingTask, CODESYSTEM_HIGHMED_BPMN,
				ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY).orElse(null);
		String correlationKey = getTaskHelper().getFirstInputParameterStringValue(leadingTask, CODESYSTEM_HIGHMED_BPMN,
				ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY).orElse(null);

		return results.getResults().stream().filter(result -> {
			if (result.getParticipatingMedics() < ConstantsFeasibility.MIN_PARTICIPATING_MEDICS)
			{
				logger.warn("Removed result with cohort id='{}' from feasibility request with task-id='{}', "
								+ "business-key='{}' and correlation-key='{}' because of not enough participating MeDICs",
						result.getCohortId(), taskId, businessKey, correlationKey);

				leadingTask.getOutput().add(getTaskHelper()
						.createOutput(CODESYSTEM_HIGHMED_BPMN, CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR,
								"Removed result with cohort id='" + result.getCohortId()
										+ "' from feasibility request because of not enough participating MeDICs"));

				return false;
			}

			return true;
		}).collect(Collectors.toList());
	}

	private boolean checkIfAtLeastOneResultExists(Task leadingTask, List<FinalFeasibilityQueryResult> results)
	{
		String taskId = leadingTask.getId();
		String businessKey = getTaskHelper().getFirstInputParameterStringValue(leadingTask, CODESYSTEM_HIGHMED_BPMN,
				ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY).orElse(null);
		String correlationKey = getTaskHelper().getFirstInputParameterStringValue(leadingTask, CODESYSTEM_HIGHMED_BPMN,
				ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY).orElse(null);

		if (results.size() < 1)
		{
			logger.warn("Did not receive enough results from participating MeDICs for any cohort definition in the "
							+ "feasibility request with task-id='{}', business-key='{}' " + "and correlation-key='{}'", taskId,
					businessKey, correlationKey);

			leadingTask.getOutput().add(getTaskHelper()
					.createOutput(CODESYSTEM_HIGHMED_BPMN, CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR,
							"Did not receive enough results from participating MeDICs for any cohort definition"));

			return false;
		}

		return true;
	}
}
