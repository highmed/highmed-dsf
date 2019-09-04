package org.highmed.dsf.bpe.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.MultiInstanceResult;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

@SuppressWarnings("unchecked")
public class ExecuteFeasibilityQueries extends AbstractServiceDelegate implements InitializingBean
{

	private static final Logger logger = LoggerFactory.getLogger(ExecuteFeasibilityQueries.class);

	private final OrganizationProvider organizationProvider;

	public ExecuteFeasibilityQueries(OrganizationProvider organizationProvider, WebserviceClient webserviceClient,
			TaskHelper taskHelper)
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
		Map<String, String> results = new HashMap<>();
		List<Group> cohortDefinitions = (List<Group>) execution.getVariable(Constants.VARIABLE_COHORTS);

		cohortDefinitions.forEach(group -> executeQuery(group,results));

		Identifier identifier = organizationProvider.getLocalIdentifier();
		MultiInstanceResult multiInstanceResult = new MultiInstanceResult(identifier.getSystem() + "|" + identifier.getValue(), results);

		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULT, multiInstanceResult);
	}

	private void executeQuery(Group group, Map<String, String> results) {
		String aqlQuery = getAqlQuery(group);
		logger.info("Executing aql-query '{}'", aqlQuery);

		// TODO: implement openEHR client
		// TODO: execute query

		IdType groupId = new IdType(group.getId());
		String groupIdString = groupId.getResourceType() + "/" + groupId.getIdPart();

		// TODO: is mock result, remove
		results.put(groupIdString, "10");
	}

	private String getAqlQuery(Group group)
	{
		// TODO: fix TEXT_CQL to support AQL
		List<Extension> queries = group.getExtension().stream()
				.filter(extension -> extension.getUrl().equals(Constants.EXTENSION_QUERY_URI))
				.filter(extension -> ((Expression) extension.getValue()).getLanguage() == Expression.ExpressionLanguage.TEXT_CQL)
				.collect(Collectors.toList());

		if(queries.size() != 1) {
			logger.error("Number of aql queries is not =1, got {}", queries.size());
			throw new IllegalArgumentException("Number of aql queries is not =1, got " + queries.size());
		}

		return ((Expression) queries.get(0).getValue()).getExpression();
	}
}
