package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResult;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResults;
import org.highmed.dsf.fhir.variables.FinalFeasibilityQueryResult;
import org.highmed.dsf.fhir.variables.FinalFeasibilityQueryResults;
import org.highmed.dsf.fhir.variables.FinalFeasibilityQueryResultsValues;
import org.highmed.pseudonymization.domain.PersonWithMdat;
import org.highmed.pseudonymization.domain.impl.MatchedPersonImpl;
import org.highmed.pseudonymization.recordlinkage.FederatedMatcherImpl;
import org.highmed.pseudonymization.recordlinkage.MatchedPerson;
import org.highmed.pseudonymization.translation.ResultSetTranslatorFromMedicRbfOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecuteRecordLink extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(ExecuteRecordLink.class);

	private final ResultSetTranslatorFromMedicRbfOnly translator;

	public ExecuteRecordLink(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ResultSetTranslatorFromMedicRbfOnly translator)
	{
		super(clientProvider, taskHelper);

		this.translator = translator;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(translator, "translator");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		FeasibilityQueryResults results = (FeasibilityQueryResults) execution
				.getVariable(ConstantsBase.VARIABLE_QUERY_RESULTS);

		Map<String, List<FeasibilityQueryResult>> byCohortId = results.getResults().stream()
				.collect(Collectors.groupingBy(FeasibilityQueryResult::getCohortId));

		FederatedMatcherImpl<PersonWithMdat> matcher = createMatcher();

		List<FinalFeasibilityQueryResult> matchedResults = byCohortId.entrySet().stream()
				.map(e -> match(matcher, e.getKey(), e.getValue())).collect(Collectors.toList());

		execution.setVariable(ConstantsBase.VARIABLE_FINAL_QUERY_RESULTS,
				FinalFeasibilityQueryResultsValues.create(new FinalFeasibilityQueryResults(matchedResults)));
	}

	private FinalFeasibilityQueryResult match(FederatedMatcherImpl<PersonWithMdat> matcher, String cohortId,
			List<FeasibilityQueryResult> results)
	{
		logger.debug("Matching results for cohort {}", cohortId);

		List<List<PersonWithMdat>> persons = results.stream().map(this::translate).collect(Collectors.toList());

		Set<MatchedPerson<PersonWithMdat>> matchedPersons = matcher.matchPersons(persons);

		return new FinalFeasibilityQueryResult(cohortId,
				toInt(persons.stream().mapToInt(r -> r.size()).filter(cohortSize -> cohortSize > 0).count()),
				toInt(matchedPersons.size()));
	}

	private List<PersonWithMdat> translate(FeasibilityQueryResult result)
	{
		return translator.translate(result.getOrganizationIdentifier(), result.getResultSet());
	}

	protected FederatedMatcherImpl<PersonWithMdat> createMatcher()
	{
		return new FederatedMatcherImpl<>(MatchedPersonImpl::new);
	}

	private int toInt(long l)
	{
		if (l > Integer.MAX_VALUE)
			throw new IllegalArgumentException("long > " + Integer.MAX_VALUE);
		else
			return (int) l;
	}
}
