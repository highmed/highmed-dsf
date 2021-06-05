package org.highmed.dsf.fhir.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.Enumerations.FHIRVersion;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.r4.model.StructureDefinition.TypeDerivationRule;
import org.hl7.fhir.r4.model.UriType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

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
				Collections.singletonList("http://highmed.org/fhir/CodeSystem/update-allow-list|highmed_allow_list")));

		assertNotNull(updatedBundle);
	}

	@Test
	public void testCreateBundleReturnMinimal() throws Exception
	{
		Bundle allowList = readBundle(Paths.get("src/test/resources/integration/allow-list.json"),
				fhirContext.newJsonParser());

		logger.debug(fhirContext.newJsonParser().encodeResourceToString(allowList));

		IdType id = getWebserviceClient().withMinimalReturn().updateConditionaly(allowList, Map.of("identifier",
				Collections.singletonList("http://highmed.org/fhir/CodeSystem/update-allow-list|highmed_allow_list")));

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
						.singletonList("http://highmed.org/fhir/CodeSystem/update-allow-list|highmed_allow_list")));

		assertNotNull(outcome);
	}

	@Test
	public void testDeleteTaskProfileViaBundleTestSupportedProfilesInConformanceStatement() throws Exception
	{
		final String taskProfileUrl = "http://highmed.org/fhir/StructureDefinition/highmed-task-test";
		final String taskProfileVersion = "1.2.3";

		StructureDefinition newS = new StructureDefinition();
		newS.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ALL");
		newS.setUrl(taskProfileUrl);
		newS.setVersion(taskProfileVersion);
		newS.setName("TaskTest");
		newS.setStatus(PublicationStatus.ACTIVE);
		newS.setFhirVersion(FHIRVersion._4_0_1);
		newS.setKind(StructureDefinitionKind.RESOURCE);
		newS.setAbstract(false);
		newS.setType("Task");
		newS.setBaseDefinition("http://highmed.org/fhir/StructureDefinition/task-base");
		newS.setDerivation(TypeDerivationRule.CONSTRAINT);
		ElementDefinition diff = newS.getDifferential().addElement();
		diff.setId("Task.instantiatesUri");
		diff.setPath("Task.instantiatesUri");
		diff.setFixed(new UriType("http://highmed.org/bpe/Process/taskTest/1.2.3"));

		assertFalse(testProfileSupported(taskProfileUrl));

		IdType newSid = getWebserviceClient().withMinimalReturn().create(newS);
		assertNotNull(newSid);

		assertTrue(testProfileSupported(taskProfileUrl));

		Bundle deleteBundle = new Bundle();
		deleteBundle.setType(BundleType.TRANSACTION);
		BundleEntryComponent deleteEntry = deleteBundle.addEntry();
		deleteEntry.setFullUrl(newSid.withServerBase(getWebserviceClient().getBaseUrl(), "Task").toString());

		BundleEntryRequestComponent request = deleteEntry.getRequest();
		request.setMethod(HTTPVerb.DELETE);
		request.setUrl("StructureDefinition/" + newSid.getIdPart());

		getWebserviceClient().withMinimalReturn().postBundle(deleteBundle);

		assertFalse(testProfileSupported(taskProfileUrl));
		StructureDefinitionDao sDdao = getSpringWebApplicationContext().getBean("structureDefinitionDao",
				StructureDefinitionDao.class);
		StructureDefinitionDao sDsDao = getSpringWebApplicationContext().getBean("structureDefinitionSnapshotDao",
				StructureDefinitionDao.class);

		assertTrue(sDdao.readByUrlAndVersion(taskProfileUrl, taskProfileVersion).isEmpty());
		assertTrue(sDsDao.readByUrlAndVersion(taskProfileUrl, taskProfileVersion).isEmpty());
	}

	private boolean testProfileSupported(String taskProfileUrl)
	{
		CapabilityStatement conformance = getWebserviceClient().getConformance();
		assertNotNull(conformance);
		assertTrue(conformance.hasRest());
		assertEquals(1, conformance.getRest().size());
		CapabilityStatementRestComponent rest = conformance.getRest().get(0);
		assertNotNull(rest);
		assertTrue(rest.hasResource());
		assertTrue(rest.getResource().size() > 0);

		Optional<CapabilityStatementRestResourceComponent> taskResourceOpt = rest.getResource().stream()
				.filter(r -> "Task".equals(r.getType())).findFirst();
		assertTrue(taskResourceOpt.isPresent());

		CapabilityStatementRestResourceComponent taskResource = taskResourceOpt.get();
		if (taskResource.hasSupportedProfile())
		{
			assertTrue(taskResource.getSupportedProfile().size() > 0);
			List<CanonicalType> profiles = taskResource.getSupportedProfile();

			return profiles.stream().filter(t -> Objects.equal(t.getValue(), taskProfileUrl)).count() == 1;
		}
		else
			return false;
	}
}
