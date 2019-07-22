package org.highmed.dsf.fhir.dao.provider;

import java.util.Optional;

import org.highmed.dsf.fhir.dao.*;
import org.hl7.fhir.r4.model.DomainResource;

public interface DaoProvider
{
	BinaryDao getBinaryDao();

	BundleDao getBundleDao();

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

	<R extends DomainResource> Optional<? extends ResourceDao<R>> getDao(Class<R> resourceClass);

	Optional<ResourceDao<?>> getDao(String resourceTypeName);
}
