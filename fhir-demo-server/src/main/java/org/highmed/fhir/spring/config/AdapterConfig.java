package org.highmed.fhir.spring.config;

import org.highmed.fhir.adapter.CapabilityStatementJsonFhirAdapter;
import org.highmed.fhir.adapter.CapabilityStatementXmlFhirAdapter;
import org.highmed.fhir.adapter.PatientJsonFhirAdapter;
import org.highmed.fhir.adapter.PatientXmlFhirAdapter;
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
