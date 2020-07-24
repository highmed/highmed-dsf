package org.highmed.dsf.bpe.service;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.KeyGenerator;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.BloomFilterConfig;
import org.highmed.dsf.bpe.variables.BloomFilterConfigValues;
import org.highmed.dsf.bpe.variables.ConstantsFeasibility;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.MultiInstanceTarget;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetValues;
import org.highmed.dsf.fhir.variables.MultiInstanceTargets;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetsValues;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;

public class SelectRequestTargets extends AbstractServiceDelegate
{
	private static final Random random = new Random();

	private final OrganizationProvider organizationProvider;
	private final KeyGenerator hmacSha2Generator;
	private final KeyGenerator hmacSha3Generator;

	public SelectRequestTargets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider, BouncyCastleProvider bouncyCastleProvider)
	{
		super(clientProvider, taskHelper);

		this.organizationProvider = organizationProvider;

		try
		{
			Objects.requireNonNull(bouncyCastleProvider, "bouncyCastleProvider");

			hmacSha2Generator = KeyGenerator.getInstance("HmacSHA256", bouncyCastleProvider);
			hmacSha3Generator = KeyGenerator.getInstance("HmacSHA3-256", bouncyCastleProvider);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
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
		ResearchStudy researchStudy = (ResearchStudy) execution
				.getVariable(ConstantsFeasibility.VARIABLE_RESEARCH_STUDY);

		execution.setVariable(ConstantsBase.VARIABLE_MULTI_INSTANCE_TARGETS,
				MultiInstanceTargetsValues.create(getMedicTargets(researchStudy)));

		execution.setVariable(ConstantsBase.VARIABLE_MULTI_INSTANCE_TARGET,
				MultiInstanceTargetValues.create(getTtpTarget(researchStudy)));

		Boolean needsRecordLinkage = (Boolean) execution
				.getVariable(ConstantsFeasibility.VARIABLE_NEEDS_RECORD_LINKAGE);
		if (Boolean.TRUE.equals(needsRecordLinkage))
		{
			execution.setVariable(ConstantsFeasibility.VARIABLE_BLOOM_FILTER_CONFIG,
					BloomFilterConfigValues.create(createBloomFilterConfig()));
		}
	}

	private BloomFilterConfig createBloomFilterConfig()
	{
		return new BloomFilterConfig(random.nextLong(), hmacSha2Generator.generateKey(),
				hmacSha3Generator.generateKey());
	}

	private MultiInstanceTargets getMedicTargets(ResearchStudy researchStudy)
	{
		List<MultiInstanceTarget> targets = researchStudy
				.getExtensionsByUrl(ConstantsFeasibility.EXTENSION_PARTICIPATING_MEDIC_URI).stream()
				.filter(e -> e.getValue() instanceof Reference).map(e -> (Reference) e.getValue())
				.map(r -> new MultiInstanceTarget(r.getIdentifier().getValue(), UUID.randomUUID().toString()))
				.collect(Collectors.toList());

		return new MultiInstanceTargets(targets);
	}

	private MultiInstanceTarget getTtpTarget(ResearchStudy researchStudy)
	{
		return researchStudy.getExtensionsByUrl(ConstantsFeasibility.EXTENSION_PARTICIPATING_TTP_URI).stream()
				.filter(e -> e.getValue() instanceof Reference).map(e -> (Reference) e.getValue())
				.map(r -> new MultiInstanceTarget(r.getIdentifier().getValue(), UUID.randomUUID().toString()))
				.findFirst().get();
	}
}
