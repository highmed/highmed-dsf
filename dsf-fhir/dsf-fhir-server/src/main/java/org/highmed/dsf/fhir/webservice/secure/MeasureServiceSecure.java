package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.authorization.AuthorizationRule;
import org.highmed.dsf.fhir.dao.MeasureDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.webservice.specification.MeasureService;
import org.hl7.fhir.r4.model.Measure;

public class MeasureServiceSecure extends AbstractResourceServiceSecure<MeasureDao, Measure, MeasureService>
		implements MeasureService
{
	public MeasureServiceSecure(MeasureService delegate, String serverBase, ResponseGenerator responseGenerator,
			ReferenceResolver referenceResolver, ReferenceCleaner referenceCleaner,
			ReferenceExtractor referenceExtractor, MeasureDao measureDao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, AuthorizationRule<Measure> authorizationRule,
			ResourceValidator resourceValidator)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, referenceCleaner, referenceExtractor,
				Measure.class, measureDao, exceptionHandler, parameterConverter, authorizationRule, resourceValidator);
	}
}
