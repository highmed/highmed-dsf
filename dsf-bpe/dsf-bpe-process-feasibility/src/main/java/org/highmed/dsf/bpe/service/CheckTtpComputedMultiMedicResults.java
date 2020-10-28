package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE;
import static org.highmed.dsf.bpe.ConstantsBase.VARIABLE_HAS_ERROR;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.ConstantsFeasibility;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResults;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Output;
import org.highmed.dsf.fhir.variables.Outputs;
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
		Task task = (Task) execution.getVariable(ConstantsBase.VARIABLE_LEADING_TASK);
		FinalFeasibilityQueryResults results = (FinalFeasibilityQueryResults) execution
				.getVariable(ConstantsFeasibility.VARIABLE_FINAL_QUERY_RESULTS);
		Outputs outputs = (Outputs) execution.getVariable(ConstantsBase.VARIABLE_PROCESS_OUTPUTS);

		List<FinalFeasibilityQueryResult> filteredResults = filterResults(task, results, outputs);
		boolean hasBlockingError = checkResultsSize(task, filteredResults);

		execution.setVariable(VARIABLE_HAS_ERROR, hasBlockingError);
		execution.setVariable(ConstantsFeasibility.VARIABLE_FINAL_QUERY_RESULTS,
				FinalFeasibilityQueryResultsValues.create(new FinalFeasibilityQueryResults(filteredResults)));
		execution.setVariable(ConstantsBase.VARIABLE_PROCESS_OUTPUTS, outputs);
	}

	private List<FinalFeasibilityQueryResult> filterResults(Task task, FinalFeasibilityQueryResults results,
			Outputs outputs)
	{
		String taskId = task.getId();
		String businessKey = getTaskHelper().getFirstInputParameterStringValue(task, CODESYSTEM_HIGHMED_BPMN,
				ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY).orElse(null);
		String correlationKey = getTaskHelper().getFirstInputParameterStringValue(task, CODESYSTEM_HIGHMED_BPMN,
				ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY).orElse(null);

		return results.getResults().stream().filter(result -> {
			if (result.getParticipatingMedics() < ConstantsFeasibility.MIN_PARTICIPATING_MEDICS)
			{
				logger.warn("Removed result with cohort id='{}' from feasibility request with task-id='{}', "
								+ "business-key='{}' and correlation-key='{}' because of not enough participating MeDICs",
						result.getCohortId(), taskId, businessKey, correlationKey);

				outputs.add(new Output(CODESYSTEM_HIGHMED_BPMN, CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE,
						"Removed result with cohort id='" + result.getCohortId()
								+ "' from feasibility request because of not enough participating MeDICs"));

				return false;
			}

			return true;
		}).collect(Collectors.toList());
	}

	private boolean checkResultsSize(Task task, List<FinalFeasibilityQueryResult> results)
	{
		String taskId = task.getId();
		String businessKey = getTaskHelper().getFirstInputParameterStringValue(task, CODESYSTEM_HIGHMED_BPMN,
				ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_BUSINESS_KEY).orElse(null);
		String correlationKey = getTaskHelper().getFirstInputParameterStringValue(task, CODESYSTEM_HIGHMED_BPMN,
				ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY).orElse(null);

		if (results.size() < 1)
		{
			logger.warn("Did not receive enough results from participating MeDICs for any cohort definition in the "
							+ "feasibility request with task-id='{}', business-key='{}' " + "and correlation-key='{}'", taskId,
					businessKey, correlationKey);

			return true;
		}

		return false;
	}
}
