package org.highmed.fhir.dao.provider;

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
import org.hl7.fhir.r4.model.DomainResource;

public interface DaoProvider
{
	CodeSystemDao getCodeSystemDao();

	EndpointDao getEndpointDao();

	HealthcareServiceDao getHealthcareServiceDao();

	LocationDao getLocationDao();

	OrganizationDao getOrganizationDao();

	PatientDao getPatientDao();

	PractitionerDao getPractitionerDao();

	PractitionerRoleDao getPractitionerRoleDao();

	ProvenanceDao getProvenanceDao();

	ResearchStudyDao getResearchStudyDao();

	StructureDefinitionDao getStructureDefinitionDao();

	StructureDefinitionSnapshotDao getStructureDefinitionSnapshotDao();

	SubscriptionDao getSubscriptionDao();

	TaskDao getTaskDao();

	ValueSetDao getValueSetDao();

	<R extends DomainResource> Optional<? extends DomainResourceDao<R>> getDao(Class<R> resourceClass);

	Optional<DomainResourceDao<?>> getDao(String resourceTypeName);
}
