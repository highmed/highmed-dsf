package org.highmed.fhir.spring.config;

import org.highmed.fhir.adapter.JsonFhirAdapter;
import org.highmed.fhir.adapter.XmlFhirAdapter;
import org.hl7.fhir.r4.model.Patient;
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
	public JsonFhirAdapter<Patient> patientJsonAdapter()
	{
		return JsonFhirAdapter.create(fhirContext, Patient.class);
	}

	@Bean
	public XmlFhirAdapter<Patient> patientXmlAdapter()
	{
		return XmlFhirAdapter.create(fhirContext, Patient.class);
	}
}
