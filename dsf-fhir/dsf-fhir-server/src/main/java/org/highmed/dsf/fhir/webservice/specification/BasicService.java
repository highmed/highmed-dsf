package org.highmed.dsf.fhir.webservice.specification;

import org.highmed.dsf.fhir.authentication.NeedsAuthentication;
import org.highmed.dsf.fhir.authentication.UserProvider;

public interface BasicService extends NeedsAuthentication
{
	void setUserProvider(UserProvider provider);
}
