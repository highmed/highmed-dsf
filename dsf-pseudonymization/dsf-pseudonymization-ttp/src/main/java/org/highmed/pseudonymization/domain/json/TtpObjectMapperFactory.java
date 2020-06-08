package org.highmed.pseudonymization.domain.json;

import java.util.BitSet;

import org.highmed.openehr.json.OpenEhrObjectMapperFactory;
import org.highmed.pseudonymization.domain.impl.FhirMdatContainer;
import org.highmed.pseudonymization.domain.impl.MatchedPersonImpl;
import org.highmed.pseudonymization.domain.impl.MedicIdImpl;
import org.highmed.pseudonymization.domain.impl.OpenEhrMdatContainer;
import org.highmed.pseudonymization.domain.impl.PersonImpl;
import org.highmed.pseudonymization.json.BitSetDeserializer;
import org.highmed.pseudonymization.json.BitSetSerializer;
import org.hl7.fhir.r4.model.Resource;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;

import ca.uhn.fhir.context.FhirContext;

public final class TtpObjectMapperFactory
{
	private TtpObjectMapperFactory()
	{
	}

	public static ObjectMapper createObjectMapper(FhirContext fhirContext)
	{
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);

		objectMapper.registerModule(OpenEhrObjectMapperFactory.openEhrModule());

		SimpleModule module = new SimpleModule();
		module.addDeserializer(BitSet.class, new BitSetDeserializer());
		module.addSerializer(BitSet.class, new BitSetSerializer());
		module.addDeserializer(Resource.class, new ResourceDeserializer(fhirContext));
		module.addSerializer(Resource.class, new ResourceSerializer(fhirContext));
		objectMapper.registerModule(module);

		objectMapper.registerSubtypes(new NamedType(MedicIdImpl.class, "MedicId"));
		objectMapper.registerSubtypes(new NamedType(OpenEhrMdatContainer.class, "OpenEhrMdatContainer"));
		objectMapper.registerSubtypes(new NamedType(FhirMdatContainer.class, "FhirMdatContainer"));
		objectMapper.registerSubtypes(new NamedType(PersonImpl.class, "Person"));
		objectMapper.registerSubtypes(new NamedType(MatchedPersonImpl.class, "MatchedPerson"));

		return objectMapper;
	}
}
