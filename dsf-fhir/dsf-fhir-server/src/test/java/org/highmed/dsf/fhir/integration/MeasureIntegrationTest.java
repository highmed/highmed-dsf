package org.highmed.dsf.fhir.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.highmed.dsf.fhir.dao.LibraryDao;
import org.highmed.dsf.fhir.dao.MeasureDao;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.junit.Test;

public class MeasureIntegrationTest extends AbstractIntegrationTest
{
	private static Library createLibrary()
	{
		Library library = new Library();
		library.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/authorization-role").setCode("REMOTE");
		library.setUrl("https://foo.bar/fhir/Library/0a887526-2b9f-413a-8842-5e9252e2d7f7");
		library.setStatus(Enumerations.PublicationStatus.ACTIVE);
		library.getType().addCoding().setSystem("http://terminology.hl7.org/CodeSystem/library-type")
				.setCode("logic-library");
		library.getContentFirstRep().setContentType("text/cql").setData("Zm9vCg==".getBytes(StandardCharsets.UTF_8));

		return library;
	}

	private static Measure createMeasure()
	{
		Measure measure = new Measure();
		measure.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/authorization-role").setCode("REMOTE");
		measure.getLibrary()
				.add(new CanonicalType("https://foo.bar/fhir/Library/0a887526-2b9f-413a-8842-5e9252e2d7f7"));
		measure.setStatus(Enumerations.PublicationStatus.ACTIVE);
		measure.getScoring().addCoding().setSystem("http://terminology.hl7.org/CodeSystem/measure-scoring")
				.setCode("cohort");

		Measure.MeasureGroupPopulationComponent population = measure.getGroupFirstRep().getPopulationFirstRep();
		population.getCode().addCoding().setSystem("http://terminology.hl7.org/CodeSystem/measure-population")
				.setCode("initial-population");
		population.getCriteria().setLanguage("text/cql").setExpression("InInitialPopulation");

		return measure;
	}

	@Test
	public void testSearchIncludingLibrary() throws Exception
	{
		LibraryDao libraryDao = getSpringWebApplicationContext().getBean(LibraryDao.class);
		libraryDao.create(createLibrary());

		MeasureDao measureDao = getSpringWebApplicationContext().getBean(MeasureDao.class);
		Measure measure = measureDao.create(createMeasure());

		Bundle searchBundle = getWebserviceClient().search(Measure.class,
				Map.of("_id", Collections.singletonList(measure.getIdElement().getIdPart()), "_include",
						Collections.singletonList("Measure:depends-on")));

		assertNotNull(searchBundle.getEntry());
		assertEquals(2, searchBundle.getEntry().size());
		assertNotNull(searchBundle.getEntry().get(0));
		assertNotNull(searchBundle.getEntry().get(0).getResource());
		assertTrue(searchBundle.getEntry().get(0).getResource() instanceof Measure);
		assertNotNull(searchBundle.getEntry().get(0).getSearch());
		assertEquals(Bundle.SearchEntryMode.MATCH, searchBundle.getEntry().get(0).getSearch().getMode());

		assertNotNull(searchBundle.getEntry().get(1));
		assertNotNull(searchBundle.getEntry().get(1).getResource());
		assertTrue(searchBundle.getEntry().get(1).getResource() instanceof Library);
		assertNotNull(searchBundle.getEntry().get(1).getSearch());
		assertEquals(Bundle.SearchEntryMode.INCLUDE, searchBundle.getEntry().get(1).getSearch().getMode());
	}

	@Test
	public void testCreateValidByLocalUser() throws Exception
	{
		Measure measure = createMeasure();

		Measure created = getWebserviceClient().create(measure);
		assertNotNull(created);
		assertNotNull(created.getIdElement().getIdPart());
		assertNotNull(created.getIdElement().getVersionIdPart());
	}
}
