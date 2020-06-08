package org.highmed.pseudonymization.recordlinkage;

@FunctionalInterface
public interface MatchedPersonFactory<P extends Person>
{
	MatchedPerson<P> create(P person);
}
