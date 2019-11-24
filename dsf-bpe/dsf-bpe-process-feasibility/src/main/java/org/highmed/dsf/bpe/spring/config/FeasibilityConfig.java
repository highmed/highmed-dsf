package org.highmed.dsf.bpe.spring.config;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.highmed.dsf.bpe.message.SendFeasibilityRequest;
import org.highmed.dsf.bpe.message.SendFeasibilityResults;
import org.highmed.dsf.bpe.plugin.FeasibilityPlugin;
import org.highmed.dsf.bpe.service.CalculateMultiMedicFeasibilityResults;
import org.highmed.dsf.bpe.service.CheckFeasibilityQueries;
import org.highmed.dsf.bpe.service.CheckFeasibilityResources;
import org.highmed.dsf.bpe.service.CheckMultiMedicFeasibilityResults;
import org.highmed.dsf.bpe.service.CheckSingleMedicFeasibilityResults;
import org.highmed.dsf.bpe.service.DownloadFeasibilityResources;
import org.highmed.dsf.bpe.service.ExecuteFeasibilityQueries;
import org.highmed.dsf.bpe.service.SelectRequestMedics;
import org.highmed.dsf.bpe.service.SelectResponseMedics;
import org.highmed.dsf.bpe.service.StoreFeasibilityResults;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.group.GroupHelper;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.openehr.client.OpenEhrWebserviceClientProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class FeasibilityConfig
{
	@Autowired
	private FhirWebserviceClientProvider fhirClientProvider;

	@Autowired
	private OpenEhrWebserviceClientProvider openehrClientProvider;

	@Autowired
	private OrganizationProvider organizationProvider;

	@Autowired
	private TaskHelper taskHelper;

	@Autowired
	private GroupHelper groupHelper;

	@Autowired
	private FhirContext fhirContext;

	@Bean
	public ProcessEnginePlugin feasibilityPlugin()
	{
		return new FeasibilityPlugin();
	}

	@Bean
	public DownloadFeasibilityResources downloadFeasibilityResources()
	{
		return new DownloadFeasibilityResources(organizationProvider, fhirClientProvider, taskHelper);
	}

	@Bean
	public CheckFeasibilityResources checkFeasibilityResources()
	{
		return new CheckFeasibilityResources(fhirClientProvider, taskHelper);
	}

	@Bean
	public SelectRequestMedics selectRequestMedics()
	{
		return new SelectRequestMedics(organizationProvider, fhirClientProvider, taskHelper);
	}

	@Bean
	public CheckFeasibilityQueries checkFeasibilityQueries()
	{
		return new CheckFeasibilityQueries(fhirClientProvider, taskHelper, groupHelper);
	}

	@Bean
	public ExecuteFeasibilityQueries executeFeasibilityQueries()
	{
		return new ExecuteFeasibilityQueries(fhirClientProvider, openehrClientProvider.getWebserviceClient(),
				taskHelper);
	}

	@Bean
	public CheckSingleMedicFeasibilityResults checkSingleMedicFeasibilityResults()
	{
		return new CheckSingleMedicFeasibilityResults(fhirClientProvider, taskHelper);
	}

	@Bean
	public SelectResponseMedics selectResponseMedics()
	{
		return new SelectResponseMedics(fhirClientProvider, taskHelper, organizationProvider);
	}

	@Bean
	public StoreFeasibilityResults storeFeasibilityResults()
	{
		return new StoreFeasibilityResults(fhirClientProvider, taskHelper, organizationProvider);
	}

	@Bean
	public CalculateMultiMedicFeasibilityResults calculateMultiMedicSimpleFeasibilityResults()
	{
		return new CalculateMultiMedicFeasibilityResults(fhirClientProvider, taskHelper);
	}

	@Bean
	public CheckMultiMedicFeasibilityResults checkMultiMedicFeasibilityResults()
	{
		return new CheckMultiMedicFeasibilityResults(fhirClientProvider, taskHelper);
	}

	@Bean
	public SendFeasibilityRequest sendFeasibilityRequest()
	{
		return new SendFeasibilityRequest(fhirClientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Bean
	public SendFeasibilityResults sendCohortSizeResultToMedic()
	{
		return new SendFeasibilityResults(fhirClientProvider, taskHelper, organizationProvider, fhirContext);
	}
}
