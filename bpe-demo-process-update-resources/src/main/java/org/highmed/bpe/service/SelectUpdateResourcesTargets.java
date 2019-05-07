package org.highmed.bpe.service;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.fhir.organization.OrganizationProvider;
import org.springframework.beans.factory.InitializingBean;

public class SelectUpdateResourcesTargets implements JavaDelegate, InitializingBean
{
	private final OrganizationProvider organizationProvider;

	public SelectUpdateResourcesTargets(OrganizationProvider organizationProvider)
	{
		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception
	{
		// TODO Auto-generated method stub

	}
}
