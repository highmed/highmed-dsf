package org.highmed.dsf.fhir.spring.config;

import java.util.List;

import org.highmed.dsf.fhir.authentication.AuthenticationFilterConfig;
import org.highmed.dsf.fhir.authentication.AuthenticationFilterConfigImpl;
import org.highmed.dsf.fhir.authentication.NeedsAuthentication;
import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.OrganizationProviderWithDbBackend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationConfig
{
	@Autowired
	private List<NeedsAuthentication> needsAuthentication;

	@Autowired
	private DaoConfig daoConfig;

	@Autowired
	private HelperConfig helperConfig;

	@Value("#{'${org.highmed.dsf.fhir.local-user.thumbprints}'.split(',')}")
	private List<String> localUserThumbprints;

	@Bean
	public AuthenticationFilterConfig authenticationFilterConfig()
	{
		return AuthenticationFilterConfigImpl.createConfigForPathsRequiringAuthentication("", needsAuthentication);
	}

	@Bean
	public OrganizationProvider organizationProvider()
	{
		return new OrganizationProviderWithDbBackend(daoConfig.organizationDao(), helperConfig.exceptionHandler(),
				localUserThumbprints);
	}
}
