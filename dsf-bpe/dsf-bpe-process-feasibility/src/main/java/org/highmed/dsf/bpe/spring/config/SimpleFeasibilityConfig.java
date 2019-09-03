package org.highmed.dsf.bpe.spring.config;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.highmed.dsf.bpe.message.SendCohortSizeRequest;
import org.highmed.dsf.bpe.message.SendCohortSizeResults;
import org.highmed.dsf.bpe.plugin.SimpleFeasibilityPlugin;
import org.highmed.dsf.bpe.service.CalculateMultiMedicCohortSizeResults;
import org.highmed.dsf.bpe.service.CheckMultiMedicCohortSizeResults;
import org.highmed.dsf.bpe.service.CheckResearchStudyCohortSize;
import org.highmed.dsf.bpe.service.CheckSingleMedicCohortSizeResults;
import org.highmed.dsf.bpe.service.DownloadResearchStudy;
import org.highmed.dsf.bpe.service.ExecuteCohortSizeQueries;
import org.highmed.dsf.bpe.service.SelectRequestMedics;
import org.highmed.dsf.bpe.service.SelectResponseMedics;
import org.highmed.dsf.bpe.service.StoreCohortSizeResults;
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
	public CheckResearchStudyCohortSize checkResearchStudySimpleCohortSize()
	{
		return new CheckResearchStudyCohortSize(clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public SelectRequestMedics selectRequestMedics()
	{
		return new SelectRequestMedics(organizationProvider, clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public ExecuteCohortSizeQueries executeSimpleCohortSizeQuery()
	{
		return new ExecuteCohortSizeQueries(organizationProvider, clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public CheckSingleMedicCohortSizeResults checkSimpleCohortSizeQueryResult()
	{
		return new CheckSingleMedicCohortSizeResults(clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public SelectResponseMedics selectResponseMedics()
	{
		return new SelectResponseMedics(organizationProvider, taskHelper, clientProvider.getLocalWebserviceClient());
	}

	@Bean
	public StoreCohortSizeResults storeSimpleCohortSizeResult()
	{
		return new StoreCohortSizeResults(organizationProvider, clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public CalculateMultiMedicCohortSizeResults calculateMultiMedicSimpleCohortSize()
	{
		return new CalculateMultiMedicCohortSizeResults(clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public CheckMultiMedicCohortSizeResults checkMultiMedicSimpleCohortSizeResult()
	{
		return new CheckMultiMedicCohortSizeResults(clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public SendCohortSizeRequest sendSimpleCohortSizeRequest()
	{
		return new SendCohortSizeRequest(organizationProvider, clientProvider, taskHelper);
	}

	@Bean
	public SendCohortSizeResults sendSimpleCohortSizeResultToMedic()
	{
		return new SendCohortSizeResults(organizationProvider, clientProvider, taskHelper);
	}
}
