package org.highmed.dsf.bpe.service;

import java.util.Objects;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class CheckRequest extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(CheckRequest.class);

	private final OrganizationProvider organizationProvider;

	public CheckRequest(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider)
	{
		super(clientProvider, taskHelper);

		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = (Task) execution.getVariable(ConstantsBase.VARIABLE_TASK);

		if (requesterIsNotOfTypeTtp(task.getRequester().getIdentifier()))
		{
			throw new RuntimeException(
					"Request check failed: process can only be started by requesting organization of type='"
							+ ConstantsBase.ORGANIZATION_TYPE_TTP + "'");
		}
	}

	private boolean requesterIsNotOfTypeTtp(Identifier requester)
	{
		Optional<Organization> organization = organizationProvider
				.getOrganization(requester.getSystem(), requester.getValue());

		return !organization.map(value -> value.getType().stream().anyMatch(type -> type.getCoding().stream().anyMatch(
				coding -> coding.getSystem().equals(ConstantsBase.ORGANIZATION_TYPE_SYSTEM) && coding.getCode()
						.equals(ConstantsBase.ORGANIZATION_TYPE_TTP)))).orElse(false);
	}
}
