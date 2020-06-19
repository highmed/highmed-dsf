package org.highmed.dsf.bpe.spring.config;

import java.util.NoSuchElementException;

import org.highmed.mpi.client.MasterPatientIndexClientFactory;
import org.highmed.mpi.client.MasterPatientIndexClientServiceLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MasterPatientIndexConfig
{
	@Value("${org.highmed.dsf.bpe.mpi.webservice.factory.class}")
	private String masterPatientIndexClientFactoryClass;

	@Bean
	public MasterPatientIndexClientServiceLoader masterPatientIndexClientServiceLoader()
	{
		return new MasterPatientIndexClientServiceLoader();
	}

	@Bean
	public MasterPatientIndexClientFactory masterPatientIndexClientFactory()
	{
		return masterPatientIndexClientServiceLoader()
				.getMasterPatientIndexClientFactory(masterPatientIndexClientFactoryClass)
				.orElseThrow(() -> new NoSuchElementException("Master patient index client factory with classname='"
						+ masterPatientIndexClientFactoryClass+ "' not found"));
	}
}
