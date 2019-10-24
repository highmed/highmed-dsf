package org.highmed.dsf.fhir.spring.config;

import java.util.List;

import org.highmed.dsf.fhir.cors.CorsFilterConfig;
import org.highmed.dsf.fhir.cors.CorsFilterConfigImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CorsConfig
{
	@Value("#{'${org.highmed.dsf.fhir.cors.origins}'.split(',')}")
	private List<String> allowedOrigins;

	@Bean
	public CorsFilterConfig corsFilterConfig()
	{
		return CorsFilterConfigImpl.createConfigForAllowedOrigins(allowedOrigins);
	}
}
