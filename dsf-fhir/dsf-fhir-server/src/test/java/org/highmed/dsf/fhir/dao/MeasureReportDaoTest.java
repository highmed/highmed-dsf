package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.highmed.dsf.fhir.dao.jdbc.MeasureReportDaoJdbc;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.MeasureReport.MeasureReportStatus;

public class MeasureReportDaoTest extends AbstractResourceDaoTest<MeasureReport, MeasureReportDao>
{
	public MeasureReportDaoTest()
	{
		super(MeasureReport.class, MeasureReportDaoJdbc::new);
	}

	@Override
	protected MeasureReport createResource()
	{
		MeasureReport measureReport = new MeasureReport();
		measureReport.setStatus(MeasureReportStatus.PENDING);
		return measureReport;
	}

	@Override
	protected void checkCreated(MeasureReport resource)
	{
		assertEquals(MeasureReportStatus.PENDING, resource.getStatus());
	}

	@Override
	protected MeasureReport updateResource(MeasureReport resource)
	{
		resource.setStatus(MeasureReportStatus.COMPLETE);
		return resource;
	}

	@Override
	protected void checkUpdates(MeasureReport resource)
	{
		assertEquals(MeasureReportStatus.COMPLETE, resource.getStatus());
	}
}
