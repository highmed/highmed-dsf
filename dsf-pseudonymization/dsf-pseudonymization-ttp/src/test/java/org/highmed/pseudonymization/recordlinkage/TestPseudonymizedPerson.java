package org.highmed.pseudonymization.recordlinkage;

import org.highmed.pseudonymization.domain.PseudonymizedPerson;

public class TestPseudonymizedPerson implements PseudonymizedPerson
{
	private final String pseudonym;

	public TestPseudonymizedPerson(String pseudonym)
	{
		this.pseudonym = pseudonym;
	}

	@Override
	public String getPseudonym()
	{
		return pseudonym;
	}

	@Override
	public String toString()
	{
		return "pseudonym: " + pseudonym;
	}
}
