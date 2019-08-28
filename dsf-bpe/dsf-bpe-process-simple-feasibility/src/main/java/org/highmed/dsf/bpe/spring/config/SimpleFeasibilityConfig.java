package org.highmed.dsf.bpe.spring.config;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.highmed.dsf.bpe.message.SendSimpleCohortSizeRequest;
import org.highmed.dsf.bpe.message.SendSimpleCohortSizeResult;
import org.highmed.dsf.bpe.plugin.SimpleFeasibilityPlugin;
import org.highmed.dsf.bpe.service.CalculateMultiMedicSimpleCohortSizeResult;
import org.highmed.dsf.bpe.service.CheckMultiMedicSimpleCohortSizeResult;
import org.highmed.dsf.bpe.service.CheckResearchStudySimpleCohortSize;
import org.highmed.dsf.bpe.service.CheckSimpleCohortSizeQueryResult;
import org.highmed.dsf.bpe.service.DownloadResearchStudy;
import org.highmed.dsf.bpe.service.ExecuteSimpleCohortSizeQuery;
import org.highmed.dsf.bpe.service.PrepareSimpleCohortSizeQuery;
import org.highmed.dsf.bpe.service.SelectRequestMedics;
import org.highmed.dsf.bpe.service.SelectResponseMedic;
import org.highmed.dsf.bpe.service.StoreSimpleCohortSizeResult;
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
		return new DownloadResearchStudy(clientProvider, taskHelper);
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
	public PrepareSimpleCohortSizeQuery prepareSimpleCohortSizeQuery()
	{
		return new PrepareSimpleCohortSizeQuery(clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public ExecuteSimpleCohortSizeQuery executeSimpleCohortSizeQuery()
	{
		return new ExecuteSimpleCohortSizeQuery(clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public CheckSimpleCohortSizeQueryResult checkSimpleCohortSizeQueryResult()
	{
		return new CheckSimpleCohortSizeQueryResult(clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public SelectResponseMedic selectResponseMedic()
	{
		return new SelectResponseMedic(organizationProvider, taskHelper, clientProvider.getLocalWebserviceClient());
	}

	@Bean
	public StoreSimpleCohortSizeResult storeSimpleCohortSizeResult()
	{
		return new StoreSimpleCohortSizeResult(clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public CalculateMultiMedicSimpleCohortSizeResult calculateMultiMedicSimpleCohortSize()
	{
		return new CalculateMultiMedicSimpleCohortSizeResult(clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public CheckMultiMedicSimpleCohortSizeResult checkMultiMedicSimpleCohortSizeResult()
	{
		return new CheckMultiMedicSimpleCohortSizeResult(clientProvider.getLocalWebserviceClient(), taskHelper);
	}

	@Bean
	public SendSimpleCohortSizeRequest sendSimpleCohortSizeRequest()
	{
		return new SendSimpleCohortSizeRequest(organizationProvider, clientProvider, taskHelper);
	}

	@Bean
	public SendSimpleCohortSizeResult sendSimpleCohortSizeResultToMedic()
	{
		return new SendSimpleCohortSizeResult(organizationProvider, clientProvider, taskHelper);
	}
}
