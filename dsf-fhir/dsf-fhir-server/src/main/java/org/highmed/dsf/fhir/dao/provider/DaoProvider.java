package org.highmed.dsf.fhir.dao.provider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import org.highmed.dsf.fhir.dao.ActivityDefinitionDao;
import org.highmed.dsf.fhir.dao.BinaryDao;
import org.highmed.dsf.fhir.dao.BundleDao;
import org.highmed.dsf.fhir.dao.CodeSystemDao;
import org.highmed.dsf.fhir.dao.DocumentReferenceDao;
import org.highmed.dsf.fhir.dao.EndpointDao;
import org.highmed.dsf.fhir.dao.GroupDao;
import org.highmed.dsf.fhir.dao.HealthcareServiceDao;
import org.highmed.dsf.fhir.dao.LibraryDao;
import org.highmed.dsf.fhir.dao.LocationDao;
import org.highmed.dsf.fhir.dao.MeasureDao;
import org.highmed.dsf.fhir.dao.MeasureReportDao;
import org.highmed.dsf.fhir.dao.NamingSystemDao;
import org.highmed.dsf.fhir.dao.OrganizationAffiliationDao;
import org.highmed.dsf.fhir.dao.OrganizationDao;
import org.highmed.dsf.fhir.dao.PatientDao;
import org.highmed.dsf.fhir.dao.PractitionerDao;
import org.highmed.dsf.fhir.dao.PractitionerRoleDao;
import org.highmed.dsf.fhir.dao.ProvenanceDao;
import org.highmed.dsf.fhir.dao.QuestionnaireDao;
import org.highmed.dsf.fhir.dao.QuestionnaireResponseDao;
import org.highmed.dsf.fhir.dao.ReadAccessDao;
import org.highmed.dsf.fhir.dao.ResearchStudyDao;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.dao.SubscriptionDao;
import org.highmed.dsf.fhir.dao.TaskDao;
import org.highmed.dsf.fhir.dao.ValueSetDao;
import org.hl7.fhir.r4.model.Resource;

public interface DaoProvider
{
	Connection newReadOnlyAutoCommitTransaction() throws SQLException;

	Connection newReadWriteTransaction() throws SQLException;

	ActivityDefinitionDao getActivityDefinitionDao();

	BinaryDao getBinaryDao();

	BundleDao getBundleDao();

	DocumentReferenceDao getDocumentReferenceDao();

	CodeSystemDao getCodeSystemDao();

	EndpointDao getEndpointDao();

	GroupDao getGroupDao();

	HealthcareServiceDao getHealthcareServiceDao();

	LibraryDao getLibraryDao();

	LocationDao getLocationDao();

	MeasureDao getMeasureDao();

	MeasureReportDao getMeasureReportDao();

	NamingSystemDao getNamingSystemDao();

	OrganizationDao getOrganizationDao();

	OrganizationAffiliationDao getOrganizationAffiliationDao();

	PatientDao getPatientDao();

	PractitionerDao getPractitionerDao();

	PractitionerRoleDao getPractitionerRoleDao();

	ProvenanceDao getProvenanceDao();

	QuestionnaireDao getQuestionnaireDao();

	QuestionnaireResponseDao getQuestionnaireResponseDao();

	ResearchStudyDao getResearchStudyDao();

	StructureDefinitionDao getStructureDefinitionDao();

	StructureDefinitionDao getStructureDefinitionSnapshotDao();

	SubscriptionDao getSubscriptionDao();

	TaskDao getTaskDao();

	ValueSetDao getValueSetDao();

	<R extends Resource> Optional<? extends ResourceDao<R>> getDao(Class<R> resourceClass);

	Optional<ResourceDao<?>> getDao(String resourceTypeName);

	ReadAccessDao getReadAccessDao();
}
