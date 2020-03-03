package org.highmed.pseudonymization.encoding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.KeyGenerator;

import org.highmed.openehr.OpenEhrObjectMapperFactory;
import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.openehr.model.structure.RowElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MdatEncoderTest
{

	private ObjectMapper rds;
	private MdatEncoder medEnc;
	private ResultSet result;

	@BeforeEach
	public void setup() throws NoSuchAlgorithmException, IOException
	{
		rds = OpenEhrObjectMapperFactory.createObjectMapper();
		KeyGenerator keygen = KeyGenerator.getInstance("AES");
		keygen.init(256);
		medEnc = new MdatEncoder(keygen.generateKey(), "aadTag");

		try (InputStream in = Files.newInputStream(Paths.get("src/test/resources/result_1.json")))
		{
			result = rds.readValue(in, ResultSet.class);
			assertNotNull(result);
		}
	}

	@Test
	public void encryptRowTest()
	{
		List<RowElement> row = result.getRow(0);
		String rowID = row.get(4).getValueAsString();

		List<RowElement> encryptedRow = medEnc.encryptRow(row, 4);

		assertNotNull(encryptedRow);
		assertEquals(encryptedRow.size(), (row.size() - 1));

		encryptedRow.add(4, new StringRowElement(rowID));

		List<RowElement> decryptedRow = medEnc.decryptRow(encryptedRow, 4);

		assertNotNull(decryptedRow);
		assertEquals(decryptedRow.size(), row.size());
		for (RowElement elem : decryptedRow)
		{
			int idx = decryptedRow.indexOf(elem);
			if (elem == null)
			{
				assertEquals(decryptedRow.get(idx), row.get(idx));
			}
			else
			{
				assertEquals(decryptedRow.get(idx).getValueAsString(), row.get(idx).getValueAsString());
			}
		}
	}

}