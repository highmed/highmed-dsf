package org.highmed.dsf.bpe.message;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.variables.ConstantsFeasibility;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResult;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResults;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class SendSingleMedicResults extends AbstractTaskMessageSend
{
	private static final Logger logger = LoggerFactory.getLogger(SendSingleMedicResults.class);

	public SendSingleMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		FeasibilityQueryResults results = (FeasibilityQueryResults) execution
				.getVariable(ConstantsBase.VARIABLE_QUERY_RESULTS);

		return results.getResults().stream().map(result -> toInput(result));
	}

	private Task.ParameterComponent toInput(FeasibilityQueryResult result)
	{
		if (result.isCohortSizeResult())
		{
			ParameterComponent input = getTaskHelper().createInputUnsignedInt(ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
					ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT, result.getCohortSize());
			input.addExtension(createCohortIdExtension(result.getCohortId()));
			return input;
		}
		else if (result.isIdResultSetUrlResult())
		{
			ParameterComponent input = getTaskHelper().createInput(ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
					ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT_REFERENCE,
					new Reference(result.getResultSetUrl()));
			input.addExtension(createCohortIdExtension(result.getCohortId()));
			return input;
		}
		else
		{
			logger.warn("Unexpected result (not a cohort-size or ResultSet URL result) for cohort with ID "
					+ result.getCohortId());
			throw new RuntimeException(
					"Unexpected result (not a cohort-size or ResultSet URL result) for cohort with ID "
							+ result.getCohortId());
		}
	}

	private Extension createCohortIdExtension(String cohortId)
	{
		return new Extension(ConstantsBase.EXTENSION_GROUP_ID_URI, new Reference(cohortId));
	}
}
