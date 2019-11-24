package org.highmed.dsf.fhir.spring.config;

import java.io.IOException;

import org.highmed.dsf.tools.build.BuildInfoReader;
import org.highmed.dsf.tools.build.BuildInfoReaderImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class BuildInfoReaderConfig
{
	@Bean
	public BuildInfoReader buildInfoReader()
	{
		return new BuildInfoReaderImpl();
	}

	@EventListener({ ContextRefreshedEvent.class })
	public void onContextRefreshedEvent(ContextRefreshedEvent event) throws IOException
	{
		buildInfoReader().logSystemDefaultTimezone();
		buildInfoReader().logBuildInfo();
	}
}
