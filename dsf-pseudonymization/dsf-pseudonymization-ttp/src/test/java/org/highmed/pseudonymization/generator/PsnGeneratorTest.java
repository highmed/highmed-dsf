package org.highmed.pseudonymization.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import org.highmed.pseudonymization.base.TtpId;

class PsnGeneratorTest
{

	private PsnGenerator gen;
	private List<TtpId> idList, idList2;
	private List<List<TtpId>> linkedIDs;

	@org.junit.jupiter.api.BeforeEach
	void setUp() throws NoSuchAlgorithmException
	{
		gen = new PsnGenerator();
		TtpId tid1 = new TtpId("UMG", UUID.randomUUID().toString());
		TtpId tid2 = new TtpId("MHH", UUID.randomUUID().toString());
		idList = new ArrayList<>();
		idList.add(tid1);
		idList.add(tid2);

		TtpId tid3 = new TtpId("UMG", UUID.randomUUID().toString());
		TtpId tid4 = new TtpId("MHH", UUID.randomUUID().toString());
		idList2 = new ArrayList<>();
		idList2.add(tid3);
		idList2.add(tid4);

		linkedIDs = new ArrayList<>();
		linkedIDs.add(idList);
		linkedIDs.add(idList2);

	}

	@org.junit.jupiter.api.Test
	void encodePseudonym() throws NoSuchPaddingException, ShortBufferException, InvalidAlgorithmParameterException,
			NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidKeyException
	{

		idList.forEach(id -> System.out.println("Plain ID: " + id));
		String encoded = gen.encodePseudonym(idList, 100);
		System.out.println("Encoded pseudonym: " + encoded);

		List<TtpId> decoded = gen.decodePseudonym(encoded);
		decoded.forEach(id -> System.out.println("Decoded ID: " + id));

		assertEquals(idList, decoded);

	}

	@org.junit.jupiter.api.Test
	void encodeMultiplePsns()
	{
	}

}
