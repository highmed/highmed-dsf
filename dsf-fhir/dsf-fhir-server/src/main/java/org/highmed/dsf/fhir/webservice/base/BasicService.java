package org.highmed.dsf.fhir.webservice.base;

import org.highmed.dsf.fhir.authentication.UserProvider;

public interface BasicService
{
	void setUserProvider(UserProvider provider);
}
