package org.highmed.fhir.spring.config;

import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HelperConfig
{
	@Value("${org.highmed.fhir.serverBase}")
	private String serverBase;

	@Bean
	public ExceptionHandler exceptionHandler()
	{
		return new ExceptionHandler(responseGenerator());
	}

	@Bean
	public ResponseGenerator responseGenerator()
	{
		return new ResponseGenerator(serverBase);
	}

	@Bean
	public ParameterConverter parameterConverter()
	{
		return new ParameterConverter(exceptionHandler());
	}
}
