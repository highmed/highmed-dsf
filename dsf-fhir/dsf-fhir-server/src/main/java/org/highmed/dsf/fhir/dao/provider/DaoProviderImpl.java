package org.highmed.dsf.fhir.dao.provider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

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
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
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
	private final DataSource dataSource;
	private final ActivityDefinitionDao activityDefinitionDao;
	private final BinaryDao binaryDao;
	private final BundleDao bundleDao;
	private final DocumentReferenceDao documentReferenceDao;
	private final CodeSystemDao codeSystemDao;
	private final EndpointDao endpointDao;
	private final GroupDao groupDao;
	private final HealthcareServiceDao healthcareServiceDao;
	private final LibraryDao libraryDao;
	private final LocationDao locationDao;
	private final MeasureDao measureDao;
	private final MeasureReportDao measureReportDao;
	private final NamingSystemDao namingSystemDao;
	private final OrganizationDao organizationDao;
	private final OrganizationAffiliationDao organizationAffiliationDao;
	private final PatientDao patientDao;
	private final PractitionerDao practitionerDao;
	private final PractitionerRoleDao practitionerRoleDao;
	private final ProvenanceDao provenanceDao;
	private final QuestionnaireDao questionnaireDao;
	private final QuestionnaireResponseDao questionnaireResponseDao;
	private final ResearchStudyDao researchStudyDao;
	private final StructureDefinitionDao structureDefinitionDao;
	private final StructureDefinitionDao structureDefinitionSnapshotDao;
	private final SubscriptionDao subscriptionDao;
	private final TaskDao taskDao;
	private final ValueSetDao valueSetDao;

	private final ReadAccessDao readAccessDao;

	private final Map<Class<? extends Resource>, ResourceDao<?>> daosByResourceClass = new HashMap<>();
	private final Map<String, ResourceDao<?>> daosByResourceTypeName = new HashMap<>();

	public DaoProviderImpl(DataSource dataSource, ActivityDefinitionDao activityDefinitionDao, BinaryDao binaryDao,
			BundleDao bundleDao, CodeSystemDao codeSystemDao, DocumentReferenceDao documentReferenceDao,
			EndpointDao endpointDao, GroupDao groupDao, HealthcareServiceDao healthcareServiceDao,
			LibraryDao libraryDao, LocationDao locationDao, MeasureDao measureDao, MeasureReportDao measureReportDao,
			NamingSystemDao namingSystemDao, OrganizationDao organizationDao,
			OrganizationAffiliationDao organizationAffiliationDao, PatientDao patientDao,
			PractitionerDao practitionerDao, PractitionerRoleDao practitionerRoleDao, ProvenanceDao provenanceDao,
			QuestionnaireDao questionnaireDao, QuestionnaireResponseDao questionnaireResponseDao,
			ResearchStudyDao researchStudyDao, StructureDefinitionDao structureDefinitionDao,
			StructureDefinitionDao structureDefinitionSnapshotDao, SubscriptionDao subscriptionDao, TaskDao taskDao,
			ValueSetDao valueSetDao, ReadAccessDao readAccessDao)
	{
		this.dataSource = dataSource;
		this.activityDefinitionDao = activityDefinitionDao;
		this.binaryDao = binaryDao;
		this.bundleDao = bundleDao;
		this.codeSystemDao = codeSystemDao;
		this.documentReferenceDao = documentReferenceDao;
		this.endpointDao = endpointDao;
		this.groupDao = groupDao;
		this.healthcareServiceDao = healthcareServiceDao;
		this.libraryDao = libraryDao;
		this.locationDao = locationDao;
		this.measureDao = measureDao;
		this.measureReportDao = measureReportDao;
		this.namingSystemDao = namingSystemDao;
		this.organizationDao = organizationDao;
		this.organizationAffiliationDao = organizationAffiliationDao;
		this.patientDao = patientDao;
		this.practitionerDao = practitionerDao;
		this.practitionerRoleDao = practitionerRoleDao;
		this.provenanceDao = provenanceDao;
		this.questionnaireDao = questionnaireDao;
		this.questionnaireResponseDao = questionnaireResponseDao;
		this.researchStudyDao = researchStudyDao;
		this.structureDefinitionDao = structureDefinitionDao;
		this.structureDefinitionSnapshotDao = structureDefinitionSnapshotDao;
		this.subscriptionDao = subscriptionDao;
		this.taskDao = taskDao;
		this.valueSetDao = valueSetDao;

		this.readAccessDao = readAccessDao;

		daosByResourceClass.put(ActivityDefinition.class, activityDefinitionDao);
		daosByResourceClass.put(Binary.class, binaryDao);
		daosByResourceClass.put(Bundle.class, bundleDao);
		daosByResourceClass.put(CodeSystem.class, codeSystemDao);
		daosByResourceClass.put(DocumentReference.class, documentReferenceDao);
		daosByResourceClass.put(Endpoint.class, endpointDao);
		daosByResourceClass.put(Group.class, groupDao);
		daosByResourceClass.put(HealthcareService.class, healthcareServiceDao);
		daosByResourceClass.put(Library.class, libraryDao);
		daosByResourceClass.put(Location.class, locationDao);
		daosByResourceClass.put(Measure.class, measureDao);
		daosByResourceClass.put(MeasureReport.class, measureReportDao);
		daosByResourceClass.put(NamingSystem.class, namingSystemDao);
		daosByResourceClass.put(Organization.class, organizationDao);
		daosByResourceClass.put(OrganizationAffiliation.class, organizationAffiliationDao);
		daosByResourceClass.put(Patient.class, patientDao);
		daosByResourceClass.put(Practitioner.class, practitionerDao);
		daosByResourceClass.put(PractitionerRole.class, practitionerRoleDao);
		daosByResourceClass.put(Provenance.class, provenanceDao);
		daosByResourceClass.put(Questionnaire.class, questionnaireDao);
		daosByResourceClass.put(QuestionnaireResponse.class, questionnaireResponseDao);
		daosByResourceClass.put(ResearchStudy.class, researchStudyDao);
		daosByResourceClass.put(StructureDefinition.class, structureDefinitionDao);
		daosByResourceClass.put(Subscription.class, subscriptionDao);
		daosByResourceClass.put(Task.class, taskDao);
		daosByResourceClass.put(ValueSet.class, valueSetDao);

		daosByResourceClass.forEach((k, v) -> daosByResourceTypeName.put(k.getAnnotation(ResourceDef.class).name(), v));
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(activityDefinitionDao, "activityDefinitionDao");
		Objects.requireNonNull(binaryDao, "binaryDao");
		Objects.requireNonNull(bundleDao, "bundleDao");
		Objects.requireNonNull(codeSystemDao, "codeSystemDao");
		Objects.requireNonNull(documentReferenceDao, "documentReferenceDao");
		Objects.requireNonNull(endpointDao, "endpointDao");
		Objects.requireNonNull(groupDao, "groupDao");
		Objects.requireNonNull(healthcareServiceDao, "healthcareServiceDao");
		Objects.requireNonNull(libraryDao, "libraryDao");
		Objects.requireNonNull(locationDao, "locationDao");
		Objects.requireNonNull(measureDao, "measureDao");
		Objects.requireNonNull(measureReportDao, "measureReportDao");
		Objects.requireNonNull(namingSystemDao, "namingSystemDao");
		Objects.requireNonNull(organizationDao, "organizationDao");
		Objects.requireNonNull(organizationAffiliationDao, "organizationAffiliationDao");
		Objects.requireNonNull(patientDao, "patientDao");
		Objects.requireNonNull(practitionerDao, "practitionerDao");
		Objects.requireNonNull(practitionerRoleDao, "practitionerRoleDao");
		Objects.requireNonNull(provenanceDao, "provenanceDao");
		Objects.requireNonNull(questionnaireDao, "questionnaireDao");
		Objects.requireNonNull(questionnaireResponseDao, "questionnaireResponseDao");
		Objects.requireNonNull(researchStudyDao, "researchStudyDao");
		Objects.requireNonNull(structureDefinitionDao, "structureDefinitionDao");
		Objects.requireNonNull(structureDefinitionSnapshotDao, "structureDefinitionSnapshotDao");
		Objects.requireNonNull(subscriptionDao, "subscriptionDao");
		Objects.requireNonNull(taskDao, "taskDao");
		Objects.requireNonNull(valueSetDao, "valueSetDao");
	}

	@Override
	public Connection newReadOnlyAutoCommitTransaction() throws SQLException
	{
		Connection connection = dataSource.getConnection();

		if (!connection.isReadOnly() || !connection.getAutoCommit())
			throw new IllegalStateException("read only, auto commit connection expected from data source");

		return connection;
	}

	@Override
	public Connection newReadWriteTransaction() throws SQLException
	{
		Connection connection = dataSource.getConnection();
		connection.setReadOnly(false);
		connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
		connection.setAutoCommit(false);

		return connection;
	}

	@Override
	public ActivityDefinitionDao getActivityDefinitionDao()
	{
		return activityDefinitionDao;
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
	public DocumentReferenceDao getDocumentReferenceDao()
	{
		return documentReferenceDao;
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
	public LibraryDao getLibraryDao()
	{
		return libraryDao;
	}

	@Override
	public LocationDao getLocationDao()
	{
		return locationDao;
	}

	@Override
	public MeasureDao getMeasureDao()
	{
		return measureDao;
	}

	@Override
	public MeasureReportDao getMeasureReportDao()
	{
		return measureReportDao;
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
	public OrganizationAffiliationDao getOrganizationAffiliationDao()
	{
		return organizationAffiliationDao;
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
	public QuestionnaireDao getQuestionnaireDao()
	{
		return questionnaireDao;
	}

	@Override
	public QuestionnaireResponseDao getQuestionnaireResponseDao()
	{
		return questionnaireResponseDao;
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
	public StructureDefinitionDao getStructureDefinitionSnapshotDao()
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
	public <R extends Resource> Optional<? extends ResourceDao<R>> getDao(Class<R> resourceClass)
	{
		@SuppressWarnings("unchecked")
		ResourceDao<R> value = (ResourceDao<R>) daosByResourceClass.get(resourceClass);
		return Optional.ofNullable(value);
	}

	@Override
	public Optional<ResourceDao<?>> getDao(String resourceTypeName)
	{
		ResourceDao<?> value = daosByResourceTypeName.get(resourceTypeName);
		return Optional.ofNullable(value);
	}

	@Override
	public ReadAccessDao getReadAccessDao()
	{
		return readAccessDao;
	}
}
