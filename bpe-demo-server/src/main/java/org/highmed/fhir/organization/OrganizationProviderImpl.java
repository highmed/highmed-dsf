package org.highmed.fhir.organization;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.highmed.fhir.client.WebserviceClientProvider;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Organization;
import org.springframework.beans.factory.InitializingBean;

public class OrganizationProviderImpl implements OrganizationProvider, InitializingBean
{
	private final WebserviceClientProvider clientProvider;
	private final String localOrganizationIdentifierCodeSystem;
	private final String localOrganizationIdentifierValue;

	public OrganizationProviderImpl(WebserviceClientProvider clientProvider, String localOrganizationIdentifierCodeSystem,
			String localOrganizationIdentifierValue)
	{
		this.clientProvider = clientProvider;
		this.localOrganizationIdentifierCodeSystem = localOrganizationIdentifierCodeSystem;
		this.localOrganizationIdentifierValue = localOrganizationIdentifierValue;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(clientProvider, "clientProvider");
		Objects.requireNonNull(localOrganizationIdentifierCodeSystem, "localOrganizationIdentifierCodeSystem");
		Objects.requireNonNull(localOrganizationIdentifierValue, "localOrganizationIdentifierValue");
	}

	@Override
	public Organization getLocalOrganization()
	{
		Bundle resultSet = clientProvider.getLocalWebserviceClient().search(Organization.class,
				Map.of("active", Collections.singletonList("true"), "identifier", Collections.singletonList(
						localOrganizationIdentifierCodeSystem + "|" + localOrganizationIdentifierValue)));

		return resultSet.getEntry().stream().map(c -> c.getResource()).filter(c -> c instanceof Organization)
				.map(c -> (Organization) c).findFirst().get();
	}

	@Override
	public List<Organization> getRemoteOrganizations()
	{
		Bundle resultSet = clientProvider.getLocalWebserviceClient().search(Organization.class,
				Map.of("active", Collections.singletonList("true")));

		return resultSet.getEntry().stream().map(c -> c.getResource()).filter(c -> c instanceof Organization)
				.map(c -> (Organization) c).filter(
						o -> !o.getIdentifier().stream()
								.anyMatch(i -> localOrganizationIdentifierCodeSystem.equals(i.getSystem())
										&& localOrganizationIdentifierValue.equals(i.getValue())))
				.collect(Collectors.toList());
	}
}
