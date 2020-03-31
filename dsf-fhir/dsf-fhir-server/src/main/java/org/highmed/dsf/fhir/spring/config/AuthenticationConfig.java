package org.highmed.dsf.fhir.spring.config;

import java.util.List;

import org.highmed.dsf.fhir.authentication.AuthenticationFilterConfig;
import org.highmed.dsf.fhir.authentication.AuthenticationFilterConfigImpl;
import org.highmed.dsf.fhir.authentication.NeedsAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationConfig
{
	@Autowired
	private List<NeedsAuthentication> needsAuthentication;

	@Bean
	public AuthenticationFilterConfig authenticationFilterConfig()
	{
		return AuthenticationFilterConfigImpl.createConfigForPathsRequiringAuthentication("", needsAuthentication);
	}
}
