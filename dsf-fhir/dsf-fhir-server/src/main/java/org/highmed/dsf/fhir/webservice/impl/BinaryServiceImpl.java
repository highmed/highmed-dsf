package org.highmed.dsf.fhir.webservice.impl;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.authorization.AuthorizationRuleProvider;
import org.highmed.dsf.fhir.dao.BinaryDao;
import org.highmed.dsf.fhir.event.EventGenerator;
import org.highmed.dsf.fhir.event.EventHandler;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.history.HistoryService;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.webservice.specification.BinaryService;
import org.hl7.fhir.r4.model.Binary;

import ca.uhn.fhir.rest.api.Constants;

public class BinaryServiceImpl extends AbstractResourceServiceImpl<BinaryDao, Binary> implements BinaryService
{
	public BinaryServiceImpl(String path, String serverBase, int defaultPageCount, BinaryDao dao,
			ResourceValidator validator, EventHandler eventHandler, ExceptionHandler exceptionHandler,
			EventGenerator eventGenerator, ResponseGenerator responseGenerator, ParameterConverter parameterConverter,
			ReferenceExtractor referenceExtractor, ReferenceResolver referenceResolver,
			ReferenceCleaner referenceCleaner, AuthorizationRuleProvider authorizationRuleProvider,
			HistoryService historyService)
	{
		super(path, Binary.class, serverBase, defaultPageCount, dao, validator, eventHandler, exceptionHandler,
				eventGenerator, responseGenerator, parameterConverter, referenceExtractor, referenceResolver,
				referenceCleaner, authorizationRuleProvider, historyService);
	}

	@Override
	public Response create(InputStream in, UriInfo uri, HttpHeaders headers)
	{
		throw new UnsupportedOperationException("Implemented and delegated by jaxrs layer");
	}

	@Override
	public Response update(String id, InputStream in, UriInfo uri, HttpHeaders headers)
	{
		throw new UnsupportedOperationException("Implemented and delegated by jaxrs layer");
	}

	@Override
	protected MediaType getMediaTypeForRead(UriInfo uri, HttpHeaders headers)
	{
		if (uri.getQueryParameters().containsKey(Constants.PARAM_FORMAT))
			return super.getMediaTypeForRead(uri, headers);
		else
			return getMediaType(uri, headers);
	}

	@Override
	protected MediaType getMediaTypeForVRead(UriInfo uri, HttpHeaders headers)
	{
		if (uri.getQueryParameters().containsKey(Constants.PARAM_FORMAT))
			return super.getMediaTypeForVRead(uri, headers);
		else
			return getMediaType(uri, headers);
	}

	private MediaType getMediaType(UriInfo uri, HttpHeaders headers)
	{
		List<MediaType> types = headers.getAcceptableMediaTypes();
		return types == null ? null : types.get(0);
	}
}
