package org.highmed.pseudonymization.psn;

import org.highmed.pseudonymization.domain.PseudonymizedPerson;
import org.highmed.pseudonymization.recordlinkage.MatchedPerson;
import org.highmed.pseudonymization.recordlinkage.Person;

@FunctionalInterface
public interface PseudonymizedPersonFactory<P extends Person, PP extends PseudonymizedPerson>
{
	PP create(MatchedPerson<P> person, String pseudonym);
}
