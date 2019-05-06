package org.highmed.bpe.spring.config;

import org.highmed.bpe.message.SendPing;
import org.highmed.bpe.message.SendPong;
import org.highmed.bpe.plugin.PingPlugin;
import org.highmed.bpe.service.LogPing;
import org.highmed.bpe.service.LogPong;
import org.highmed.bpe.service.SelectPongTarget;
import org.highmed.bpe.service.SelectPingTargets;
import org.highmed.fhir.client.WebserviceClientProvider;
import org.highmed.fhir.organization.OrganizationProvider;
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

	@Bean
	public PingPlugin pingPlugin()
	{
		return new PingPlugin();
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
		return new SelectPongTarget(organizationProvider);
	}
}
