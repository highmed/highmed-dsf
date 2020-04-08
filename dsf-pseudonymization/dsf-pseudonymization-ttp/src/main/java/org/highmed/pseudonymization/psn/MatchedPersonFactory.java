package org.highmed.pseudonymization.psn;

import java.util.List;

import org.highmed.pseudonymization.domain.PseudonymizedPerson;
import org.highmed.pseudonymization.recordlinkage.MatchedPerson;
import org.highmed.pseudonymization.recordlinkage.MedicId;
import org.highmed.pseudonymization.recordlinkage.Person;

@FunctionalInterface
public interface MatchedPersonFactory<P extends Person>
{
	MatchedPerson<P> create(PseudonymizedPerson person, List<MedicId> medicIds);
}
