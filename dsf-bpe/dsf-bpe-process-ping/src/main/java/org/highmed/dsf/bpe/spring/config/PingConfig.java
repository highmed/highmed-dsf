package org.highmed.dsf.bpe.spring.config;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.highmed.dsf.bpe.message.SendPing;
import org.highmed.dsf.bpe.message.SendPong;
import org.highmed.dsf.bpe.plugin.PingPlugin;
import org.highmed.dsf.bpe.service.LogPing;
import org.highmed.dsf.bpe.service.LogPong;
import org.highmed.dsf.bpe.service.SelectPingTargets;
import org.highmed.dsf.bpe.service.SelectPongTarget;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class PingConfig
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
	public ProcessEnginePlugin pingPlugin()
	{
		return new PingPlugin();
	}

	@Bean
	public SendPing sendPing()
	{
		return new SendPing(clientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Bean
	public SendPong sendPong()
	{
		return new SendPong(clientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Bean
	public LogPing logPing()
	{
		return new LogPing(clientProvider, taskHelper, organizationProvider);
	}

	@Bean
	public LogPong logPong()
	{
		return new LogPong(clientProvider, taskHelper, organizationProvider);
	}

	@Bean
	public SelectPingTargets selectPingTargets()
	{
		return new SelectPingTargets(clientProvider, taskHelper, organizationProvider);
	}

	@Bean
	public SelectPongTarget selectPongTarget()
	{
		return new SelectPongTarget(clientProvider, taskHelper, organizationProvider);
	}
}
