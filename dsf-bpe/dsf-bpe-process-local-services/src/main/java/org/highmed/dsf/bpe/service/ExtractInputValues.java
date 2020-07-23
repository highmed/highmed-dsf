package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.BloomFilterConfig;
import org.highmed.dsf.fhir.variables.BloomFilterConfigValues;
import org.highmed.dsf.fhir.variables.FhirResourcesListValues;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

public class ExtractInputValues extends AbstractServiceDelegate implements InitializingBean
{
	public ExtractInputValues(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);

		Stream<String> queries = getQueries(task);
		List<Group> cohortDefinitions = getCohortDefinitions(queries);
		execution.setVariable(Constants.VARIABLE_COHORTS, FhirResourcesListValues.create(cohortDefinitions));

		boolean needsConsentCheck = getNeedsConsentCheck(task);
		execution.setVariable(Constants.VARIABLE_NEEDS_CONSENT_CHECK, needsConsentCheck);

		boolean needsRecordLinkage = getNeedsRecordLinkageCheck(task);
		execution.setVariable(Constants.VARIABLE_NEEDS_RECORD_LINKAGE, needsRecordLinkage);

		if (needsRecordLinkage)
		{
			BloomFilterConfig bloomFilterConfig = getBloomFilterConfig(task);
			execution.setVariable(Constants.VARIABLE_BLOOM_FILTER_CONFIG,
					BloomFilterConfigValues.create(bloomFilterConfig));
		}
	}

	private Stream<String> getQueries(Task task)
	{
		return getTaskHelper().getInputParameterStringValues(task, Constants.CODESYSTEM_QUERY_TYPE,
				Constants.CODESYSTEM_QUERY_TYPE_AQL);
	}

	private List<Group> getCohortDefinitions(Stream<String> queries)
	{
		return queries.map(q -> {
			Group group = new Group();
			group.setIdElement(new IdType(UUID.randomUUID().toString()));
			group.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/query").setValue(new Expression()
					.setLanguageElement(Constants.AQL_QUERY_TYPE).setExpression(q));
			return group;
		}).collect(Collectors.toList());
	}

	private boolean getNeedsConsentCheck(Task task)
	{
		return getTaskHelper().getFirstInputParameterBooleanValue(task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
				Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK).orElseThrow(
				() -> new IllegalArgumentException(
						"NeedsConsentCheck boolean is not set in task with id='" + task.getId()
								+ "', this error should " + "have been caught by resource validation"));
	}

	private boolean getNeedsRecordLinkageCheck(Task task)
	{
		return getTaskHelper().getFirstInputParameterBooleanValue(task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
				Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE).orElseThrow(
				() -> new IllegalArgumentException(
						"NeedsRecordLinkage boolean is not set in task with id='" + task.getId()
								+ "', this error should " + "have been caught by resource validation"));
	}

	private BloomFilterConfig getBloomFilterConfig(Task task)
	{
		return BloomFilterConfig.fromBytes(getTaskHelper()
				.getFirstInputParameterByteValue(task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_BLOOM_FILTER_CONFIG).orElseThrow(
						() -> new IllegalArgumentException(
								"BloomFilterConfig byte[] is not set in task with id='" + task.getId()
										+ "', this error should " + "have been caught by resource validation")));
	}
}
