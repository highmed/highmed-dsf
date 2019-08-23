package org.highmed.dsf.bpe.spring.config;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.highmed.dsf.bpe.message.SendSimpleCohortSizeRequest;
import org.highmed.dsf.bpe.message.SendSimpleCohortSizeResult;
import org.highmed.dsf.bpe.plugin.SimpleFeasibilityPlugin;
import org.highmed.dsf.bpe.service.*;
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

	@Autowired
	private BaseProcessConfig baseProcessConfig;


	// Plugin

	@Bean
	public ProcessEnginePlugin feasibilityPlugin()
	{
		return new SimpleFeasibilityPlugin(baseProcessConfig.defaultBpmnParseListener());
	}


	// Tasks

	@Bean
	public DownloadResearchStudy downloadResearchStudy()
	{
		return new DownloadResearchStudy(clientProvider, taskHelper);
	}

	@Bean
	public CheckResearchStudySimpleCohortSize checkResearchStudySimpleCohortSize()
	{
		return new CheckResearchStudySimpleCohortSize( clientProvider.getLocalWebserviceClient());
	}

	@Bean
	public SelectRequestMedics selectRequestMedics()
	{
		return new SelectRequestMedics(organizationProvider, clientProvider.getLocalWebserviceClient());
	}

	@Bean
	public PrepareSimpleCohortSizeQuery prepareSimpleCohortSizeQuery()
	{
		return new PrepareSimpleCohortSizeQuery( clientProvider.getLocalWebserviceClient());
	}

	@Bean
	public ExecuteSimpleCohortSizeQuery executeSimpleCohortSizeQuery()
	{
		return new ExecuteSimpleCohortSizeQuery( clientProvider.getLocalWebserviceClient());
	}

	@Bean
	public CheckSimpleCohortSizeQueryResult checkSimpleCohortSizeQueryResult()
	{
		return new CheckSimpleCohortSizeQueryResult( clientProvider.getLocalWebserviceClient());
	}

	@Bean
	public SelectResponseMedic selectResponseMedic()
	{
		return new SelectResponseMedic(organizationProvider, taskHelper,  clientProvider.getLocalWebserviceClient());
	}

	@Bean
	public StoreSimpleCohortSizeResult storeSimpleCohortSizeResult()
	{
		return new StoreSimpleCohortSizeResult( clientProvider.getLocalWebserviceClient());
	}

	@Bean
	public CalculateMultiMedicSimpleCohortSizeResult calculateMultiMedicSimpleCohortSize()
	{
		return new CalculateMultiMedicSimpleCohortSizeResult( clientProvider.getLocalWebserviceClient());
	}

	@Bean
	public CheckMultiMedicSimpleCohortSizeResult checkMultiMedicSimpleCohortSizeResult()
	{
		return new CheckMultiMedicSimpleCohortSizeResult( clientProvider.getLocalWebserviceClient());
	}

	// Messages

	@Bean
	public SendSimpleCohortSizeRequest sendSimpleCohortSizeRequest()
	{
		return new SendSimpleCohortSizeRequest(organizationProvider, clientProvider);
	}

	@Bean
	public SendSimpleCohortSizeResult sendSimpleCohortSizeResultToMedic()
	{
		return new SendSimpleCohortSizeResult(organizationProvider, clientProvider);
	}
}
