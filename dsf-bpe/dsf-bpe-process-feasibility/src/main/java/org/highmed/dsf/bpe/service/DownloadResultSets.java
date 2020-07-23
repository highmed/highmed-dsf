package org.highmed.dsf.bpe.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResult;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResults;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResultsValues;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.openehr.model.structure.ResultSet;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DownloadResultSets extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadResultSets.class);

	private final ObjectMapper openEhrObjectMapper;

	public DownloadResultSets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ObjectMapper openEhrObjectMapper)
	{
		super(clientProvider, taskHelper);

		this.openEhrObjectMapper = openEhrObjectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(openEhrObjectMapper, "openEhrObjectMapper");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		FeasibilityQueryResults results = (FeasibilityQueryResults) execution
				.getVariable(ConstantsBase.VARIABLE_QUERY_RESULTS);

		List<FeasibilityQueryResult> resultsWithResultSets = download(results);

		execution.setVariable(ConstantsBase.VARIABLE_QUERY_RESULTS,
				FeasibilityQueryResultsValues.create(new FeasibilityQueryResults(resultsWithResultSets)));
	}

	private List<FeasibilityQueryResult> download(FeasibilityQueryResults results)
	{
		return results.getResults().stream().map(r -> download(r)).collect(Collectors.toList());
	}

	private FeasibilityQueryResult download(FeasibilityQueryResult result)
	{
		IdType id = new IdType(result.getResultSetUrl());
		FhirWebserviceClient client = getFhirWebserviceClientProvider().getRemoteWebserviceClient(id.getBaseUrl());

		InputStream binary = readBinaryResource(client, id.getIdPart());
		ResultSet resultSet = deserializeResultSet(binary);

		return FeasibilityQueryResult.idResult(result.getOrganizationIdentifier(), result.getCohortId(), resultSet);
	}

	private InputStream readBinaryResource(FhirWebserviceClient client, String id)
	{
		try
		{
			logger.info("Reading binary from {} with id {}", client.getBaseUrl(), id);
			return client.readBinary(id, MediaType.valueOf(ConstantsBase.OPENEHR_MIMETYPE_JSON));
		}
		catch (Exception e)
		{
			logger.warn("Error while reading Binary resoruce: " + e.getMessage(), e);
			throw e;
		}
	}

	private ResultSet deserializeResultSet(InputStream content)
	{
		try (content)
		{
			return openEhrObjectMapper.readValue(content, ResultSet.class);
		}
		catch (IOException e)
		{
			logger.warn("Error while deserializing ResultSet: " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
}
