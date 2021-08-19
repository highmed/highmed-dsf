package org.highmed.dsf.fhir.spring.config;

import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HelperConfig
{
	@Autowired
	private PropertiesConfig propertiesConfig;

	@Bean
	public ExceptionHandler exceptionHandler()
	{
		return new ExceptionHandler(responseGenerator());
	}

	@Bean
	public ResponseGenerator responseGenerator()
	{
		return new ResponseGenerator(propertiesConfig.getServerBaseUrl());
	}

	@Bean
	public ParameterConverter parameterConverter()
	{
		return new ParameterConverter(exceptionHandler());
	}
}
