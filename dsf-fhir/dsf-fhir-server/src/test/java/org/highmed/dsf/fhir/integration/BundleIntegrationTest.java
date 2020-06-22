package org.highmed.dsf.fhir.integration;

import static org.junit.Assert.assertNotNull;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleIntegrationTest extends AbstractIntegrationTest
{
	private static final Logger logger = LoggerFactory.getLogger(BundleIntegrationTest.class);

	@Test
	public void testCreateBundle() throws Exception
	{
		Bundle whiteList = readBundle(Paths.get("src/test/resources/integration/white-list.json"),
				fhirContext.newJsonParser());

		logger.debug(fhirContext.newJsonParser().encodeResourceToString(whiteList));

		Bundle updatedBundle = getWebserviceClient().updateConditionaly(whiteList, Map.of("identifier",
				Collections.singletonList("http://highmed.org/fhir/CodeSystem/update-whitelist|highmed_whitelist")));

		assertNotNull(updatedBundle);
	}

	@Test
	public void testCreateBundleReturnMinimal() throws Exception
	{
		Bundle whiteList = readBundle(Paths.get("src/test/resources/integration/white-list.json"),
				fhirContext.newJsonParser());

		logger.debug(fhirContext.newJsonParser().encodeResourceToString(whiteList));

		IdType id = getWebserviceClient().withMinimalReturn().updateConditionaly(whiteList, Map.of("identifier",
				Collections.singletonList("http://highmed.org/fhir/CodeSystem/update-whitelist|highmed_whitelist")));

		assertNotNull(id);
	}

	@Test
	public void testCreateBundleReturnOperationOutcome() throws Exception
	{
		Bundle whiteList = readBundle(Paths.get("src/test/resources/integration/white-list.json"),
				fhirContext.newJsonParser());

		logger.debug(fhirContext.newJsonParser().encodeResourceToString(whiteList));

		OperationOutcome outcome = getWebserviceClient().withOperationOutcomeReturn().updateConditionaly(whiteList,
				Map.of("identifier", Collections
						.singletonList("http://highmed.org/fhir/CodeSystem/update-whitelist|highmed_whitelist")));

		assertNotNull(outcome);
	}
}
