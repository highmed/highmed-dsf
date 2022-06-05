package org.highmed.dsf.fhir.spring.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.ActivityDefinitionDao;
import org.highmed.dsf.fhir.dao.BinaryDao;
import org.highmed.dsf.fhir.dao.BundleDao;
import org.highmed.dsf.fhir.dao.CodeSystemDao;
import org.highmed.dsf.fhir.dao.DocumentReferenceDao;
import org.highmed.dsf.fhir.dao.EndpointDao;
import org.highmed.dsf.fhir.dao.GroupDao;
import org.highmed.dsf.fhir.dao.HealthcareServiceDao;
import org.highmed.dsf.fhir.dao.HistoryDao;
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
import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.dao.SubscriptionDao;
import org.highmed.dsf.fhir.dao.TaskDao;
import org.highmed.dsf.fhir.dao.ValueSetDao;
import org.highmed.dsf.fhir.dao.jdbc.ActivityDefinitionDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.BinaryDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.BundleDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.CodeSystemDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.DocumentReferenceDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.EndpointDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.GroupDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.HealthcareServiceDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.HistroyDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.LibraryDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.LocationDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.MeasureDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.MeasureReportDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.NamingSystemDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.OrganizationAffiliationDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.OrganizationDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.PatientDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.PractitionerDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.PractitionerRoleDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.ProvenanceDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.QuestionnaireDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.QuestionnaireResponseDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.ReadAccessDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.ResearchStudyDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.StructureDefinitionDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.StructureDefinitionSnapshotDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.SubscriptionDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.TaskDaoJdbc;
import org.highmed.dsf.fhir.dao.jdbc.ValueSetDaoJdbc;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.dao.provider.DaoProviderImpl;
import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoConfig
{
	@Autowired
	private PropertiesConfig propertiesConfig;

	@Autowired
	private FhirConfig fhirConfig;

	@Bean
	public BasicDataSource dataSource()
	{
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(Driver.class.getName());
		dataSource.setUrl(propertiesConfig.getDbUrl());
		dataSource.setUsername(propertiesConfig.getDbUsername());
		dataSource.setPassword(toString(propertiesConfig.getDbPassword()));
		dataSource.setDefaultReadOnly(true);

		dataSource.setTestOnBorrow(true);
		dataSource.setValidationQuery("SELECT 1");
		return dataSource;
	}

	@Bean
	public BasicDataSource permanentDeleteDataSource()
	{
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(Driver.class.getName());
		dataSource.setUrl(propertiesConfig.getDbUrl());
		dataSource.setUsername(propertiesConfig.getDbPermanentDeleteUsername());
		dataSource.setPassword(toString(propertiesConfig.getDbPermanentDeletePassword()));
		dataSource.setDefaultReadOnly(true);

		dataSource.setTestOnBorrow(true);
		dataSource.setValidationQuery("SELECT 1");
		return dataSource;
	}

	private String toString(char[] password)
	{
		return password == null ? null : String.valueOf(password);
	}

	@Bean
	public ActivityDefinitionDao activityDefinitionDao()
	{
		return new ActivityDefinitionDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public BinaryDao binaryDao()
	{
		return new BinaryDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public BundleDao bundleDao()
	{
		return new BundleDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public CodeSystemDao codeSystemDao()
	{
		return new CodeSystemDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public DocumentReferenceDao documentReferenceDao()
	{
		return new DocumentReferenceDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public EndpointDao endpointDao()
	{
		return new EndpointDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public GroupDao groupDao()
	{
		return new GroupDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public HealthcareServiceDao healthcareServiceDao()
	{
		return new HealthcareServiceDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public LibraryDao libraryDao()
	{
		return new LibraryDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public LocationDao locationDao()
	{
		return new LocationDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public MeasureDao measureDao()
	{
		return new MeasureDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public MeasureReportDao measureReportDao()
	{
		return new MeasureReportDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public NamingSystemDao namingSystemDao()
	{
		return new NamingSystemDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public OrganizationDao organizationDao()
	{
		return new OrganizationDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public OrganizationAffiliationDao organizationAffiliationDao()
	{
		return new OrganizationAffiliationDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public PatientDao patientDao()
	{
		return new PatientDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public PractitionerDao practitionerDao()
	{
		return new PractitionerDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public PractitionerRoleDao practitionerRoleDao()
	{
		return new PractitionerRoleDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public ProvenanceDao provenanceDao()
	{
		return new ProvenanceDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public QuestionnaireDao questionnaireDao()
	{
		return new QuestionnaireDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public QuestionnaireResponseDao questionnaireResponseDao()
	{
		return new QuestionnaireResponseDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public ResearchStudyDao researchStudyDao()
	{
		return new ResearchStudyDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public StructureDefinitionDao structureDefinitionDao()
	{
		return new StructureDefinitionDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public StructureDefinitionDao structureDefinitionSnapshotDao()
	{
		return new StructureDefinitionSnapshotDaoJdbc(dataSource(), permanentDeleteDataSource(),
				fhirConfig.fhirContext());
	}

	@Bean
	public SubscriptionDao subscriptionDao()
	{
		return new SubscriptionDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public TaskDao taskDao()
	{
		return new TaskDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public ValueSetDao valueSetDao()
	{
		return new ValueSetDaoJdbc(dataSource(), permanentDeleteDataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public DaoProvider daoProvider()
	{
		return new DaoProviderImpl(dataSource(), activityDefinitionDao(), binaryDao(), bundleDao(), codeSystemDao(),
				documentReferenceDao(), endpointDao(), groupDao(), healthcareServiceDao(), libraryDao(), locationDao(),
				measureDao(), measureReportDao(), namingSystemDao(), organizationDao(), organizationAffiliationDao(),
				patientDao(), practitionerDao(), practitionerRoleDao(), provenanceDao(), questionnaireDao(),
				questionnaireResponseDao(), researchStudyDao(), structureDefinitionDao(),
				structureDefinitionSnapshotDao(), subscriptionDao(), taskDao(), valueSetDao(), readAccessDao());
	}

	@Bean
	public HistoryDao historyDao()
	{
		return new HistroyDaoJdbc(dataSource(), fhirConfig.fhirContext(), (BinaryDaoJdbc) binaryDao());
	}

	@Bean
	public ReadAccessDao readAccessDao()
	{
		return new ReadAccessDaoJdbc(dataSource());
	}
}
