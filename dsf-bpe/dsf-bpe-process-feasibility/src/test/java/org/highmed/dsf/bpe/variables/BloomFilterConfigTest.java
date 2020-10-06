package org.highmed.dsf.bpe.variables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Base64;

import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BloomFilterConfigTest
{
	private static final Logger logger = LoggerFactory.getLogger(BloomFilterConfigTest.class);

	private static final byte[] b1 = Base64.getDecoder().decode("KLhuuy3lDSmo8A/mcYBBZJ+Xu+ok30qDGM4L0magwyY=");
	private static final byte[] b2 = Base64.getDecoder().decode("VALdwRisuEsUBIXaqJ01L9lk0jJUSGm5ZBE+Ha5bm8c=");
	private static final long l = -9139328758761390867L;

	@Test
	public void testReadWriteJson() throws Exception
	{
		ObjectMapper objectMapper = new ObjectMapper();

		BloomFilterConfig bfc = new BloomFilterConfig(l, new SecretKeySpec(b1, "HmacSHA256"),
				new SecretKeySpec(b2, "HmacSHA3-256"));

		String value = objectMapper.writeValueAsString(bfc);

		logger.debug("BloomFilterConfig: {}", value);

		BloomFilterConfig readBfc = objectMapper.readValue(value, BloomFilterConfig.class);

		assertNotNull(readBfc);
		assertEquals(bfc.getPermutationSeed(), readBfc.getPermutationSeed());
		assertEquals(bfc.getHmacSha2Key(), readBfc.getHmacSha2Key());
		assertEquals(bfc.getHmacSha3Key(), readBfc.getHmacSha3Key());
	}

	@Test
	public void testReadWriteBytes() throws Exception
	{
		BloomFilterConfig bfc = new BloomFilterConfig(l, new SecretKeySpec(b1, "HmacSHA256"),
				new SecretKeySpec(b2, "HmacSHA3-256"));

		byte[] bytes = bfc.toBytes();

		logger.debug("BloomFilterConfig: {}", Base64.getEncoder().encodeToString(bytes));

		BloomFilterConfig fromBytes = BloomFilterConfig.fromBytes(bytes);

		assertNotNull(fromBytes);
		assertEquals(bfc.getPermutationSeed(), fromBytes.getPermutationSeed());
		assertEquals(bfc.getHmacSha2Key(), fromBytes.getHmacSha2Key());
		assertEquals(bfc.getHmacSha3Key(), fromBytes.getHmacSha3Key());
	}
}
