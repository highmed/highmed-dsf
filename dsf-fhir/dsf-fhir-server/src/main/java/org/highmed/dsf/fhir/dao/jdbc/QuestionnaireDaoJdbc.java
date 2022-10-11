package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.QuestionnaireDao;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireDate;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireIdentifier;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireStatus;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireUrl;
import org.highmed.dsf.fhir.search.parameters.QuestionnaireVersion;
import org.highmed.dsf.fhir.search.parameters.user.QuestionnaireUserFilter;
import org.hl7.fhir.r4.model.Questionnaire;

import ca.uhn.fhir.context.FhirContext;

public class QuestionnaireDaoJdbc extends AbstractResourceDaoJdbc<Questionnaire> implements QuestionnaireDao
{
	private final ReadByUrlDaoJdbc<Questionnaire> readByUrl;

	public QuestionnaireDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource, FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, Questionnaire.class, "questionnaires",
				"questionnaire", "questionnaire_id", QuestionnaireUserFilter::new,
				with(QuestionnaireDate::new, QuestionnaireIdentifier::new, QuestionnaireStatus::new,
						QuestionnaireUrl::new, QuestionnaireVersion::new),
				with());

		readByUrl = new ReadByUrlDaoJdbc<>(this::getDataSource, this::getResource, getResourceTable(),
				getResourceColumn());
	}

	@Override
	protected Questionnaire copy(Questionnaire resource)
	{
		return resource.copy();
	}

	@Override
	public Optional<Questionnaire> readByUrlAndVersion(String urlAndVersion) throws SQLException
	{
		return readByUrl.readByUrlAndVersion(urlAndVersion);
	}

	@Override
	public Optional<Questionnaire> readByUrlAndVersionWithTransaction(Connection connection, String urlAndVersion)
			throws SQLException
	{
		return readByUrl.readByUrlAndVersionWithTransaction(connection, urlAndVersion);
	}

	@Override
	public Optional<Questionnaire> readByUrlAndVersion(String url, String version) throws SQLException
	{
		return readByUrl.readByUrlAndVersion(url, version);
	}

	@Override
	public Optional<Questionnaire> readByUrlAndVersionWithTransaction(Connection connection, String url, String version)
			throws SQLException
	{
		return readByUrl.readByUrlAndVersionWithTransaction(connection, url, version);
	}
}
