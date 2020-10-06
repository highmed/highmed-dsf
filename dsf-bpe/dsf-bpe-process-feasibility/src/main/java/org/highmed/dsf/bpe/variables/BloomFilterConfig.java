package org.highmed.dsf.bpe.variables;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import org.highmed.dsf.fhir.variables.KeyDeserializer;
import org.highmed.dsf.fhir.variables.KeySerializer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class BloomFilterConfig
{
	private static final int SEED_LENGTH = 8;
	private static final int HMAC_SHA2_KEY_LENGTH = 32;
	private static final int HMAC_SHA3_KEY_LENGTH = 32;

	private final long permutationSeed;

	@JsonDeserialize(using = KeyDeserializer.class)
	@JsonSerialize(using = KeySerializer.class)
	private final Key hmacSha2Key;

	@JsonDeserialize(using = KeyDeserializer.class)
	@JsonSerialize(using = KeySerializer.class)
	private final Key hmacSha3Key;

	public static BloomFilterConfig fromBytes(byte[] bytes)
	{
		if (bytes.length != SEED_LENGTH + HMAC_SHA2_KEY_LENGTH + HMAC_SHA3_KEY_LENGTH)
			throw new IllegalArgumentException(
					"bytes.length = " + (SEED_LENGTH + HMAC_SHA2_KEY_LENGTH + HMAC_SHA3_KEY_LENGTH)
							+ " expected, but got " + bytes.length);

		byte[] seed = new byte[SEED_LENGTH];
		byte[] key1 = new byte[HMAC_SHA2_KEY_LENGTH];
		byte[] key2 = new byte[HMAC_SHA3_KEY_LENGTH];

		System.arraycopy(bytes, 0, seed, 0, seed.length);
		System.arraycopy(bytes, seed.length, key1, 0, key1.length);
		System.arraycopy(bytes, seed.length + key1.length, key2, 0, key2.length);

		long permutationSeed = bigEndianToLong(seed);
		Key hmacSha2Key = new SecretKeySpec(key1, "HmacSHA256");
		Key hmacSha3Key = new SecretKeySpec(key2, "HmacSHA3-256");

		return new BloomFilterConfig(permutationSeed, hmacSha2Key, hmacSha3Key);
	}

	@JsonCreator
	public BloomFilterConfig(@JsonProperty("permutationSeed") long permutationSeed,
			@JsonProperty("hmacSha2Key") Key hmacSha2Key, @JsonProperty("hmacSha3Key") Key hmacSha3Key)
	{
		this.permutationSeed = permutationSeed;
		this.hmacSha2Key = hmacSha2Key;
		this.hmacSha3Key = hmacSha3Key;
	}

	public long getPermutationSeed()
	{
		return permutationSeed;
	}

	public Key getHmacSha2Key()
	{
		return hmacSha2Key;
	}

	public Key getHmacSha3Key()
	{
		return hmacSha3Key;
	}

	@JsonIgnore
	public byte[] toBytes()
	{
		byte[] bytes = new byte[SEED_LENGTH + HMAC_SHA2_KEY_LENGTH + HMAC_SHA3_KEY_LENGTH];

		byte[] seed = longToBigEndian(permutationSeed);
		byte[] key1 = hmacSha2Key.getEncoded();
		byte[] key2 = hmacSha3Key.getEncoded();

		System.arraycopy(seed, 0, bytes, 0, seed.length);
		System.arraycopy(key1, 0, bytes, SEED_LENGTH, key1.length);
		System.arraycopy(key2, 0, bytes, SEED_LENGTH + HMAC_SHA2_KEY_LENGTH, key2.length);

		return bytes;
	}

	private static byte[] longToBigEndian(long l)
	{
		return new byte[] { (byte) (l >>> 56), (byte) (l >>> 48), (byte) (l >>> 40), (byte) (l >>> 32),
				(byte) (l >>> 24), (byte) (l >>> 16), (byte) (l >>> 8), (byte) (l >>> 0) };
	}

	private static long bigEndianToLong(byte[] b)
	{
		return (((long) b[0] << 56) + ((long) (b[1] & 255) << 48) + ((long) (b[2] & 255) << 40)
				+ ((long) (b[3] & 255) << 32) + ((long) (b[4] & 255) << 24) + ((b[5] & 255) << 16) + ((b[6] & 255) << 8)
				+ ((b[7] & 255) << 0));
	}
}
