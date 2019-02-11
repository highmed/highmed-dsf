package org.highmed.fhir.spring.config;

import org.highmed.fhir.adapter.CapabilityStatementJsonFhirAdapter;
import org.highmed.fhir.adapter.CapabilityStatementXmlFhirAdapter;
import org.highmed.fhir.adapter.PatientJsonFhirAdapter;
import org.highmed.fhir.adapter.PatientXmlFhirAdapter;
import org.highmed.fhir.adapter.SubscriptionJsonFhirAdapter;
import org.highmed.fhir.adapter.SubscriptionXmlFhirAdapter;
import org.highmed.fhir.adapter.TaskJsonFhirAdapter;
import org.highmed.fhir.adapter.TaskXmlFhirAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class AdapterConfig
{
	@Autowired
	private FhirContext fhirContext;

	@Bean
	public PatientJsonFhirAdapter patientJsonAdapter()
	{
		return new PatientJsonFhirAdapter(fhirContext);
	}

	@Bean
	public PatientXmlFhirAdapter patientXmlAdapter()
	{
		return new PatientXmlFhirAdapter(fhirContext);
	}

	@Bean
	public TaskJsonFhirAdapter taskJsonAdapter()
	{
		return new TaskJsonFhirAdapter(fhirContext);
	}

	@Bean
	public TaskXmlFhirAdapter taskXmlAdapter()
	{
		return new TaskXmlFhirAdapter(fhirContext);
	}
	
	@Bean
	public SubscriptionJsonFhirAdapter subscriptionJsonAdapter()
	{
		return new SubscriptionJsonFhirAdapter(fhirContext);
	}
	
	@Bean
	public SubscriptionXmlFhirAdapter subscriptionXmlAdapter()
	{
		return new SubscriptionXmlFhirAdapter(fhirContext);
	}

	@Bean
	public CapabilityStatementJsonFhirAdapter capabilityStatementJsonAdapter()
	{
		return new CapabilityStatementJsonFhirAdapter(fhirContext);
	}

	@Bean
	public CapabilityStatementXmlFhirAdapter capabilityStatementXmlAdapter()
	{
		return new CapabilityStatementXmlFhirAdapter(fhirContext);
	}
}
