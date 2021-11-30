package org.highmed.dsf.fhir.webservice.base;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserProvider;

public class AbstractBasicService implements BasicService
{
	protected UserProvider userProvider;

	@Override
	public final void setUserProvider(UserProvider userProvider)
	{
		this.userProvider = userProvider;
	}

	protected final User getCurrentUser()
	{
		return userProvider.getCurrentUser();
	}
}
