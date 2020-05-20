package org.highmed.pseudonymization.bloomfilter;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.BitSet;
import java.util.function.Supplier;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class BloomFilterGenerator
{
	public static final int NUMBER_OF_HASH_FUNCTIONS = 15;

	private final BigInteger bitSetLength;
	private final ThreadLocal<BiGramHasher> biGramHasher;

	public BloomFilterGenerator(int bitSetLength, Supplier<BiGramHasher> biGramHasherSupplier)
	{
		this.bitSetLength = BigInteger.valueOf(bitSetLength);

		biGramHasher = ThreadLocal.withInitial(biGramHasherSupplier);
	}

	public static BloomFilterGenerator withMd5Sha1BiGramHasher(int bitSetLength)
	{
		return new BloomFilterGenerator(bitSetLength, Md5Sha1BiGramHasher::new);
	}

	public static BloomFilterGenerator withHmacMd5HmacSha1BiGramHasher(int bitSetLength, byte[] key1, byte[] key2)
	{
		return new BloomFilterGenerator(bitSetLength, () -> new HmacMd5HmacSha1BiGramHasher(key1, key2));
	}

	public static BloomFilterGenerator withSha1Sha2BiGramHasher(int bitSetLength)
	{
		return new BloomFilterGenerator(bitSetLength, Sha1Sha2BiGramHasher::new);
	}

	public static BloomFilterGenerator withHmacSha1HmacSha2BiGramHasher(int bitSetLength, byte[] key1, byte[] key2,
			Provider provider)
	{
		return new BloomFilterGenerator(bitSetLength, () -> new HmacSha1HmacSha2BiGramHasher(key1, key2, provider));
	}

	public static BloomFilterGenerator withSha2Sha3BiGramHasher(int bitSetLength)
	{
		return new BloomFilterGenerator(bitSetLength, Sha2Sha3BiGramHasher::new);
	}

	public static BloomFilterGenerator withHmacSha2HmacSha3BiGramHasher(int bitSetLength, byte[] key1, byte[] key2)
	{
		return new BloomFilterGenerator(bitSetLength, () -> new HmacSha2HmacSha3BiGramHasher(key1, key2));
	}

	public static BloomFilterGenerator withHmacSha2HmacSha3BiGramHasher(int bitSetLength, byte[] key1, byte[] key2,
			Provider provider)
	{
		return new BloomFilterGenerator(bitSetLength, () -> new HmacSha2HmacSha3BiGramHasher(key1, key2, provider));
	}

	public BitSet generateBitSet(String input)
	{
		BitSet bitSet = new BitSet(bitSetLength.intValue());

		for (byte[] biGram : toBiGrams(input))
			for (int i = 0; i < NUMBER_OF_HASH_FUNCTIONS; i++)
				bitSet.set(calculateHashesAndMapToPosition(biGram, i));

		return bitSet;
	}

	private byte[][] toBiGrams(String src)
	{
		src = " " + src + " ";
		byte[][] biGrams = new byte[src.length() - 1][];
		for (int i = 0; i < biGrams.length; i++)
			biGrams[i] = src.substring(i, i + 2).getBytes(StandardCharsets.UTF_8);

		return biGrams;
	}

	protected int calculateHashesAndMapToPosition(byte[] biGram, int hashIteration)
	{
		BigInteger hash1 = new BigInteger(biGramHasher.get().firstHash(biGram));
		BigInteger hash2 = new BigInteger(biGramHasher.get().secondHash(biGram));

		return hash1.add(hash2.multiply(BigInteger.valueOf(hashIteration))).mod(bitSetLength).intValue();
	}

	public Class<? extends BiGramHasher> getBiGramHasherType()
	{
		return biGramHasher.get().getClass();
	}

	public static interface BiGramHasher
	{
		byte[] firstHash(byte[] biGram);

		byte[] secondHash(byte[] biGram);
	}

	public static class Md5Sha1BiGramHasher implements BiGramHasher
	{
		private final MessageDigest md5Digest;
		private final MessageDigest sha1Digest;

		public Md5Sha1BiGramHasher()
		{
			try
			{
				md5Digest = MessageDigest.getInstance("MD5");
				sha1Digest = MessageDigest.getInstance("SHA-1");
			}
			catch (NoSuchAlgorithmException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public byte[] firstHash(byte[] biGram)
		{
			try
			{
				return md5Digest.digest(biGram);
			}
			finally
			{
				md5Digest.reset();
			}
		}

		@Override
		public byte[] secondHash(byte[] biGram)
		{
			try
			{
				return sha1Digest.digest(biGram);
			}
			finally
			{
				sha1Digest.reset();
			}
		}
	}

	public static class HmacMd5HmacSha1BiGramHasher implements BiGramHasher
	{
		private final Mac md5Mac;
		private final Mac sha1Mac;

		public HmacMd5HmacSha1BiGramHasher(byte[] key1, byte[] key2)
		{
			this(new SecretKeySpec(key1, "HmacMD5"), new SecretKeySpec(key2, "HmacSHA1"));
		}

		public HmacMd5HmacSha1BiGramHasher(Key key1, Key key2)
		{
			if (!"HmacMD5".equals(key1.getAlgorithm()))
				throw new IllegalArgumentException("key1.algorithm HmacMD5 expected, but got " + key1.getAlgorithm());
			if (!"HmacSHA1".equals(key2.getAlgorithm()))
				throw new IllegalArgumentException("key2.algorithm HmacSHA1 expected, but got " + key2.getAlgorithm());

			try
			{
				md5Mac = Mac.getInstance("HmacMD5");
				md5Mac.init(key1);

				sha1Mac = Mac.getInstance("HmacSHA1");
				sha1Mac.init(key2);
			}
			catch (InvalidKeyException | NoSuchAlgorithmException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public byte[] firstHash(byte[] biGram)
		{
			try
			{
				return md5Mac.doFinal(biGram);
			}
			catch (IllegalStateException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public byte[] secondHash(byte[] biGram)
		{
			try
			{
				return sha1Mac.doFinal(biGram);
			}
			catch (IllegalStateException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	public static class Sha1Sha2BiGramHasher implements BiGramHasher
	{
		private final MessageDigest sha1Digest;
		private final MessageDigest sha2Digest;

		public Sha1Sha2BiGramHasher()
		{
			try
			{
				sha1Digest = MessageDigest.getInstance("SHA-1");
				sha2Digest = MessageDigest.getInstance("SHA-256");
			}
			catch (NoSuchAlgorithmException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public byte[] firstHash(byte[] biGram)
		{
			try
			{
				return sha1Digest.digest(biGram);
			}
			finally
			{
				sha1Digest.reset();
			}
		}

		@Override
		public byte[] secondHash(byte[] biGram)
		{
			try
			{
				return sha2Digest.digest(biGram);
			}
			finally
			{
				sha2Digest.reset();
			}
		}
	}

	public static class HmacSha1HmacSha2BiGramHasher implements BiGramHasher
	{
		private final Mac sha1Mac;
		private final Mac sha2Mac;

		public HmacSha1HmacSha2BiGramHasher(byte[] key1, byte[] key2)
		{
			this(key1, key2, null);
		}

		public HmacSha1HmacSha2BiGramHasher(byte[] key1, byte[] key2, Provider provider)
		{
			this(new SecretKeySpec(key1, "HmacSHA1"), new SecretKeySpec(key2, "HmacSHA256"), provider);
		}

		public HmacSha1HmacSha2BiGramHasher(Key key1, Key key2)
		{
			this(key1, key2, null);
		}

		public HmacSha1HmacSha2BiGramHasher(Key key1, Key key2, Provider provider)
		{
			if (!"HmacSHA1".equals(key1.getAlgorithm()))
				throw new IllegalArgumentException("key1.algorithm HmacSHA1 expected, but got " + key1.getAlgorithm());
			if (!"HmacSHA256".equals(key2.getAlgorithm()))
				throw new IllegalArgumentException(
						"key2.algorithm HmacSHA256 expected, but got " + key2.getAlgorithm());

			try
			{
				if (provider == null)
					sha1Mac = Mac.getInstance("HmacSHA1", provider);
				else
					sha1Mac = Mac.getInstance("HmacSHA1");
				sha1Mac.init(key1);

				if (provider == null)
					sha2Mac = Mac.getInstance("HmacSHA256");
				else
					sha2Mac = Mac.getInstance("HmacSHA256", provider);
				sha2Mac.init(key2);
			}
			catch (InvalidKeyException | NoSuchAlgorithmException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public byte[] firstHash(byte[] biGram)
		{
			try
			{
				return sha1Mac.doFinal(biGram);
			}
			catch (IllegalStateException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public byte[] secondHash(byte[] biGram)
		{
			try
			{
				return sha2Mac.doFinal(biGram);
			}
			catch (IllegalStateException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	public static class Sha2Sha3BiGramHasher implements BiGramHasher
	{
		private final MessageDigest sha2Digest;
		private final MessageDigest sha3Digest;

		public Sha2Sha3BiGramHasher()
		{
			try
			{
				sha2Digest = MessageDigest.getInstance("SHA-256");
				sha3Digest = MessageDigest.getInstance("SHA3-256");
			}
			catch (NoSuchAlgorithmException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public byte[] firstHash(byte[] biGram)
		{
			try
			{
				return sha2Digest.digest(biGram);
			}
			finally
			{
				sha2Digest.reset();
			}
		}

		@Override
		public byte[] secondHash(byte[] biGram)
		{
			try
			{
				return sha3Digest.digest(biGram);
			}
			finally
			{
				sha3Digest.reset();
			}
		}
	}

	public static class HmacSha2HmacSha3BiGramHasher implements BiGramHasher
	{
		private final Mac sha2Mac;
		private final Mac sha3Mac;

		public HmacSha2HmacSha3BiGramHasher(byte[] key1, byte[] key2)
		{
			this(key1, key2, null);
		}

		public HmacSha2HmacSha3BiGramHasher(byte[] key1, byte[] key2, Provider provider)
		{
			this(new SecretKeySpec(key1, "HmacSHA256"), new SecretKeySpec(key2, "HmacSHA3-256"), provider);
		}

		public HmacSha2HmacSha3BiGramHasher(Key key1, Key key2)
		{
			this(key1, key2, null);
		}

		public HmacSha2HmacSha3BiGramHasher(Key key1, Key key2, Provider provider)
		{
			if (!"HmacSHA256".equals(key1.getAlgorithm()))
				throw new IllegalArgumentException(
						"key1.algorithm HmacSHA256 expected, but got " + key1.getAlgorithm());
			if (!"HmacSHA3-256".equals(key2.getAlgorithm()))
				throw new IllegalArgumentException(
						"key2.algorithm HmacSHA3-256 expected, but got " + key2.getAlgorithm());
			try
			{
				if (provider == null)
					sha2Mac = Mac.getInstance("HmacSHA256");
				else
					sha2Mac = Mac.getInstance("HmacSHA256", provider);
				sha2Mac.init(key1);

				if (provider == null)
					sha3Mac = Mac.getInstance("HmacSHA3-256");
				else
					sha3Mac = Mac.getInstance("HmacSHA3-256", provider);
				sha3Mac.init(key2);
			}
			catch (InvalidKeyException | NoSuchAlgorithmException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public byte[] firstHash(byte[] biGram)
		{
			try
			{
				return sha2Mac.doFinal(biGram);
			}
			catch (IllegalStateException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public byte[] secondHash(byte[] biGram)
		{
			try
			{
				return sha3Mac.doFinal(biGram);
			}
			catch (IllegalStateException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
