package org.highmed.dsf.bpe.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.ConstantsUpdateAllowlist;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class DownloadAllowlist extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadAllowlist.class);

	private final FhirContext context;

	public DownloadAllowlist(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper, FhirContext context)
	{
		super(clientProvider, taskHelper);
		this.context = context;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		Objects.requireNonNull(context, "fhirContext");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = (Task) execution.getVariable(ConstantsBase.VARIABLE_TASK);
		IdType bundleId = getBundleId(task);
		FhirWebserviceClient requesterClient = getFhirWebserviceClientProvider()
				.getRemoteWebserviceClient(bundleId.getBaseUrl());

		Bundle bundle;
		try
		{
			if (bundleId.hasVersionIdPart())
				bundle = requesterClient.read(Bundle.class, bundleId.getIdPart(), bundleId.getVersionIdPart());
			else
				bundle = requesterClient.read(Bundle.class, bundleId.getIdPart());
		}
		catch (WebApplicationException e)
		{
			logger.error("Error while reading Bundle with id {} from organization {}: {}", bundleId.getValue(),
					task.getRequester().getReference(), e.getMessage());
			throw new RuntimeException("Error while reading Bundle with id " + bundleId.getValue()
					+ " from organization " + task.getRequester().getReference() + ", " + e.getMessage(), e);
		}

		if (!EnumSet.of(BundleType.TRANSACTION, BundleType.BATCH).contains(bundle.getType()))
		{
			logger.error("Bundle type TRANSACTION or BATCH expected, but got {}", bundle.getType());
			throw new RuntimeException("Bundle type TRANSACTION or BATCH expected, but got " + bundle.getType());
		}

		try
		{
			logger.debug("Posting bundle to local endpoint: {}", context.newXmlParser().encodeResourceToString(bundle));
			getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().postBundle(bundle);
		}
		catch (Exception e)
		{
			logger.error("Error while executing Bundle with id {} from organization {} locally: {}",
					bundleId.getValue(), task.getRequester().getReference(), e.getMessage());
			throw new RuntimeException("Error while executing Bundle with id " + bundleId.getValue()
					+ " from organization " + task.getRequester().getReference() + " locally, " + e.getMessage(), e);
		}
	}

	private IdType getBundleId(Task task)
	{
		List<Reference> bundleReferences = getTaskHelper()
				.getInputParameterReferenceValues(task, ConstantsUpdateAllowlist.CODESYSTEM_HIGHMED_UPDATE_ALLOWLIST,
						ConstantsUpdateAllowlist.CODESYSTEM_HIGHMED_UPDATE_ALLOWLIST_VALUE_ALLOWLIST)
				.collect(Collectors.toList());

		if (bundleReferences.size() != 1)
		{
			logger.error("Task input parameter {} contains unexpected number of Bundle IDs, expected 1, got {}",
					ConstantsUpdateAllowlist.CODESYSTEM_HIGHMED_UPDATE_ALLOWLIST_VALUE_ALLOWLIST,
					bundleReferences.size());
			throw new RuntimeException("Task input parameter "
					+ ConstantsUpdateAllowlist.CODESYSTEM_HIGHMED_UPDATE_ALLOWLIST_VALUE_ALLOWLIST
					+ " contains unexpected number of Bundle IDs, expected 1, got " + bundleReferences.size());
		}
		else if (!bundleReferences.get(0).hasReference()
				|| !bundleReferences.get(0).getReference().contains("/Bundle/"))
		{
			logger.error("Task input parameter {} has no Bundle reference",
					ConstantsUpdateAllowlist.CODESYSTEM_HIGHMED_UPDATE_ALLOWLIST_VALUE_ALLOWLIST);
			throw new RuntimeException("Task input parameter "
					+ ConstantsUpdateAllowlist.CODESYSTEM_HIGHMED_UPDATE_ALLOWLIST_VALUE_ALLOWLIST
					+ " has no Bundle reference");
		}

		return new IdType(bundleReferences.get(0).getReference());
	}
}
