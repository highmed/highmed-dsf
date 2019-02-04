package org.highmed.bpe.spring.config;

import org.highmed.bpe.werbservice.TestService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebserviceConfig
{
	@Bean
	public TestService testService()
	{
		return new TestService();
	}
}
