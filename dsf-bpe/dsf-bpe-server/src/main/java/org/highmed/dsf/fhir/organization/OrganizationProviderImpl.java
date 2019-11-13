package org.highmed.dsf.fhir.organization;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
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

	private final FhirWebserviceClientProvider clientProvider;
	private final String organizationIdentifierLocalValue;
	private final Identifier localIdentifier;

	public OrganizationProviderImpl(FhirWebserviceClientProvider clientProvider, String organizationIdentifierLocalValue)
	{
		this.clientProvider = clientProvider;
		this.organizationIdentifierLocalValue = organizationIdentifierLocalValue;

		localIdentifier = new Identifier().setSystem(getDefaultSystem()).setValue(organizationIdentifierLocalValue);
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(clientProvider, "clientProvider");
		Objects.requireNonNull(organizationIdentifierLocalValue, "organizationIdentifierLocalValue");
	}

	@Override
	public String getDefaultSystem()
	{
		return Constants.ORGANIZATION_IDENTIFIER_SYSTEM;
	}

	@Override
	public String getLocalIdentifierValue()
	{
		return organizationIdentifierLocalValue;
	}

	@Override
	public Identifier getLocalIdentifier()
	{
		return localIdentifier;
	}

	private Stream<Organization> searchForOrganizations(String system, String identifier)
	{
		return searchForOrganizations(system + "|" + identifier);
	}

	private Stream<Organization> searchForOrganizations(String identifierValue)
	{
		Bundle resultSet = clientProvider.getLocalWebserviceClient().search(Organization.class, Map.of("active",
				Collections.singletonList("true"), "identifier", Collections.singletonList(identifierValue)));

		return resultSet.getEntry().stream().map(bundleEntry -> bundleEntry.getResource())
				.filter(resource -> resource instanceof Organization).map(organization -> (Organization) organization);
	}

	@Override
	public Optional<Organization> getOrganization(String identifier)
	{
		return getOrganization(getDefaultSystem(), identifier);
	}

	@Override
	public Optional<Organization> getOrganization(String system, String identifier)
	{
		return searchForOrganizations(system, identifier).findFirst();
	}

	@Override
	public Organization getLocalOrganization()
	{
		return getOrganization(organizationIdentifierLocalValue).get();
	}

	@Override
	public List<Organization> getRemoteOrganizations()
	{
		return searchForOrganizations(getDefaultSystem(), "")
				.filter(noIdentifierMatches(getDefaultSystem(), organizationIdentifierLocalValue))
				.collect(Collectors.toList());
	}

	@Override
	public List<Identifier> getRemoteIdentifiers()
	{
		return getRemoteOrganizations().stream()
				.map(organization -> organization.getIdentifier().stream()
						.filter(identifierWithSystem(getDefaultSystem())).findFirst().orElse(null))
				.filter(identifier -> identifier != null).collect(Collectors.toList());
	}

	@Override
	public Optional<Identifier> getIdentifier(IdType organizationId)
	{
		if (!organizationId.hasBaseUrl() || clientProvider.getLocalBaseUrl().equals(organizationId.getBaseUrl()))
		{
			List<Identifier> identifiers = clientProvider.getLocalWebserviceClient()
					.read(Organization.class, organizationId.getIdPart()).getIdentifier();
			return identifiers.stream().filter(identifierWithSystem(getDefaultSystem())).findFirst();
		}
		else
		{
			logger.warn("OrganizationId starts with baseUrl not equal to {}, but {}", clientProvider.getLocalBaseUrl(),
					organizationId.getBaseUrl());
			return Optional.empty();
		}
	}

	@Override
	public Stream<Organization> searchRemoteOrganizations(String searchParameterIdentifierValue)
	{
		return searchForOrganizations(searchParameterIdentifierValue)
				.filter(noIdentifierMatches(getDefaultSystem(), organizationIdentifierLocalValue));
	}

	@Override
	public Stream<Identifier> searchRemoteOrganizationsIdentifiers(String searchParameterIdentifierValue)
	{
		return searchRemoteOrganizations(searchParameterIdentifierValue)
				.map(organization -> organization.getIdentifier().stream()
						.filter(identifierWithSystem(getDefaultSystem())).findFirst().orElse(null))
				.filter(identifier -> identifier != null);
	}

	private static Predicate<? super Identifier> identifierWithSystem(String system)
	{
		return identifier -> system.equals(identifier.getSystem());
	}

	private static Predicate<? super Organization> noIdentifierMatches(String system, String value)
	{
		return organization -> !organization.getIdentifier().stream()
				.anyMatch(i -> system.equals(i.getSystem()) && value.equals(i.getValue()));
	}
}
