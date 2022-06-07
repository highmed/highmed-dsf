package org.highmed.dsf.fhir.resources;

import java.util.Objects;

import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.Questionnaire;

public class QuestionnaireResource extends AbstractResource
{
	private QuestionnaireResource(Class<? extends MetadataResource> type, String dependencyNameAndVersion,
			String questionnaireUrl, String questionnaireVersion, String questionnaireFileName)
	{
		super(type, dependencyNameAndVersion, questionnaireUrl, questionnaireVersion, null, questionnaireFileName);
	}

	public static QuestionnaireResource file(String questionnaireFileName)
	{
		return new QuestionnaireResource(Questionnaire.class, null, null, null,
				Objects.requireNonNull(questionnaireFileName, "questionnaireFileName"));
	}

	public static QuestionnaireResource dependency(String dependencyNameAndVersion, String questionnaireUrl,
			String questionnaireVersion)
	{
		return new QuestionnaireResource(Questionnaire.class,
				Objects.requireNonNull(dependencyNameAndVersion, "dependencyNameAndVersion"),
				Objects.requireNonNull(questionnaireUrl, "questionnaireUrl"),
				Objects.requireNonNull(questionnaireVersion, "questionnaireVersion"), null);
	}
}
