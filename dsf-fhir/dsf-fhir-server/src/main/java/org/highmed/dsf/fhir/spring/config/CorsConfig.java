package org.highmed.dsf.fhir.spring.config;

import org.highmed.dsf.fhir.cors.CorsFilterConfig;
import org.highmed.dsf.fhir.cors.CorsFilterConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CorsConfig
{
	@Autowired
	private PropertiesConfig propertiesConfig;

	@Bean
	public CorsFilterConfig corsFilterConfig()
	{
		return CorsFilterConfigImpl.createConfigForAllowedOrigins(propertiesConfig.getAllowedOrigins());
	}
}
