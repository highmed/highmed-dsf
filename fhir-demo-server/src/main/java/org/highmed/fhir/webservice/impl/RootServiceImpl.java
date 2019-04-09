package org.highmed.fhir.webservice.impl;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.authentication.UserProvider;
import org.highmed.fhir.webservice.specification.RootService;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;

public class RootServiceImpl implements RootService, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(RootServiceImpl.class);

	public RootServiceImpl()
	{
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
	}

	@Override
	public void setUserProvider(UserProvider provider)
	{
	}

	@Override
	public String getPath()
	{
		throw new UnsupportedOperationException("implemented by jaxrs service layer");
	}

	@Override
	public Response handleBundle(Bundle bundle, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("handleBundle ... TODO");

		logger.debug(FhirContext.forR4().newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle));
		
		// TODO Auto-generated method stub

		return Response.ok().build();
	}
}
