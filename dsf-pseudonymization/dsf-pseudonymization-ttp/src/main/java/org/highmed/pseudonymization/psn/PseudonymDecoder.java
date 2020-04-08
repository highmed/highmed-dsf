package org.highmed.pseudonymization.psn;

import java.util.List;
import java.util.stream.Collectors;

import org.highmed.pseudonymization.domain.PseudonymizedPerson;
import org.highmed.pseudonymization.recordlinkage.MatchedPerson;
import org.highmed.pseudonymization.recordlinkage.Person;

public interface PseudonymDecoder<P extends Person>
{
	MatchedPerson<P> decodePseudonym(PseudonymizedPerson person);

	default List<MatchedPerson<P>> decodePseudonyms(List<PseudonymizedPerson> persons)
	{
		return persons.parallelStream().map(this::decodePseudonym).collect(Collectors.toList());
	}
}
