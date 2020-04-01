package org.highmed.pseudonymization.psn;

import java.util.List;

public interface PseudonymEncoder
{
	List<String> encodePseudonyms(List<Pseudonym> pseudonyms);

	List<Pseudonym> decodePseudonyms(List<String> pseudonyms);
}
