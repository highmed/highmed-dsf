package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.highmed.dsf.fhir.authentication.UserProvider;
import org.highmed.dsf.fhir.webservice.specification.BasicService;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.rest.api.Constants;

@Consumes({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
		Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
@Produces({ MediaType.TEXT_HTML, Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON,
		Constants.CT_FHIR_XML, Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
public abstract class AbstractServiceJaxrs<S extends BasicService> implements BasicService, InitializingBean
{
	@Context
	private volatile HttpServletRequest httpRequest;

	protected final S delegate;

	public AbstractServiceJaxrs(S delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		setUserProvider(new UserProvider(() -> httpRequest));
	}

	@Override
	public final String getPath()
	{
		return delegate.getPath();
	}

	@Override
	public final void setUserProvider(UserProvider provider)
	{
		delegate.setUserProvider(provider);
	}
}
