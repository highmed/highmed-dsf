package org.highmed.fhir.spring.config;

import org.highmed.fhir.adapter.PatientJsonAdapter;
import org.highmed.fhir.adapter.PatientXmlAdapter;
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
	public PatientJsonAdapter patientJsonAdapter()
	{
		return new PatientJsonAdapter(fhirContext);
	}

	@Bean
	public PatientXmlAdapter patientXmlAdapter()
	{
		return new PatientXmlAdapter(fhirContext);
	}
}
