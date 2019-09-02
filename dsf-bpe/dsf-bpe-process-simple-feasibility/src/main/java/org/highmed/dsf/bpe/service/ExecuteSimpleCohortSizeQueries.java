package org.highmed.dsf.bpe.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.bpe.variables.MultiInstanceResult;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

@SuppressWarnings("unchecked")
public class ExecuteSimpleCohortSizeQueries extends AbstractServiceDelegate implements InitializingBean
{

	private static final Logger logger = LoggerFactory.getLogger(ExecuteSimpleCohortSizeQueries.class);

	private final OrganizationProvider organizationProvider;

	public ExecuteSimpleCohortSizeQueries(OrganizationProvider organizationProvider, WebserviceClient webserviceClient, TaskHelper taskHelper)
	{
		super(webserviceClient, taskHelper);
		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		// TODO: is mock result, remove
		// TODO: implement openehr client and execute query

		Map<String, String> results = new HashMap<>();
		List<Group> cohortDefinitions = (List<Group>) execution.getVariable(Constants.VARIABLE_COHORTS);

		cohortDefinitions.forEach(group -> {
			IdType groupId = new IdType(group.getId());
			String groupIdString = groupId.getResourceType() + "/" + groupId.getIdPart();
			results.put(groupIdString, "10");
		});

		Identifier identifier = organizationProvider.getLocalIdentifier();

		MultiInstanceResult multiInstanceResult = new MultiInstanceResult(identifier.getSystem() + "|" + identifier.getValue(), results);
		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULT, multiInstanceResult);
	}
}
