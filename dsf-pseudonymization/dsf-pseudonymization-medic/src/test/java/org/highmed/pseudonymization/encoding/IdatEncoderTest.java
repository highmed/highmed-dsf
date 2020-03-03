package org.highmed.pseudonymization.encoding;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;

import org.highmed.openehr.OpenEhrObjectMapperFactory;
import org.highmed.pseudonymization.base.IdatEncoded;
import org.highmed.pseudonymization.base.TtpId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class IdatEncoderTest
{

	IdatEncoder idEnc;
	private ObjectMapper rds;
	IdatContainer container;
	TtpId encodedTid;

	@BeforeEach
	public void setup() throws NoSuchAlgorithmException
	{
		rds = OpenEhrObjectMapperFactory.createObjectMapper();
		KeyGenerator keygen = KeyGenerator.getInstance("AES");
		keygen.init(256);

		idEnc = new IdatEncoder(keygen.generateKey(), "aadTag", "permutationSeed".getBytes(StandardCharsets.UTF_8), "UKHD");

		container = new IdatContainer("123A456B789C", "Hans", "Meier", "Male", "140331414", "74078", "Heilbronn",
				"Allee", "Germany", "987654321E");
	}

	@Test
	public void encodeContainerTest()
	{
		IdatEncoded enc = idEnc.encodeContainer(container); //todo
	}
}