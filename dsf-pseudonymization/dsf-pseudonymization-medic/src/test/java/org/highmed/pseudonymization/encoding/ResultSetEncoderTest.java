package org.highmed.pseudonymization.encoding;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;

import org.highmed.openehr.OpenEhrObjectMapperFactory;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.io.MpiLookup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultSetEncoderTest
{

	private static final String FILE_PATH = "src/test/resources/result_1.json";
	private static final String FILE_PATH_NO_ID = "src/test/resources/result_1_noid.json";

	private ObjectMapper rds;
	private ResultSetEncoder rsEnc;
	private ResultSet result, noid;

	@Mock
	private MpiLookup mpi = Mockito.mock(MpiLookup.class);

	@BeforeEach
	public void setup() throws NoSuchAlgorithmException, IOException
	{
		Mockito.when(mpi.fetchIdat(Mockito.anyString())).
				thenAnswer(new Answer<IdatContainer>()
				{
					@Override
					public IdatContainer answer(InvocationOnMock invocation)
					{
						Object[] args = invocation.getArguments();
						String idString = (String) args[0];
						return new IdatContainer(idString, "John", "Doe", "Male", "30071987", "74078", "Heilbronn",
								"Allee", "Germany", "1234E");
					}
				});

		rds = OpenEhrObjectMapperFactory.createObjectMapper();
		KeyGenerator keygen = KeyGenerator.getInstance("AES");
		keygen.init(256);

		rsEnc = new ResultSetEncoder(keygen.generateKey(), keygen.generateKey(), "aadTag",
				"permutationSeed".getBytes(StandardCharsets.UTF_8), "UKHD", mpi);

		try (InputStream in = Files.newInputStream(Paths.get(FILE_PATH)))
		{
			result = rds.readValue(in, ResultSet.class);
			assertNotNull(result);
		}

		try (InputStream in = Files.newInputStream(Paths.get(FILE_PATH_NO_ID)))
		{
			noid = rds.readValue(in, ResultSet.class);
			assertNotNull(noid);
		}

	}

	@Test
	public void testEncodeResultSet() throws NoSuchFieldException, JsonProcessingException
	{
		ResultSet encoded = rsEnc.encodeResultSet(result);
		assertNotNull(encoded);
		System.out.println(rds.writeValueAsString(encoded));
	}

	@Test
	public void testEncodeIDlessResultSet()
	{
		assertThrows(NoSuchFieldException.class, () -> {
			rsEnc.encodeResultSet(noid);
		});
	}
}
