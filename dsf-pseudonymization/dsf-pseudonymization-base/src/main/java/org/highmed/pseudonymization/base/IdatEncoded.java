package org.highmed.pseudonymization.base;

import java.util.BitSet;

/**
 * Bloom-Filter encoded version of a subject's IDAT
 * Containing a {@link TtpId} with their encrypted Local ID and Origin
 * as well as a Record-Level Bloom Filter of their IDAT for linkage.
 */
public interface IdatEncoded
{

	TtpId getEncodedID();

	BitSet getRBF();
}
