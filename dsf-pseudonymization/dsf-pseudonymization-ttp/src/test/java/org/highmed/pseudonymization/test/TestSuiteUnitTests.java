package org.highmed.pseudonymization.test;

import org.highmed.pseudonymization.domain.MatchedPersonImplJsonTest;
import org.highmed.pseudonymization.domain.PersonImplJsonTest;
import org.highmed.pseudonymization.domain.PseudonymizedPersonImplJsonTest;
import org.highmed.pseudonymization.psn.PseudonyWithPaddingJsonTest;
import org.highmed.pseudonymization.psn.PseudonymGeneratorImplTest;
import org.highmed.pseudonymization.recordlinkage.FederatedMatcherTest;
import org.highmed.pseudonymization.recordlinkage.SingleOrganizationMatcherTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ MatchedPersonImplJsonTest.class, PersonImplJsonTest.class, PseudonymizedPersonImplJsonTest.class,
		PseudonymGeneratorImplTest.class, PseudonyWithPaddingJsonTest.class, FederatedMatcherTest.class,
		SingleOrganizationMatcherTest.class })
public class TestSuiteUnitTests
{
}
