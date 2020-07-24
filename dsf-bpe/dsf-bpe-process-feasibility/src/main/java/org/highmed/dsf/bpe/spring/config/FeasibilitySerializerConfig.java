package org.highmed.dsf.bpe.spring.config;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.highmed.dsf.bpe.plugin.FeasibilitySerializerPlugin;
import org.highmed.dsf.bpe.variables.BloomFilterConfigSerializer;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResultSerializer;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResultsSerializer;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResultSerializer;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResultsSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class FeasibilitySerializerConfig
{
	@Autowired
	FhirContext fhirContext;

	@Autowired
	public ObjectMapper objectMapper;

	@Bean
	public FeasibilityQueryResultSerializer feasibilityQueryResultSerializer()
	{
		return new FeasibilityQueryResultSerializer(objectMapper);
	}

	@Bean
	public FeasibilityQueryResultsSerializer feasibilityQueryResultsSerializer()
	{
		return new FeasibilityQueryResultsSerializer(objectMapper);
	}

	@Bean
	public FinalFeasibilityQueryResultSerializer finalFeasibilityQueryResultSerializer()
	{
		return new FinalFeasibilityQueryResultSerializer(objectMapper);
	}

	@Bean
	public FinalFeasibilityQueryResultsSerializer finalFeasibilityQueryResultsSerializer()
	{
		return new FinalFeasibilityQueryResultsSerializer(objectMapper);
	}

	@Bean
	public BloomFilterConfigSerializer bloomFilterConfigSerializer()
	{
		return new BloomFilterConfigSerializer(objectMapper);
	}

	@Bean
	public ProcessEnginePlugin feasibilitySerializerPlugin()
	{
		return new FeasibilitySerializerPlugin(feasibilityQueryResultSerializer(), feasibilityQueryResultsSerializer(),
				finalFeasibilityQueryResultSerializer(), finalFeasibilityQueryResultsSerializer(),
				bloomFilterConfigSerializer());
	}
}
