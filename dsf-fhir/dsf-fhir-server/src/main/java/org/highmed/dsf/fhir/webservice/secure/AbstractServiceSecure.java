package org.highmed.dsf.fhir.webservice.secure;

import java.util.Objects;
import java.util.function.Function;

import javax.ws.rs.core.Response;

import org.highmed.dsf.fhir.authentication.UserProvider;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.BasicService;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractServiceSecure<S extends BasicService> implements BasicService, InitializingBean
{
	protected final S delegate;
	protected final ResponseGenerator responseGenerator;

	protected UserProvider provider;

	public AbstractServiceSecure(S delegate, ResponseGenerator responseGenerator)
	{
		this.delegate = delegate;
		this.responseGenerator = responseGenerator;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(delegate, "delegate");
		Objects.requireNonNull(responseGenerator, "responseGenerator");
	}

	@Override
	public final void setUserProvider(UserProvider provider)
	{
		delegate.setUserProvider(provider);

		this.provider = provider;
	}

	@Override
	public final String getPath()
	{
		return delegate.getPath();
	}

	protected final Function<String, Response> forbidden(String operation)
	{
		return reason -> responseGenerator.forbiddenNotAllowed(operation, provider.getCurrentUser(), reason);
	}
}
