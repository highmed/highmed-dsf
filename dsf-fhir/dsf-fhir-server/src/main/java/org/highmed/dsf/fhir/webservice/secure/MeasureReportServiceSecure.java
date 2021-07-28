package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.authorization.AuthorizationRule;
import org.highmed.dsf.fhir.dao.MeasureReportDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.webservice.specification.MeasureReportService;
import org.hl7.fhir.r4.model.MeasureReport;

public class MeasureReportServiceSecure
		extends AbstractResourceServiceSecure<MeasureReportDao, MeasureReport, MeasureReportService>
		implements MeasureReportService
{
	public MeasureReportServiceSecure(MeasureReportService delegate, String serverBase,
			ResponseGenerator responseGenerator, ReferenceResolver referenceResolver, ReferenceCleaner referenceCleaner,
			ReferenceExtractor referenceExtractor, MeasureReportDao measureReportDao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, AuthorizationRule<MeasureReport> authorizationRule,
			ResourceValidator resourceValidator)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, referenceCleaner, referenceExtractor,
				MeasureReport.class, measureReportDao, exceptionHandler, parameterConverter, authorizationRule,
				resourceValidator);
	}
}
