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
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class OrganizationProviderImpl implements OrganizationProvider, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(OrganizationProviderImpl.class);

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

	@Override
	public List<Identifier> getRemoteIdentifiers()
	{
		return getRemoteOrganizations()
				.stream().map(o -> o.getIdentifier().stream()
						.filter(i -> organizationIdentifierCodeSystem.equals(i.getSystem())).findFirst().orElse(null))
				.filter(i -> i != null).collect(Collectors.toList());
	}

	@Override
	public Optional<Identifier> getIdentifier(IdType organizationId)
	{
		if (!organizationId.hasBaseUrl() || clientProvider.getLocalBaseUrl().equals(organizationId.getBaseUrl()))
		{
			List<Identifier> identifiers = clientProvider.getLocalWebserviceClient()
					.read(Organization.class, organizationId.getIdPart()).getIdentifier();
			return identifiers.stream().filter(i -> organizationIdentifierCodeSystem.equals(i.getSystem())).findFirst();
		}
		else
		{
			logger.warn("OrganizationId starts with baseUrl not equal to {}, but {}", clientProvider.getLocalBaseUrl(),
					organizationId.getBaseUrl());
			return Optional.empty();
		}
	}
}
