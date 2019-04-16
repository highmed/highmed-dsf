package org.highmed.fhir.webservice.impl;

import java.util.Objects;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.authentication.UserProvider;
import org.highmed.fhir.dao.command.CommandFactory;
import org.highmed.fhir.dao.command.CommandList;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.webservice.specification.RootService;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.springframework.beans.factory.InitializingBean;

public class RootServiceImpl implements RootService, InitializingBean
{
	private final CommandFactory commandFactory;
	private final ResponseGenerator responseGenerator;
	private final ParameterConverter parameterConverter;

	public RootServiceImpl(CommandFactory commandFactory, ResponseGenerator responseGenerator,
			ParameterConverter parameterConverter)
	{
		this.commandFactory = commandFactory;
		this.responseGenerator = responseGenerator;
		this.parameterConverter = parameterConverter;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(commandFactory, "commandFactory");
		Objects.requireNonNull(responseGenerator, "responseGenerator");
		Objects.requireNonNull(parameterConverter, "parameterConverter");
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
		if (bundle.getType() != null
				&& (BundleType.BATCH.equals(bundle.getType()) || BundleType.TRANSACTION.equals(bundle.getType())))
		{
			CommandList commands = commandFactory.createCommands(bundle);
			Bundle result = commands.execute();

			return responseGenerator.response(Status.OK, result, parameterConverter.getMediaType(uri, headers)).build();
		}

		return Response.status(Status.BAD_REQUEST).build(); // TODO
	}
}
