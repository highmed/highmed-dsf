package org.highmed.fhir.spring.config;

import org.highmed.fhir.werbservice.ConformanceProvider;
import org.highmed.fhir.werbservice.TaskProvider;
import org.highmed.fhir.werbservice.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebserviceConfig
{
	@Value("org.highmed.fhir.serverBase")
	private String serverBase;

	@Autowired
	public FhirConfig fhirConfig;

	@Bean
	public TestService testService()
	{
		return new TestService();
	}

	@Bean
	public ConformanceProvider conformanceProvider()
	{
		return new ConformanceProvider(fhirConfig.fhirContext(), serverBase, taskProvider());
	}

	@Bean
	public TaskProvider taskProvider()
	{
		return new TaskProvider(fhirConfig.fhirContext(), serverBase);
	}
}
