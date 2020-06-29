package org.highmed.pseudonymization.test;

import org.highmed.pseudonymization.recordlinkage.MatchingTimeTest;
import org.highmed.pseudonymization.recordlinkage.WeightDistributionTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ MatchingTimeTest.class, WeightDistributionTest.class })
public class TestSuitePerformanceTests
{
}
