package org.highmed.dsf.fhir.authorization.process;

abstract class AbstractRecipient implements Recipient
{
	protected final String consortiumIdentifier;
	protected final String roleSystem;
	protected final String roleCode;
	protected final String organizationIdentifier;

	AbstractRecipient(String consortiumIdentifier, String roleSystem, String roleCode, String organizationIdentifier)
	{
		this.consortiumIdentifier = consortiumIdentifier;
		this.roleSystem = roleSystem;
		this.roleCode = roleCode;
		this.organizationIdentifier = organizationIdentifier;
	}
}
