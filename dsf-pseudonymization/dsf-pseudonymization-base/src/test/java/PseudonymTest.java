import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.highmed.pseudonymization.base.Pseudonym;
import org.highmed.pseudonymization.base.TtpId;

import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;


public class PseudonymTest
{
	private Pseudonym psn;
	private ObjectMapper mapper;

	@org.junit.jupiter.api.BeforeEach
	void setUp()
	{
		mapper = new ObjectMapper();
		List<TtpId> idList = new ArrayList<>();
		idList.add(new TtpId(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
		idList.add(new TtpId(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
		this.psn = new Pseudonym(idList);
	}

	@org.junit.jupiter.api.Test
	void testSerialize() throws IOException
	{
		String marshalled = mapper.writeValueAsString(psn);
		Pseudonym unmarshalled = mapper.readValue(marshalled, Pseudonym.class);
		assertNotNull(unmarshalled);
		assertNotSame(psn, unmarshalled);
		assertEquals(psn, unmarshalled);
	}

	@org.junit.jupiter.api.Test
	void withPadding()
	{
		Random rngsus = new Random();
		int paddingLength = rngsus.nextInt(100);
		String paddingBefore = psn.getPadding();

		Pseudonym newPsn = psn.withPadding(paddingLength);
		String paddingAfter = newPsn.getPadding();

		assertNotSame(psn, newPsn);
		assertNotEquals(paddingBefore, paddingAfter);
		assertEquals(paddingAfter.length(), paddingLength);
		assertEquals(paddingAfter, StringUtils.repeat(" ", paddingLength));
	}
}
