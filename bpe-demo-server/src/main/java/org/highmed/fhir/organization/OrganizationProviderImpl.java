package org.highmed.fhir.organization;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.highmed.fhir.client.WebserviceClientProvider;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Organization;
import org.springframework.beans.factory.InitializingBean;

public class OrganizationProviderImpl implements OrganizationProvider, InitializingBean
{
	private final WebserviceClientProvider clientProvider;
	private final String organizationIdentifierCodeSystem;
	private final String organizationIdentifierLocalValue;

	public OrganizationProviderImpl(WebserviceClientProvider clientProvider, String organizationIdentifierCodeSystem,
			String organizationIdentifierLocalValue)
	{
		this.clientProvider = clientProvider;
		this.organizationIdentifierCodeSystem = organizationIdentifierCodeSystem;
		this.organizationIdentifierLocalValue = organizationIdentifierLocalValue;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(clientProvider, "clientProvider");
		Objects.requireNonNull(organizationIdentifierCodeSystem, "organizationIdentifierCodeSystem");
		Objects.requireNonNull(organizationIdentifierLocalValue, "organizationIdentifierLocalValue");
	}

	@Override
	public String getDefaultSystem()
	{
		return organizationIdentifierCodeSystem;
	}

	@Override
	public String getLocalIdentifier()
	{
		return organizationIdentifierLocalValue;
	}

	private Stream<Organization> searchForOrganizations(String system, String identifier)
	{
		Bundle resultSet = clientProvider.getLocalWebserviceClient().search(Organization.class, Map.of("active",
				Collections.singletonList("true"), "identifier", Collections.singletonList(system + "|" + identifier)));

		return resultSet.getEntry().stream().map(c -> c.getResource()).filter(c -> c instanceof Organization)
				.map(c -> (Organization) c);
	}

	@Override
	public Optional<Organization> getOrganization(String system, String identifier)
	{
		return searchForOrganizations(system, identifier).findFirst();
	}

	@Override
	public Optional<Organization> getOrganization(String identifier)
	{
		Stream<Organization> organizations = searchForOrganizations(organizationIdentifierCodeSystem, identifier);

		return organizations.findFirst();
	}

	@Override
	public Organization getLocalOrganization()
	{
		return getOrganization(organizationIdentifierLocalValue).get();
	}

	@Override
	public List<Organization> getRemoteOrganizations()
	{
		Stream<Organization> organizations = searchForOrganizations(organizationIdentifierCodeSystem, "");

		return organizations.filter(
				o -> !o.getIdentifier().stream().anyMatch(i -> organizationIdentifierCodeSystem.equals(i.getSystem())
						&& organizationIdentifierLocalValue.equals(i.getValue())))
				.collect(Collectors.toList());
	}
}
