package org.highmed.dsf.bpe.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MasterPatientIndexConfig
{
	@Value("${org.highmed.dsf.bpe.mpi.client.factory.class}")
	private String masterPatientIndexClientFactoryClass;

	@Autowired
	private Environment environment;
}
