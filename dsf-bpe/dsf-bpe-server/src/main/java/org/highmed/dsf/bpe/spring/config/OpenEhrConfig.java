package org.highmed.dsf.bpe.spring.config;

import org.highmed.dsf.openehr.client.OpenEhrClientProviderImpl;
import org.highmed.dsf.openehr.client.OpenEhrWebserviceClientProvider;

import org.highmed.openehr.deserializer.RowElementDeserializer;
import org.highmed.openehr.model.structure.RowElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
public class OpenEhrConfig
{
	@Value("${org.highmed.dsf.bpe.openehr.webservice.baseUrl}")
	private String baseUrl;

	@Value("${org.highmed.dsf.bpe.openehr.webservice.basicAuthUsername}")
	private String basicAuthUsername;

	@Value("${org.highmed.dsf.bpe.openehr.webservice.basicAuthPassword}")
	private String basicAuthPassword;

	@Value("${org.highmed.dsf.bpe.openehr.webservice.readTimeout}")
	private int readTimeout;

	@Value("${org.highmed.dsf.bpe.openehr.webservice.connectionTimeout}")
	private int connectTimeout;

	@Bean
	public ObjectMapper openEhrObjectMapper()
	{
		ObjectMapper mapper = new ObjectMapper();

		mapper.getFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
		mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

		SimpleModule module = new SimpleModule();
		module.addDeserializer(RowElement.class, rowElementDeserializer());

		mapper.registerModule(module);

		return mapper;
	}

	@Bean
	public RowElementDeserializer rowElementDeserializer()
	{
		return new RowElementDeserializer();
	}

	@Bean
	public OpenEhrWebserviceClientProvider webserviceClientProvider()
	{
		return new OpenEhrClientProviderImpl(baseUrl, basicAuthUsername, basicAuthPassword, connectTimeout, readTimeout,
				openEhrObjectMapper());
	}
}
