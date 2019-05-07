package org.highmed.bpe.service;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.fhir.client.WebserviceClientProvider;
import org.springframework.beans.factory.InitializingBean;

public class UpdateResources implements JavaDelegate, InitializingBean
{
	private final WebserviceClientProvider clientProvider;

	public UpdateResources(WebserviceClientProvider clientProvider)
	{
		this.clientProvider = clientProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(clientProvider, "clientProvider");
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception
	{
		// TODO Auto-generated method stub

	}
}
