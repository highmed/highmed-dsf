package org.highmed.fhir.dao;

public interface DaoProvider
{
	CodeSystemDao getCodeSystemDao();

	EndpointDao getEndpointDao();

	HealthcareServiceDao getHealthcareServiceDao();

	LocationDao getLocationDao();

	OrganizationDao getOrganizationDao();

	PatientDao getPatientDao();

	PractitionerDao getPractitionerDao();

	ProvenanceDao getProvenanceDao();

	ResearchStudyDao getResearchStudyDao();

	StructureDefinitionDao getStructureDefinitionDao();

	StructureDefinitionSnapshotDao getStructureDefinitionSnapshotDao();

	SubscriptionDao getSubscriptionDao();

	TaskDao getTaskDao();

	ValueSetDao getValueSetDao();
}
