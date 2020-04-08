package org.highmed.pseudonymization.domain;

import org.highmed.pseudonymization.recordlinkage.Person;

public interface PersonWithMdat extends Person
{
	MdatContainer getMdatContainer();
}
