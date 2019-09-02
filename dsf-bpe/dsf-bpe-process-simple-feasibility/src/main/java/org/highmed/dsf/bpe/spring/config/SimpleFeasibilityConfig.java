package org.highmed.dsf.bpe.spring.config;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.highmed.dsf.bpe.message.SendSimpleCohortSizeRequest;
import org.highmed.dsf.bpe.message.SendSimpleCohortSizeResults;
import org.highmed.dsf.bpe.plugin.SimpleFeasibilityPlugin;
import org.highmed.dsf.bpe.service.CalculateMultiMedicSimpleCohortSizeResults;
import org.highmed.dsf.bpe.service.CheckMultiMedicSimpleCohortSizeResults;
import org.highmed.dsf.bpe.service.CheckResearchStudySimpleCohortSize;
import org.highmed.dsf.bpe.service.CheckSingleMedicSimpleCohortSizeResults;
import org.highmed.dsf.bpe.service.DownloadResearchStudy;
import org.highmed.dsf.bpe.service.ExecuteSimpleCohortSizeQueries;
import org.highmed.dsf.bpe.service.SelectRequestMedics;
import org.highmed.dsf.bpe.service.SelectResponseMedics;
import org.highmed.dsf.bpe.service.StoreSimpleCohortSizeResults;
import org.highmed.dsf.fhir.client.WebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleFeasibilityConfig
{
	@Autowired
	private WebserviceClientProvider clientProvider;

	@Autowired
	private OrganizationProvider organizationProvider;

	@Autowired
	private TaskHelper taskHelper;

	@Bean
	public ProcessEnginePlugin feasibilityPlugin()
	{
		return new SimpleFeasibilityPlugin();
	}

	@Bean
	public DownloadResearchStudy downloadResearchStudy()
	{
		return new DownloadResearchStudy(organizationProvider, clientProvider, taskHelper);
	}

	@Bean
	public CheckResearchStudySimpleCohortSize checkResearchStudySimpleCohortSize()
	{
		return new CheckResearchStudySimpleCohortSize(clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public SelectRequestMedics selectRequestMedics()
	{
		return new SelectRequestMedics(organizationProvider, clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public ExecuteSimpleCohortSizeQueries executeSimpleCohortSizeQuery()
	{
		return new ExecuteSimpleCohortSizeQueries(organizationProvider, clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public CheckSingleMedicSimpleCohortSizeResults checkSimpleCohortSizeQueryResult()
	{
		return new CheckSingleMedicSimpleCohortSizeResults(clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public SelectResponseMedics selectResponseMedics()
	{
		return new SelectResponseMedics(organizationProvider, taskHelper, clientProvider.getLocalWebserviceClient());
	}

	@Bean
	public StoreSimpleCohortSizeResults storeSimpleCohortSizeResult()
	{
		return new StoreSimpleCohortSizeResults(organizationProvider, clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public CalculateMultiMedicSimpleCohortSizeResults calculateMultiMedicSimpleCohortSize()
	{
		return new CalculateMultiMedicSimpleCohortSizeResults(clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public CheckMultiMedicSimpleCohortSizeResults checkMultiMedicSimpleCohortSizeResult()
	{
		return new CheckMultiMedicSimpleCohortSizeResults(clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public SendSimpleCohortSizeRequest sendSimpleCohortSizeRequest()
	{
		return new SendSimpleCohortSizeRequest(organizationProvider, clientProvider, taskHelper);
	}

	@Bean
	public SendSimpleCohortSizeResults sendSimpleCohortSizeResultToMedic()
	{
		return new SendSimpleCohortSizeResults(organizationProvider, clientProvider, taskHelper);
	}
}
