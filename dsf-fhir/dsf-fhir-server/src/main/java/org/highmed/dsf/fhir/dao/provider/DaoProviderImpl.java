package org.highmed.dsf.fhir.dao.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.highmed.dsf.fhir.dao.BinaryDao;
import org.highmed.dsf.fhir.dao.BundleDao;
import org.highmed.dsf.fhir.dao.CodeSystemDao;
import org.highmed.dsf.fhir.dao.EndpointDao;
import org.highmed.dsf.fhir.dao.GroupDao;
import org.highmed.dsf.fhir.dao.HealthcareServiceDao;
import org.highmed.dsf.fhir.dao.LocationDao;
import org.highmed.dsf.fhir.dao.NamingSystemDao;
import org.highmed.dsf.fhir.dao.OrganizationDao;
import org.highmed.dsf.fhir.dao.PatientDao;
import org.highmed.dsf.fhir.dao.PractitionerDao;
import org.highmed.dsf.fhir.dao.PractitionerRoleDao;
import org.highmed.dsf.fhir.dao.ProvenanceDao;
import org.highmed.dsf.fhir.dao.ResearchStudyDao;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.dao.StructureDefinitionSnapshotDao;
import org.highmed.dsf.fhir.dao.SubscriptionDao;
import org.highmed.dsf.fhir.dao.TaskDao;
import org.highmed.dsf.fhir.dao.ValueSetDao;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class DaoProviderImpl implements DaoProvider, InitializingBean
{
	private final BinaryDao binaryDao;
	private final BundleDao bundleDao;
	private final CodeSystemDao codeSystemDao;
	private final EndpointDao endpointDao;
	private final GroupDao groupDao;
	private final HealthcareServiceDao healthcareServiceDao;
	private final LocationDao locationDao;
	private final NamingSystemDao namingSystemDao;
	private final OrganizationDao organizationDao;
	private final PatientDao patientDao;
	private final PractitionerDao practitionerDao;
	private final PractitionerRoleDao practitionerRoleDao;
	private final ProvenanceDao provenanceDao;
	private final ResearchStudyDao researchStudyDao;
	private final StructureDefinitionDao structureDefinitionDao;
	private final StructureDefinitionSnapshotDao structureDefinitionSnapshotDao;
	private final SubscriptionDao subscriptionDao;
	private final TaskDao taskDao;
	private final ValueSetDao valueSetDao;

	private final Map<Class<? extends Resource>, ResourceDao<?>> daosByResourecClass = new HashMap<>();
	private final Map<String, ResourceDao<?>> daosByResourceTypeName = new HashMap<>();

	public DaoProviderImpl(BinaryDao binaryDao, BundleDao bundleDao, CodeSystemDao codeSystemDao,
			EndpointDao endpointDao, GroupDao groupDao, HealthcareServiceDao healthcareServiceDao,
			LocationDao locationDao, NamingSystemDao namingSystemDao, OrganizationDao organizationDao,
			PatientDao patientDao, PractitionerDao practitionerDao, PractitionerRoleDao practitionerRoleDao,
			ProvenanceDao provenanceDao, ResearchStudyDao researchStudyDao,
			StructureDefinitionDao structureDefinitionDao,
			StructureDefinitionSnapshotDao structureDefinitionSnapshotDao, SubscriptionDao subscriptionDao,
			TaskDao taskDao, ValueSetDao valueSetDao)
	{
		this.binaryDao = binaryDao;
		this.bundleDao = bundleDao;
		this.codeSystemDao = codeSystemDao;
		this.endpointDao = endpointDao;
		this.groupDao = groupDao;
		this.healthcareServiceDao = healthcareServiceDao;
		this.locationDao = locationDao;
		this.namingSystemDao = namingSystemDao;
		this.organizationDao = organizationDao;
		this.patientDao = patientDao;
		this.practitionerDao = practitionerDao;
		this.practitionerRoleDao = practitionerRoleDao;
		this.provenanceDao = provenanceDao;
		this.researchStudyDao = researchStudyDao;
		this.structureDefinitionDao = structureDefinitionDao;
		this.structureDefinitionSnapshotDao = structureDefinitionSnapshotDao;
		this.subscriptionDao = subscriptionDao;
		this.taskDao = taskDao;
		this.valueSetDao = valueSetDao;

		daosByResourecClass.put(Binary.class, binaryDao);
		daosByResourecClass.put(Bundle.class, bundleDao);
		daosByResourecClass.put(CodeSystem.class, codeSystemDao);
		daosByResourecClass.put(Endpoint.class, endpointDao);
		daosByResourecClass.put(Group.class, groupDao);
		daosByResourecClass.put(HealthcareService.class, healthcareServiceDao);
		daosByResourecClass.put(Location.class, locationDao);
		daosByResourecClass.put(NamingSystem.class, namingSystemDao);
		daosByResourecClass.put(Organization.class, organizationDao);
		daosByResourecClass.put(Patient.class, patientDao);
		daosByResourecClass.put(Practitioner.class, practitionerDao);
		daosByResourecClass.put(PractitionerRole.class, practitionerRoleDao);
		daosByResourecClass.put(Provenance.class, provenanceDao);
		daosByResourecClass.put(ResearchStudy.class, researchStudyDao);
		daosByResourecClass.put(StructureDefinition.class, structureDefinitionDao);
		daosByResourecClass.put(Subscription.class, subscriptionDao);
		daosByResourecClass.put(Task.class, taskDao);
		daosByResourecClass.put(ValueSet.class, valueSetDao);

		daosByResourecClass.forEach((k, v) -> daosByResourceTypeName.put(k.getAnnotation(ResourceDef.class).name(), v));
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(binaryDao, "binaryDao");
		Objects.requireNonNull(bundleDao, "bundleDao");
		Objects.requireNonNull(codeSystemDao, "codeSystemDao");
		Objects.requireNonNull(endpointDao, "endpointDao");
		Objects.requireNonNull(groupDao, "groupDao");
		Objects.requireNonNull(healthcareServiceDao, "healthcareServiceDao");
		Objects.requireNonNull(locationDao, "locationDao");
		Objects.requireNonNull(namingSystemDao, "namingSystemDao");
		Objects.requireNonNull(organizationDao, "organizationDao");
		Objects.requireNonNull(patientDao, "patientDao");
		Objects.requireNonNull(practitionerDao, "practitionerDao");
		Objects.requireNonNull(practitionerRoleDao, "practitionerRoleDao");
		Objects.requireNonNull(provenanceDao, "provenanceDao");
		Objects.requireNonNull(researchStudyDao, "researchStudyDao");
		Objects.requireNonNull(structureDefinitionDao, "structureDefinitionDao");
		Objects.requireNonNull(structureDefinitionSnapshotDao, "structureDefinitionSnapshotDao");
		Objects.requireNonNull(subscriptionDao, "subscriptionDao");
		Objects.requireNonNull(taskDao, "taskDao");
		Objects.requireNonNull(valueSetDao, "valueSetDao");
	}

	@Override
	public BinaryDao getBinaryDao()
	{
		return binaryDao;
	}

	@Override
	public BundleDao getBundleDao()
	{
		return bundleDao;
	}

	@Override
	public CodeSystemDao getCodeSystemDao()
	{
		return codeSystemDao;
	}

	@Override
	public EndpointDao getEndpointDao()
	{
		return endpointDao;
	}

	@Override
	public GroupDao getGroupDao()
	{
		return groupDao;
	}

	@Override
	public HealthcareServiceDao getHealthcareServiceDao()
	{
		return healthcareServiceDao;
	}

	@Override
	public LocationDao getLocationDao()
	{
		return locationDao;
	}

	@Override
	public NamingSystemDao getNamingSystemDao()
	{
		return namingSystemDao;
	}

	@Override
	public OrganizationDao getOrganizationDao()
	{
		return organizationDao;
	}

	@Override
	public PatientDao getPatientDao()
	{
		return patientDao;
	}

	@Override
	public PractitionerDao getPractitionerDao()
	{
		return practitionerDao;
	}

	@Override
	public PractitionerRoleDao getPractitionerRoleDao()
	{
		return practitionerRoleDao;
	}

	@Override
	public ProvenanceDao getProvenanceDao()
	{
		return provenanceDao;
	}

	@Override
	public ResearchStudyDao getResearchStudyDao()
	{
		return researchStudyDao;
	}

	@Override
	public StructureDefinitionDao getStructureDefinitionDao()
	{
		return structureDefinitionDao;
	}

	@Override
	public StructureDefinitionSnapshotDao getStructureDefinitionSnapshotDao()
	{
		return structureDefinitionSnapshotDao;
	}

	@Override
	public SubscriptionDao getSubscriptionDao()
	{
		return subscriptionDao;
	}

	@Override
	public TaskDao getTaskDao()
	{
		return taskDao;
	}

	@Override
	public ValueSetDao getValueSetDao()
	{
		return valueSetDao;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> Optional<? extends ResourceDao<R>> getDao(Class<R> resourceClass)
	{
		ResourceDao<R> value = (ResourceDao<R>) daosByResourecClass.get(resourceClass);
		return Optional.ofNullable(value);
	}

	@Override
	public Optional<ResourceDao<?>> getDao(String resourceTypeName)
	{
		ResourceDao<?> value = daosByResourceTypeName.get(resourceTypeName);
		return Optional.ofNullable(value);
	}
}
