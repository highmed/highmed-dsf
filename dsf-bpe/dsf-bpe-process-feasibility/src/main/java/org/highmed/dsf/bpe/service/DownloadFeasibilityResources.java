package org.highmed.dsf.bpe.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.ConstantsFeasibility;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.BloomFilterConfig;
import org.highmed.dsf.fhir.variables.BloomFilterConfigValues;
import org.highmed.dsf.fhir.variables.FhirResourceValues;
import org.highmed.dsf.fhir.variables.FhirResourcesListValues;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class DownloadFeasibilityResources extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadFeasibilityResources.class);

	private final OrganizationProvider organizationProvider;

	public DownloadFeasibilityResources(OrganizationProvider organizationProvider,
			FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
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

		IdType researchStudyId = getResearchStudyId(task);
		FhirWebserviceClient client = getWebserviceClient(researchStudyId);
		Bundle bundle = getResearchStudyAndCohortDefinitions(researchStudyId, client);

		ResearchStudy researchStudy = (ResearchStudy) bundle.getEntryFirstRep().getResource();
		execution.setVariable(ConstantsFeasibility.VARIABLE_RESEARCH_STUDY, FhirResourceValues.create(researchStudy));

		List<Group> cohortDefinitions = getCohortDefinitions(bundle, client.getBaseUrl());
		execution.setVariable(ConstantsFeasibility.VARIABLE_COHORTS, FhirResourcesListValues.create(cohortDefinitions));

		String ttpIdentifier = getTtpIdentifier(researchStudy, client);
		execution.setVariable(ConstantsBase.VARIABLE_TTP_IDENTIFIER, ttpIdentifier);

		boolean needsConsentCheck = getNeedsConsentCheck(task);
		execution.setVariable(ConstantsFeasibility.VARIABLE_NEEDS_CONSENT_CHECK, needsConsentCheck);

		boolean needsRecordLinkage = getNeedsRecordLinkageCheck(task);
		execution.setVariable(ConstantsFeasibility.VARIABLE_NEEDS_RECORD_LINKAGE, needsRecordLinkage);

		if (needsRecordLinkage)
		{
			BloomFilterConfig bloomFilterConfig = getBloomFilterConfig(task);
			execution.setVariable(ConstantsFeasibility.VARIABLE_BLOOM_FILTER_CONFIG,
					BloomFilterConfigValues.create(bloomFilterConfig));
		}
	}

	private IdType getResearchStudyId(Task task)
	{
		Reference researchStudyReference = getTaskHelper()
				.getInputParameterReferenceValues(task, ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
						ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE).findFirst()
				.get();

		return new IdType(researchStudyReference.getReference());
	}

	private FhirWebserviceClient getWebserviceClient(IdType researchStudyId)
	{
		if (researchStudyId.getBaseUrl() == null || researchStudyId.getBaseUrl()
				.equals(getFhirWebserviceClientProvider().getLocalBaseUrl()))
		{
			return getFhirWebserviceClientProvider().getLocalWebserviceClient();
		}
		else
		{
			return getFhirWebserviceClientProvider().getRemoteWebserviceClient(researchStudyId.getBaseUrl());
		}
	}

	private Bundle getResearchStudyAndCohortDefinitions(IdType researchStudyId, FhirWebserviceClient client)
	{
		try
		{
			Bundle bundle = client.searchWithStrictHandling(ResearchStudy.class,
					Map.of("_id", Collections.singletonList(researchStudyId.getIdPart()), "_include",
							Collections.singletonList("ResearchStudy:enrollment")));

			if (bundle.getEntry().size() < 2)
			{
				throw new RuntimeException("Returned search-set contained less then two entries");
			}
			else if (!bundle.getEntryFirstRep().hasResource() || !(bundle.getEntryFirstRep()
					.getResource() instanceof ResearchStudy))
			{
				throw new RuntimeException("Returned search-set did not contain ResearchStudy at index == 0");
			}
			else if (bundle.getEntry().stream().skip(1).map(c -> c.hasResource() && c.getResource() instanceof Group)
					.filter(b -> !b).findAny().isPresent())
			{
				throw new RuntimeException("Returned search-set contained unexpected resource at index >= 1");
			}

			return bundle;
		}
		catch (Exception e)
		{
			logger.warn("Error while reading ResearchStudy  with id {} including Groups from {}: {}",
					researchStudyId.getIdPart(), client.getBaseUrl(), e.getMessage());
			throw e;
		}
	}

	private List<Group> getCohortDefinitions(Bundle bundle, String baseUrl)
	{
		return bundle.getEntry().stream().skip(1).map(e -> {
			Group group = (Group) e.getResource();
			IdType oldId = group.getIdElement();
			group.setIdElement(
					new IdType(baseUrl, oldId.getResourceType(), oldId.getIdPart(), oldId.getVersionIdPart()));
			return group;
		}).collect(Collectors.toList());
	}

	private String getTtpIdentifier(ResearchStudy researchStudy, FhirWebserviceClient client)
	{
		Extension ext = researchStudy
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/participating-ttp");
		Reference ref = (Reference) ext.getValue();
		return ref.getIdentifier().getValue();
	}

	private boolean getNeedsConsentCheck(Task task)
	{
		return getTaskHelper()
				.getFirstInputParameterBooleanValue(task, ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
						ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK).orElseThrow(
						() -> new IllegalArgumentException(
								"NeedsConsentCheck boolean is not set in task with id='" + task.getId()
										+ "', this error should " + "have been caught by resource validation"));
	}

	private boolean getNeedsRecordLinkageCheck(Task task)
	{
		return getTaskHelper()
				.getFirstInputParameterBooleanValue(task, ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
						ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE).orElseThrow(
						() -> new IllegalArgumentException(
								"NeedsRecordLinkage boolean is not set in task with id='" + task.getId()
										+ "', this error should " + "have been caught by resource validation"));
	}

	private BloomFilterConfig getBloomFilterConfig(Task task)
	{
		return BloomFilterConfig.fromBytes(getTaskHelper()
				.getFirstInputParameterByteValue(task, ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
						ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_BLOOM_FILTER_CONFIG).orElseThrow(
						() -> new IllegalArgumentException(
								"BloomFilterConfig byte[] is not set in task with id='" + task.getId()
										+ "', this error should " + "have been caught by resource validation")));
	}
}
