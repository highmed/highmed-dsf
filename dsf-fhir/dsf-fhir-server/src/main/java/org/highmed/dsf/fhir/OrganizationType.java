package org.highmed.dsf.fhir;

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
}
