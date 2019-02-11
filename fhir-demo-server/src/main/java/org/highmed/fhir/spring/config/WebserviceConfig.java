package org.highmed.fhir.spring.config;

import org.highmed.fhir.webservice.ConformanceService;
import org.highmed.fhir.webservice.PatientService;
import org.highmed.fhir.webservice.SubscriptionService;
import org.highmed.fhir.webservice.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebserviceConfig
{
	@Value("${org.highmed.fhir.serverBase}")
	private String serverBase;

	@Autowired
	private DaoConfig daoConfig;

	@Bean
	public ConformanceService conformanceService()
	{
		return new ConformanceService(serverBase);
	}

	@Bean
	public PatientService patientService()
	{
		return new PatientService(serverBase, daoConfig.patientDao());
	}

	@Bean
	public SubscriptionService subscriptionService()
	{
		return new SubscriptionService(serverBase, daoConfig.subscriptionDao());
	}

	@Bean
	public TaskService taskService()
	{
		return new TaskService(serverBase, daoConfig.taskDao());
	}
}
