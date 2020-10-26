package org.highmed.dsf.bpe.spring.config;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.highmed.dsf.bpe.plugin.UpdateAllowlistPlugin;
import org.highmed.dsf.bpe.service.DownloadAllowlist;
import org.highmed.dsf.bpe.service.UpdateAllowlist;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class UpdateAllowlistConfig
{
	@Autowired
	private FhirWebserviceClientProvider clientProvider;

	@Autowired
	private OrganizationProvider organizationProvider;

	@Autowired
	private TaskHelper taskHelper;

	@Autowired
	private FhirContext fhirContext;

	@Bean
	public ProcessEnginePlugin updateAllowlistPlugin()
	{
		return new UpdateAllowlistPlugin();
	}

	@Bean
	public UpdateAllowlist updateAllowlist()
	{
		return new UpdateAllowlist(organizationProvider, clientProvider, taskHelper);
	}

	@Bean
	public DownloadAllowlist downloadAllowlist()
	{
		return new DownloadAllowlist(clientProvider, taskHelper, fhirContext);
	}
}
