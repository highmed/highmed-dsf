package org.highmed.fhir.authentication;

import javax.servlet.http.HttpServletRequest;

public interface AuthenticationFilterConfig
{
	boolean needsAuthentication(HttpServletRequest request);
}
