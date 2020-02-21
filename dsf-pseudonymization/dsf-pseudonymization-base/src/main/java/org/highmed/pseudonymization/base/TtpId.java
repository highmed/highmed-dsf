package org.highmed.pseudonymization.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Contains a subject's encrypted local ID along with that
 * ID's origin institution
 */
public class TtpId
{

	private String origin, idString;

	/**
	 * @param origin   The location where this ID hails from
	 * @param idString Encrypted string of the local MPI ID
	 */
	@JsonCreator
	public TtpId(
			@JsonProperty("origin")
					String origin,
			@JsonProperty("idString")
					String idString)
	{
		this.origin = origin;
		this.idString = idString;
	}

	public String getOrigin()
	{
		return origin;
	}

	public String getIdString()
	{
		return idString;
	}

	/**
	 * @return ID String formatted as (e.g.): "UMG": "1234H"
	 */
	@Override
	public String toString()
	{
		return "\"" + origin + "\": \"" + idString + "\"";
	}

	/**
	 * Parse a new TtpID from a decoded pseudonym
	 *
	 * @param input String of the format (including quotes): "origin": "localID"
	 * @return TtpID Object
	 */
	public static TtpId fromString(String input)
	{
		String[] substrings = input.split(": ");
		String origin = substrings[0];
		origin = origin.substring(1, origin.length() - 1); // Strip the string of quotation marks
		String idString = substrings[1];
		idString = idString.substring(1, idString.length() - 1);

		return new TtpId(origin, idString);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof TtpId))
			return false;
		TtpId ttpID = (TtpId) o;
		return origin.equals(ttpID.origin) && idString.equals(ttpID.idString);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(origin, idString);
	}
}