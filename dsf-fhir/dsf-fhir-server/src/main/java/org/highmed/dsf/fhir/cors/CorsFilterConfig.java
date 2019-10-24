package org.highmed.dsf.fhir.cors;

public interface CorsFilterConfig
{
	boolean originAllowed(String origin);
}
