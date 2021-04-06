package org.highmed.pseudonymization.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesGcmUtil
{
	private static final String AES = "AES";
	private static final String AES_MODE_PADDING = "AES/GCM/NoPadding";
	private static final int AES_KEY_SIZE = 256;
	private static final int GCM_IV_LENGTH = 12;
	private static final int GCM_TAG_LENGTH = 128;

	private static final SecureRandom random = new SecureRandom();

	public static SecretKey generateAES256Key() throws NoSuchAlgorithmException
	{
		KeyGenerator keyGen = KeyGenerator.getInstance(AES);
		keyGen.init(AES_KEY_SIZE);
		return keyGen.generateKey();
	}

	public static byte[] generateIv(int ivLength)
	{
		byte[] bytes = new byte[ivLength];
		random.nextBytes(bytes);
		return bytes;
	}

	public static byte[] encrypt(byte[] message, byte[] aadTag, SecretKey key)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, ShortBufferException
	{
		byte[] iv = generateIv(GCM_IV_LENGTH);

		Cipher cipher = Cipher.getInstance(AES_MODE_PADDING);
		SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES);
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

		cipher.updateAAD(aadTag);
		byte[] encrypted = cipher.doFinal(message);

		byte[] output = new byte[iv.length + encrypted.length];
		System.arraycopy(iv, 0, output, 0, iv.length);
		System.arraycopy(encrypted, 0, output, iv.length, encrypted.length);
		return output;
	}

	public static byte[] decrypt(byte[] encrypted, byte[] aadTag, SecretKey key)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
	{
		Cipher cipher = Cipher.getInstance(AES_MODE_PADDING);
		SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES);
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, encrypted, 0, GCM_IV_LENGTH);
		cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

		cipher.updateAAD(aadTag);
		return cipher.doFinal(encrypted, GCM_IV_LENGTH, encrypted.length - GCM_IV_LENGTH);
	}

	public static byte[] encryptWithStaticIv(byte[] message, byte[] addTag, SecretKey key, byte[] iv) throws Exception
	{
		Cipher cipher = Cipher.getInstance(AES_MODE_PADDING);
		SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES);
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

		cipher.updateAAD(addTag);
		return cipher.doFinal(message);
	}

	public static byte[] decryptWithStaticIv(byte[] encrypted, byte[] addTag, SecretKey key, byte[] iv)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
	{
		Cipher cipher = Cipher.getInstance(AES_MODE_PADDING);
		SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES);
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
		cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

		cipher.updateAAD(addTag);
		return cipher.doFinal(encrypted);
	}
}