package org.highmed.dsf.fhir.webservice.impl;

import java.util.Objects;

import org.highmed.dsf.fhir.authentication.UserProvider;
import org.highmed.dsf.fhir.webservice.specification.BasicService;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractServiceImpl implements BasicService, InitializingBean
{
	private final String path;

	public AbstractServiceImpl(String path)
	{
		this.path = path;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(path, "path");
	}

	@Override
	public final String getPath()
	{
		return path;
	}

	@Override
	public final void setUserProvider(UserProvider provider)
	{
	}
}
