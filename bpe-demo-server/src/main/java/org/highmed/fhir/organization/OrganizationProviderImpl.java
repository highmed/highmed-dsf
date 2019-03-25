package org.highmed.fhir.organization;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.highmed.fhir.client.ClientProvider;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Organization;

public class OrganizationProviderImpl implements OrganizationProvider
{
	private final ClientProvider clientProvider;
	private final String localOrganizationIdPart;

	public OrganizationProviderImpl(ClientProvider clientProvider, String localOrganizationIdPart)
	{
		this.clientProvider = clientProvider;
		this.localOrganizationIdPart = localOrganizationIdPart;
	}

	@Override
	public Organization getLocalOrganization()
	{
		return clientProvider.getLocalClient().read(Organization.class, localOrganizationIdPart);
	}

	@Override
	public List<Organization> getRemoteOrganizations()
	{
		Bundle resultSet = clientProvider.getLocalClient().search(Organization.class, Collections.emptyMap());

		return resultSet.getEntry().stream().map(c -> c.getResource()).filter(c -> c instanceof Organization)
				.map(c -> (Organization) c).collect(Collectors.toList());
	}
}
