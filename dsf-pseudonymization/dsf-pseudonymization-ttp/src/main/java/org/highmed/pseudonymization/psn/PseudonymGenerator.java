package org.highmed.pseudonymization.psn;

import java.util.Collection;
import java.util.List;

import org.highmed.pseudonymization.domain.PseudonymizedPerson;
import org.highmed.pseudonymization.recordlinkage.MatchedPerson;
import org.highmed.pseudonymization.recordlinkage.Person;

public interface PseudonymGenerator<P extends Person, R extends PseudonymizedPerson>
{
	List<R> createPseudonymsAndShuffle(Collection<? extends MatchedPerson<P>> persons);
}
