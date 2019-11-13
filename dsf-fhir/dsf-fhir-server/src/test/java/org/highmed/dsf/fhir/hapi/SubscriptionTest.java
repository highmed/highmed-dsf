package org.highmed.dsf.fhir.hapi;

import static org.junit.Assert.*;

import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionChannelComponent;
import org.hl7.fhir.r4.model.Subscription.SubscriptionChannelType;
import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.Constants;

public class SubscriptionTest
{
	private static final Logger logger = LoggerFactory.getLogger(SubscriptionTest.class);

	@Test
	public void testSubscriptionXml() throws Exception
	{
		Subscription subscription = new Subscription();
		subscription.setStatus(SubscriptionStatus.ACTIVE);
		subscription.setReason("Businness Process Engine");
		subscription.setCriteria("Task?status=requested");
		SubscriptionChannelComponent channel = subscription.getChannel();
		channel.setType(SubscriptionChannelType.WEBSOCKET);
		channel.setPayload(Constants.CT_FHIR_JSON_NEW);

		FhirContext context = FhirContext.forR4();
		String string = context.newXmlParser().setPrettyPrint(true).encodeResourceToString(subscription);
		assertNotNull(string);

		logger.info("Subscription (xml):\n{}", string);

		Subscription read = context.newXmlParser().parseResource(Subscription.class, string);
		assertNotNull(read);
		assertEquals(SubscriptionStatus.ACTIVE, read.getStatus());
		assertEquals(SubscriptionChannelType.WEBSOCKET, read.getChannel().getType());
		assertEquals(Constants.CT_FHIR_JSON_NEW, read.getChannel().getPayload());

		logger.info("Subscription (json):\n{}",
				context.newJsonParser().setPrettyPrint(true).encodeResourceToString(subscription));
	}
}
