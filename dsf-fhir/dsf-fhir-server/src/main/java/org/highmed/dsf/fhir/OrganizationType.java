package org.highmed.dsf.fhir;

import java.util.Optional;

public enum OrganizationType
{
	MeDIC("MeDIC"), TTP("TTP");

	private final String code;

	OrganizationType(String code)
	{
		this.code = code;
	}

	public String getCode()
	{
		return code;
	}

	public String getSystem()
	{
		return "http://highmed.org/fhir/CodeSystem/organization-type";
	}

	public static Optional<OrganizationType> fromString(String type)
	{
		switch (type)
		{
			case "MEDIC":
			case "MeDIC":
			case "medic":
				return Optional.of(MeDIC);

			case "TTP":
			case "ttp":
				return Optional.of(TTP);

			default:
				return Optional.empty();
		}
	}
}
