package org.highmed.dsf.fhir.dao.jdbc;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.MeasureReportDao;
import org.highmed.dsf.fhir.search.parameters.MeasureReportIdentifier;
import org.highmed.dsf.fhir.search.parameters.user.MeasureReportUserFilter;
import org.hl7.fhir.r4.model.MeasureReport;

import ca.uhn.fhir.context.FhirContext;

public class MeasureReportDaoJdbc extends AbstractResourceDaoJdbc<MeasureReport> implements MeasureReportDao
{
	public MeasureReportDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource, FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, MeasureReport.class, "measure_reports",
				"measure_report", "measure_report_id", MeasureReportUserFilter::new, with(MeasureReportIdentifier::new),
				with());
	}

	@Override
	protected MeasureReport copy(MeasureReport resource)
	{
		return resource.copy();
	}
}
