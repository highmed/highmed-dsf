package org.highmed.dsf.fhir.adapter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.TimeType;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.UriType;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class QuestionnaireResponseHtmlFhirAdapter extends HtmlFhirAdapter<QuestionnaireResponse>
{
	private static final String CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY = "business-key";
	private static final String CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID = "user-task-id";

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private static final SimpleDateFormat DATE_TIME_DISPLAY_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	public QuestionnaireResponseHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, QuestionnaireResponse.class);
	}

	@Override
	protected boolean isHtmlEnabled()
	{
		return true;
	}

	@Override
	protected void doWriteHtml(QuestionnaireResponse questionnaireResponse, OutputStreamWriter out) throws IOException
	{
		boolean isCompleted = QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED
				.equals(questionnaireResponse.getStatus());
		out.write("<div id=\"spinner\" class=\"spinner spinner-disabled\"></div>");
		out.write("<form>\n");
		out.write("<div class=\"row row-info " + (isCompleted ? "info-color-completed" : "info-color-progress")
				+ "\">\n");

		out.write("<div>");
		out.write("<svg class=\"info-icon\" id=\"info-icon\" viewBox=\"0 0 24 24\">\n");
		out.write("<title>Info</title>\n");
		out.write("<path class=\"" + (isCompleted ? "info-path-completed" : "info-path-progress")
				+ "\" d=\"M12 2c5.514 0 10 4.486 10 10s-4.486 10-10 10-10-4.486-10-10 4.486-10 10-10zm0-2c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm-.001 5.75c.69 0 1.251.56 1.251 1.25s-.561 1.25-1.251 1.25-1.249-.56-1.249-1.25.559-1.25 1.249-1.25zm2.001 12.25h-4v-1c.484-.179 1-.201 1-.735v-4.467c0-.534-.516-.618-1-.797v-1h3v6.265c0 .535.517.558 1 .735v.999z\"/>\"/>\n");
		out.write("</svg>\n");
		out.write("</div>\n");

		String urlVersion = questionnaireResponse.getQuestionnaire();
		String[] urlVersionSplit = urlVersion.split("\\|");
		String href = "/fhir/Questionnaire?url=" + urlVersionSplit[0] + "&version=" + urlVersionSplit[1];

		out.write("<div>");
		out.write("<p>\n");
		out.write("This QuestionnaireResponse answers the Questionnaire:</br><b><a class=\"info-link "
				+ (isCompleted ? "info-link-completed" : "info-link-progress") + "\" href=\"" + href + "\">"
				+ urlVersion + "</b></a>");
		out.write("</p>\n");
		out.write("<ul class=\"info-list\">\n");
		out.write("<li><b>State:</b> " + questionnaireResponse.getStatus().getDisplay() + "</li>\n");
		out.write("<li><b>Process instance-id:</b> " + getProcessInstanceId(questionnaireResponse) + "</li>\n");

		String lastUpdated = DATE_TIME_DISPLAY_FORMAT.format(questionnaireResponse.getMeta().getLastUpdated());
		if (isCompleted)
		{
			out.write("<li><b>Completion date:</b> " + lastUpdated + "</li>\n");
		}
		else
		{
			out.write("<li><b>Creation date:</b> " + lastUpdated + "</li>\n");
		}

		out.write("</ul>\n");
		out.write("</div>\n");
		out.write("</div>\n");

		out.write("<fieldset id=\"qr-form-fieldset\" " + (isCompleted ? "disabled=\"disabled\"" : "") + ">\n");

		for (QuestionnaireResponse.QuestionnaireResponseItemComponent item : questionnaireResponse.getItem())
		{
			writeRow(item, isCompleted, out);
		}

		out.write("<div class=\"row row-submit\" id=\"submit-row\">\n");
		out.write("<button type=\"button\" id=\"submit\" class=\"submit\" onclick=\"completeQuestionnaireResponse();\" "
				+ (isCompleted ? "disabled" : "") + ">Submit</button>\n");
		out.write("</div>\n");
		out.write("</fieldset>\n");
		out.write("</form>\n");
	}

	private String getProcessInstanceId(QuestionnaireResponse questionnaireResponse)
	{
		return questionnaireResponse.getItem().stream()
				.filter(i -> CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY.equals(i.getLinkId()))
				.flatMap(i -> i.getAnswer().stream()).map(a -> ((StringType) a.getValue()).getValue()).findFirst()
				.orElse("unknown");
	}

	private void writeRow(QuestionnaireResponse.QuestionnaireResponseItemComponent item, boolean isCompleted,
			OutputStreamWriter out) throws IOException
	{
		if (item.hasAnswer())
			writeFormRow(item, isCompleted, out);
		else
			writeDisplayRow(item, out);
	}

	private void writeDisplayRow(QuestionnaireResponse.QuestionnaireResponseItemComponent item, OutputStreamWriter out)
			throws IOException
	{
		String linkId = item.getLinkId();

		out.write("<div class=\"row row-display\" id=\"" + linkId + "-display-row\"" + style(linkId) + ">\n");
		out.write("<p class=\"p-display\">" + item.getText() + "</label>\n");
		out.write("</div>\n");
	}

	private void writeFormRow(QuestionnaireResponse.QuestionnaireResponseItemComponent item, boolean isCompleted,
			OutputStreamWriter out) throws IOException
	{
		String linkId = item.getLinkId();

		out.write("<div class=\"row\" id=\"" + linkId + "-answer-row\"" + style(linkId) + ">\n");
		out.write("<label for=\"" + linkId + "-label\">" + item.getText() + "</label>\n");

		writeFormInput(item.getAnswerFirstRep(), linkId, isCompleted, out);

		out.write("<ul class=\"error-list-not-visible\" id=\"" + linkId + "-error\">\n");
		out.write("</ul>\n");
		out.write("</div>\n");
	}

	private String style(String linkId)
	{
		return display(linkId) ? "" : "style=\"display:none;\"";
	}

	private boolean display(String linkId)
	{
		return !(CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_BUSINESS_KEY.equals(linkId)
				|| CODESYSTEM_HIGHMED_BPMN_USER_TASK_VALUE_USER_TASK_ID.equals(linkId));
	}

	private void writeFormInput(QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answerPlaceholder,
			String linkId, boolean isCompleted, OutputStreamWriter out) throws IOException
	{
		Type type = answerPlaceholder.getValue();

		// if type is null, the corresponding Questionnaire.item is of type display
		if (type != null)
		{
			if (type instanceof StringType)
			{
				String value = ((StringType) type).getValue();
				out.write("<input type=\"text\" id=\"" + linkId + "\" name=\"" + linkId + "\" "
						+ (isCompleted ? "value=\"" + value + "\"" : "placeholder=\"" + value + "\"")
						+ "\"></input>\n");
			}
			else if (type instanceof IntegerType)
			{
				String value = String.valueOf(((IntegerType) type).getValue());
				out.write("<input type=\"number\" id=\"" + linkId + "\" name=\"" + linkId + "\" step=\"1\" "
						+ (isCompleted ? "value=\"" + value + "\"" : "placeholder=\"" + value + "\"") + "></input>\n");
			}
			else if (type instanceof DecimalType)
			{
				String value = String.valueOf(((DecimalType) type).getValue());
				out.write("<input type=\"number\" id=\"" + linkId + "\" name=\"" + linkId + "\" step=\"0.01\" "
						+ (isCompleted ? "value=\"" + value + "\"" : "placeholder=\"" + value + "\"") + "></input>\n");
			}
			else if (type instanceof BooleanType)
			{
				boolean valueIsTrue = ((BooleanType) type).getValue();

				out.write("<div>\n");
				out.write("<label class=\"radio\"><input type=\"radio\" id=\"" + linkId + "\" name=\"" + linkId
						+ "\" value=\"true\" " + ((valueIsTrue) ? "checked" : "") + "/>Yes</label>\n");
				out.write("<label class=\"radio\"><input type=\"radio\" id=\"" + linkId + "\" name=\"" + linkId
						+ "\" value=\"false\" " + ((!valueIsTrue) ? "checked" : "") + "/>No</label>\n");
				out.write("</div>\n");
			}
			else if (type instanceof DateType)
			{
				Date value = ((DateType) type).getValue();
				String date = DATE_FORMAT.format(value);

				out.write("<input type=\"date\" id=\"" + linkId + "\" name=\"" + linkId + "\" "
						+ (isCompleted ? "value=\"" + date + "\"" : "placeholder=\"yyyy.MM.dd\"") + "></input>\n");
			}
			else if (type instanceof TimeType)
			{
				String value = ((TimeType) type).getValue();
				out.write("<input type=\"time\" id=\"" + linkId + "\" name=\"" + linkId + "\" "
						+ (isCompleted ? "value=\"" + value + "\"" : "placeholder=\"hh:mm:ss\"") + "></input>\n");
			}
			else if (type instanceof DateTimeType)
			{
				Date value = ((DateTimeType) type).getValue();
				String dateTime = DATE_TIME_FORMAT.format(value);

				out.write("<input type=\"datetime-local\" id=\"" + linkId + "\" name=\"" + linkId + "\" "
						+ (isCompleted ? "value=\"" + dateTime + "\"" : "placeholder=\"yyyy.MM.dd hh:mm:ss\"")
						+ "></input>\n");
			}
			else if (type instanceof UriType)
			{
				String value = ((UriType) type).getValue();
				out.write("<input type=\"url\" id=\"" + linkId + "\" name=\"" + linkId + "\" "
						+ (isCompleted ? "value=\"" + value + "\"" : "placeholder=\"" + value + "\"") + "></input>\n");
			}
			else if (type instanceof Reference)
			{
				String value = ((Reference) type).getReference();
				out.write("<input type=\"url\" id=\"" + linkId + "\" name=\"" + linkId + "\" "
						+ (isCompleted ? "value=\"" + value + "\"" : "placeholder=\"" + value + "\"") + "></input>\n");
			}
			else
			{
				throw new RuntimeException("Answer type '" + type.getClass().getName()
						+ "' in QuestionnaireResponse.item is not supported");
			}
		}
	}
}
