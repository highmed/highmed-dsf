package org.highmed.dsf.fhir.questionnaire;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Test;

public class QuestionnaireResponseTest
{
	@Test
	public void testFlattenItemsToLeaves()
	{
		QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();

		QuestionnaireResponse.QuestionnaireResponseItemComponent node1 = questionnaireResponse.addItem();
		node1.setLinkId("Item 1.1 LinkId").addAnswer().setValue(new StringType("Item 1.1 Value"));

		QuestionnaireResponse.QuestionnaireResponseItemComponent node2 = questionnaireResponse.addItem();
		node2.addItem().setLinkId("Item 2.1 LinkId").addAnswer().setValue(new StringType("Item 2.1 Value"));
		node2.addItem().setLinkId("Item 2.2 LinkId").addAnswer().setValue(new StringType("Item 2.2 Value"));

		QuestionnaireResponse.QuestionnaireResponseItemComponent node3 = questionnaireResponse.addItem();
		node3.addItem().addItem().setLinkId("Item 3.1 LinkId").addAnswer().setValue(new StringType("Item 3.1 Value"));

		QuestionnaireResponseHelper questionnaireResponseHelper = new QuestionnaireResponseHelperImpl();

		List<QuestionnaireResponse.QuestionnaireResponseItemComponent> itemLeavesAsList = questionnaireResponseHelper
				.getItemLeavesAsList(questionnaireResponse);

		assertEquals(4, itemLeavesAsList.size());
	}
}
