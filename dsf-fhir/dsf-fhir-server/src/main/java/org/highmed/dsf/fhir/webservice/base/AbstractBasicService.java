package org.highmed.dsf.fhir.webservice.base;

import java.util.Objects;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserProvider;
import org.springframework.beans.factory.InitializingBean;

public class AbstractBasicService implements BasicService, InitializingBean
{
	private final String path;

	protected UserProvider userProvider;

	public AbstractBasicService(String path)
	{
		this.path = path;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(path, "path");
	}

	@Override
	public final void setUserProvider(UserProvider userProvider)
	{
		this.userProvider = userProvider;
	}

	@Override
	public final String getPath()
	{
		return path;
	}

	protected final User getCurrentUser()
	{
		return userProvider.getCurrentUser();
	}
}
