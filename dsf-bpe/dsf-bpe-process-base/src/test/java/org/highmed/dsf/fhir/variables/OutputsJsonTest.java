package org.highmed.dsf.fhir.variables;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OutputsJsonTest
{
	@Test
	public void testReadWrite() throws Exception
	{
		Outputs o = new Outputs(Collections.singleton(new Output("system", "code", "value")));
		ObjectMapper mapper = new ObjectMapper();

		String stringValue = mapper.writeValueAsString(o);

		Outputs read = mapper.readValue(stringValue, Outputs.class);
		assertNotNull(read);
		assertNotNull(read.getOutputs());
		assertEquals(o.getOutputs().size(), read.getOutputs().size());
		assertEquals(o.getOutputs().get(0).getSystem(), read.getOutputs().get(0).getSystem());
		assertEquals(o.getOutputs().get(0).getCode(), read.getOutputs().get(0).getCode());
		assertEquals(o.getOutputs().get(0).getValue(), read.getOutputs().get(0).getValue());
	}
}
