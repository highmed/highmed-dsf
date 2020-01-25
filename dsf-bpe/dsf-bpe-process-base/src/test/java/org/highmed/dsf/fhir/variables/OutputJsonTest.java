package org.highmed.dsf.fhir.variables;

import static org.junit.Assert.*;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OutputJsonTest
{
	@Test
	public void testReadWrite() throws Exception
	{
		Output o = new Output("system", "code", "value");
		ObjectMapper mapper = new ObjectMapper();
		
		String stringValue = mapper.writeValueAsString(o);
		
		Output read = mapper.readValue(stringValue, Output.class);
		assertNotNull(read);
		assertEquals(o.getSystem(), read.getSystem());
		assertEquals(o.getCode(), read.getCode());
		assertEquals(o.getValue(), read.getValue());
	}
}
