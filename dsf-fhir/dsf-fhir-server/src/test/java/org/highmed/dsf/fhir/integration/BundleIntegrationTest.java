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
		Bundle allowList = readBundle(Paths.get("src/test/resources/integration/allow-list.json"),
				fhirContext.newJsonParser());

		logger.debug(fhirContext.newJsonParser().encodeResourceToString(allowList));

		Bundle updatedBundle = getWebserviceClient().updateConditionaly(allowList, Map.of("identifier",
				Collections.singletonList("http://highmed.org/fhir/CodeSystem/update-allowlist|highmed_allowlist")));

		assertNotNull(updatedBundle);
	}

	@Test
	public void testCreateBundleReturnMinimal() throws Exception
	{
		Bundle allowList = readBundle(Paths.get("src/test/resources/integration/allow-list.json"),
				fhirContext.newJsonParser());

		logger.debug(fhirContext.newJsonParser().encodeResourceToString(allowList));

		IdType id = getWebserviceClient().withMinimalReturn().updateConditionaly(allowList, Map.of("identifier",
				Collections.singletonList("http://highmed.org/fhir/CodeSystem/update-allowlist|highmed_allowlist")));

		assertNotNull(id);
	}

	@Test
	public void testCreateBundleReturnOperationOutcome() throws Exception
	{
		Bundle allowList = readBundle(Paths.get("src/test/resources/integration/allow-list.json"),
				fhirContext.newJsonParser());

		logger.debug(fhirContext.newJsonParser().encodeResourceToString(allowList));

		OperationOutcome outcome = getWebserviceClient().withOperationOutcomeReturn().updateConditionaly(allowList,
				Map.of("identifier", Collections
						.singletonList("http://highmed.org/fhir/CodeSystem/update-allowlist|highmed_allowlist")));

		assertNotNull(outcome);
	}
}
