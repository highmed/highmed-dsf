package org.highmed.dsf.bpe.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.ConstantsFeasibility;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.springframework.beans.factory.InitializingBean;

public class ModifyQueries extends AbstractServiceDelegate implements InitializingBean
{
	private final String ehrIdColumnPath;

	public ModifyQueries(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper, String ehrIdColumnPath)
	{
		super(clientProvider, taskHelper);
		this.ehrIdColumnPath = ehrIdColumnPath;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		Objects.requireNonNull(ehrIdColumnPath, "ehrIdColumnPath");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Boolean needsConsentCheck = (Boolean) execution.getVariable(ConstantsFeasibility.VARIABLE_NEEDS_CONSENT_CHECK);
		Boolean needsRecordLinkage = (Boolean) execution
				.getVariable(ConstantsFeasibility.VARIABLE_NEEDS_RECORD_LINKAGE);
		boolean idQuery = Boolean.TRUE.equals(needsConsentCheck) || Boolean.TRUE.equals(needsRecordLinkage);

		if (idQuery)
		{
			// <groupId, query>
			@SuppressWarnings("unchecked")
			Map<String, String> queries = (Map<String, String>) execution
					.getVariable(ConstantsFeasibility.VARIABLE_QUERIES);

			Map<String, String> modifiedQueries = modifyQueries(queries);

			execution.setVariable(ConstantsFeasibility.VARIABLE_QUERIES, modifiedQueries);
		}
	}

	private Map<String, String> modifyQueries(Map<String, String> queries)
	{
		Map<String, String> modifiedQueries = new HashMap<>();

		for (Entry<String, String> entry : queries.entrySet())
			modifiedQueries.put(entry.getKey(), replaceSelectCountWithSelectMpiId(entry.getValue()));

		return modifiedQueries;
	}

	protected String replaceSelectCountWithSelectMpiId(String value)
	{
		// TODO Implement correct replacement for default id query
		return value.replace("select count(e)", "select e"  + ehrIdColumnPath + " as EHRID");
	}
}
