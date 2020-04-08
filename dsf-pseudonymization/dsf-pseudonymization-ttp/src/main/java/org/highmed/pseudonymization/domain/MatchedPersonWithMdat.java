package org.highmed.pseudonymization.domain;

import java.util.List;

import org.highmed.pseudonymization.recordlinkage.MatchedPerson;

public interface MatchedPersonWithMdat extends MatchedPerson<PersonWithMdat>
{
	List<MdatContainer> getMdatContainers();
}
