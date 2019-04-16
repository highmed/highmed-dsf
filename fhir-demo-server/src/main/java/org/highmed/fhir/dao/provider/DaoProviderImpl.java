package org.highmed.fhir.dao.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.highmed.fhir.dao.CodeSystemDao;
import org.highmed.fhir.dao.DomainResourceDao;
import org.highmed.fhir.dao.EndpointDao;
import org.highmed.fhir.dao.HealthcareServiceDao;
import org.highmed.fhir.dao.LocationDao;
import org.highmed.fhir.dao.OrganizationDao;
import org.highmed.fhir.dao.PatientDao;
import org.highmed.fhir.dao.PractitionerDao;
import org.highmed.fhir.dao.PractitionerRoleDao;
import org.highmed.fhir.dao.ProvenanceDao;
import org.highmed.fhir.dao.ResearchStudyDao;
import org.highmed.fhir.dao.StructureDefinitionDao;
import org.highmed.fhir.dao.StructureDefinitionSnapshotDao;
import org.highmed.fhir.dao.SubscriptionDao;
import org.highmed.fhir.dao.TaskDao;
import org.highmed.fhir.dao.ValueSetDao;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.beans.factory.InitializingBean;

public class DaoProviderImpl implements DaoProvider, InitializingBean
{
	private final CodeSystemDao codeSystemDao;
	private final EndpointDao endpointDao;
	private final HealthcareServiceDao healthcareServiceDao;
	private final LocationDao locationDao;
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

	private final Map<Class<? extends DomainResource>, DomainResourceDao<?>> daos = new HashMap<>();

	public DaoProviderImpl(CodeSystemDao codeSystemDao, EndpointDao endpointDao,
			HealthcareServiceDao healthcareServiceDao, LocationDao locationDao, OrganizationDao organizationDao,
			PatientDao patientDao, PractitionerDao practitionerDao, PractitionerRoleDao practitionerRoleDao,
			ProvenanceDao provenanceDao, ResearchStudyDao researchStudyDao,
			StructureDefinitionDao structureDefinitionDao,
			StructureDefinitionSnapshotDao structureDefinitionSnapshotDao, SubscriptionDao subscriptionDao,
			TaskDao taskDao, ValueSetDao valueSetDao)
	{
		this.codeSystemDao = codeSystemDao;
		this.endpointDao = endpointDao;
		this.healthcareServiceDao = healthcareServiceDao;
		this.locationDao = locationDao;
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

		daos.put(CodeSystem.class, codeSystemDao);
		daos.put(Endpoint.class, endpointDao);
		daos.put(HealthcareService.class, healthcareServiceDao);
		daos.put(Location.class, locationDao);
		daos.put(Organization.class, organizationDao);
		daos.put(Patient.class, patientDao);
		daos.put(Practitioner.class, practitionerDao);
		daos.put(PractitionerRole.class, practitionerRoleDao);
		daos.put(Provenance.class, provenanceDao);
		daos.put(ResearchStudy.class, researchStudyDao);
		daos.put(StructureDefinition.class, structureDefinitionDao);
		daos.put(Subscription.class, subscriptionDao);
		daos.put(Task.class, taskDao);
		daos.put(ValueSet.class, valueSetDao);
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(codeSystemDao, "codeSystemDao");
		Objects.requireNonNull(endpointDao, "endpointDao");
		Objects.requireNonNull(healthcareServiceDao, "healthcareServiceDao");
		Objects.requireNonNull(locationDao, "locationDao");
		Objects.requireNonNull(organizationDao, "organizationDao");
		Objects.requireNonNull(patientDao, "patientDao");
		Objects.requireNonNull(practitionerDao, "practitionerDao");
		Objects.requireNonNull(provenanceDao, "provenanceDao");
		Objects.requireNonNull(researchStudyDao, "researchStudyDao");
		Objects.requireNonNull(structureDefinitionDao, "structureDefinitionDao");
		Objects.requireNonNull(structureDefinitionSnapshotDao, "structureDefinitionSnapshotDao");
		Objects.requireNonNull(subscriptionDao, "subscriptionDao");
		Objects.requireNonNull(taskDao, "taskDao");
		Objects.requireNonNull(valueSetDao, "valueSetDao");
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
	public <R extends DomainResource> Optional<? extends DomainResourceDao<R>> getDao(Class<R> resourceClass)
	{
		DomainResourceDao<?> value = daos.get(resourceClass);
		return (Optional<? extends DomainResourceDao<R>>) Optional.ofNullable(value);
	}
}
