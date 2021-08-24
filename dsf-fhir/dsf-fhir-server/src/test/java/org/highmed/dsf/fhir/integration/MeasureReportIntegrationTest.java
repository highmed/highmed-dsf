package org.highmed.dsf.fhir.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.highmed.dsf.fhir.dao.MeasureDao;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.junit.Test;

public class MeasureReportIntegrationTest extends AbstractIntegrationTest
{
	private static Measure createMeasure()
	{
		Measure measure = new Measure();
		measure.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ALL");
		measure.setUrl("https://foo.bar/fhir/Measure/8cc30173-8b85-4418-882a-a3b8a9652fc6");
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
	public void testCreateValidByLocalUser() throws Exception
	{
		MeasureDao measureDao = getSpringWebApplicationContext().getBean(MeasureDao.class);
		Measure measure = measureDao.create(createMeasure());
		assertEquals("https://foo.bar/fhir/Measure/8cc30173-8b85-4418-882a-a3b8a9652fc6", measure.getUrl());

		MeasureReport measureReport = new MeasureReport();
		measureReport.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/read-access-tag").setCode("ALL");
		measureReport.setStatus(MeasureReport.MeasureReportStatus.COMPLETE);
		measureReport.setType(MeasureReport.MeasureReportType.SUMMARY);
		measureReport.setMeasure("https://foo.bar/fhir/Measure/8cc30173-8b85-4418-882a-a3b8a9652fc6");
		measureReport.getPeriod().setStart(new Date());

		MeasureReport.MeasureReportGroupPopulationComponent population = measureReport.getGroupFirstRep()
				.getPopulationFirstRep();
		population.getCode().addCoding().setSystem("http://terminology.hl7.org/CodeSystem/measure-population")
				.setCode("initial-population");
		population.setCount(42);

		MeasureReport created = getWebserviceClient().create(measureReport);
		assertNotNull(created);
		assertNotNull(created.getIdElement().getIdPart());
		assertNotNull(created.getIdElement().getVersionIdPart());
	}
}
