package org.highmed.dsf.bpe.spring.config;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.highmed.dsf.bpe.message.SendPing;
import org.highmed.dsf.bpe.message.SendPong;
import org.highmed.dsf.bpe.plugin.PingPlugin;
import org.highmed.dsf.bpe.service.LogPing;
import org.highmed.dsf.bpe.service.LogPong;
import org.highmed.dsf.bpe.service.SelectPingTargets;
import org.highmed.dsf.bpe.service.SelectPongTarget;
import org.highmed.dsf.fhir.client.WebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PingConfig
{
	@Autowired
	private WebserviceClientProvider clientProvider;

	@Autowired
	private OrganizationProvider organizationProvider;

	@Autowired
	private TaskHelper taskHelper;

	@Autowired
	private BaseProcessConfig baseProcessConfig;

	@Bean
	public ProcessEnginePlugin pingPlugin()
	{
		return new PingPlugin(baseProcessConfig.defaultBpmnParseListener());
	}

	@Bean
	public SendPing sendPing()
	{
		return new SendPing(organizationProvider, clientProvider);
	}

	@Bean
	public SendPong sendPong()
	{
		return new SendPong(organizationProvider, clientProvider);
	}

	@Bean
	public LogPing logPing()
	{
		return new LogPing();
	}

	@Bean
	public LogPong logPong()
	{
		return new LogPong();
	}

	@Bean
	public SelectPingTargets selectPingTargets()
	{
		return new SelectPingTargets(organizationProvider);
	}

	@Bean
	public SelectPongTarget selectPongTarget()
	{
		return new SelectPongTarget(organizationProvider, taskHelper);
	}
}
