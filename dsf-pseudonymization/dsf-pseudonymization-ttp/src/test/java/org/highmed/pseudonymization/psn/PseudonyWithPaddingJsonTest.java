package org.highmed.pseudonymization.psn;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.highmed.pseudonymization.recordlinkage.MedicId;
import org.highmed.pseudonymization.recordlinkage.TestMedicId;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;

public class PseudonyWithPaddingJsonTest
{
	@Test
	public void testWriteRead() throws Exception
	{
		MedicId medicId1 = new TestMedicId("org1", "value1");
		MedicId medicId2 = new TestMedicId("org2", "value2");
		PseudonymWithPadding p = new PseudonymWithPadding("", Arrays.asList(medicId1, medicId2));

		ObjectMapper o = new ObjectMapper();
		o.registerSubtypes(new NamedType(TestMedicId.class, "TestMedicId"));

		String string = o.writeValueAsString(p);
		System.out.println(string);

		PseudonymWithPadding read = o.readValue(string, PseudonymWithPadding.class);
		assertNotNull(read);
	}
}
