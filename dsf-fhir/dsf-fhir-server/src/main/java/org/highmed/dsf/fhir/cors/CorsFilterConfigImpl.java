package org.highmed.dsf.fhir.cors;

import java.util.List;
import java.util.Objects;

public class CorsFilterConfigImpl implements CorsFilterConfig
{
	public static CorsFilterConfig createConfigForAllowedOrigins(List<String> allowedOrigins)
	{
		Objects.requireNonNull(allowedOrigins);
		return new CorsFilterConfigImpl(allowedOrigins);
	}

	private final List<String> allowedOrigins;

	private CorsFilterConfigImpl(List<String> allowedOrigins)
	{
		this.allowedOrigins = allowedOrigins;
	}

	@Override
	public boolean originAllowed(String origin)
	{
		return allowedOrigins.contains(origin);
	}
}
