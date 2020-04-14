package org.highmed.pseudonymization.recordlinkage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class FederatedMatcherTest
{
	@Test
	public void testMatch() throws Exception
	{
		FederatedMatcher<TestPerson> m = new FederatedMatcherImpl<TestPerson>(TestMatchedPerson::new);

		List<List<TestPerson>> personLists = Arrays.asList(
				Arrays.asList(new TestPerson(new TestMedicId("org1", "id1"),
						createRecordBloomFilter(200, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9))),
				Arrays.asList(new TestPerson(new TestMedicId("org2", "id2"),
						createRecordBloomFilter(200, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9))));

		Set<MatchedPerson<TestPerson>> matchedPersons = m.matchPersons(personLists);
		assertNotNull(matchedPersons);
		assertEquals(1, matchedPersons.size());
	}

	private BitSet createRecordBloomFilter(int length, int... setBitatPositions)
	{
		BitSet b = new BitSet(length);
		for (int i : setBitatPositions)
			b.set(i);
		return b;
	}
}
