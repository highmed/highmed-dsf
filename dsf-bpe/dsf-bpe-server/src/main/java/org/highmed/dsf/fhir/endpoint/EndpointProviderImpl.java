package org.highmed.dsf.fhir.endpoint;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.EndpointProvider;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Endpoint.EndpointStatus;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.springframework.beans.factory.InitializingBean;

public class EndpointProviderImpl implements EndpointProvider, InitializingBean
{
	private final FhirWebserviceClientProvider fhirWebserviceClientProvider;
	private final String organizationIdentifierLocalValue;

	public EndpointProviderImpl(FhirWebserviceClientProvider fhirWebserviceClientProvider,
			String organizationIdentifierLocalValue)
	{
		this.fhirWebserviceClientProvider = fhirWebserviceClientProvider;
		this.organizationIdentifierLocalValue = organizationIdentifierLocalValue;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(fhirWebserviceClientProvider, "fhirWebserviceClientProvider");
		Objects.requireNonNull(organizationIdentifierLocalValue, "organizationIdentifierLocalValue");
	}

	private FhirWebserviceClient getLocalWebserviceClient()
	{
		return fhirWebserviceClientProvider.getLocalWebserviceClient();
	}

	@Override
	public Endpoint getLocalEndpoint()
	{
		return getFirstDefaultEndpoint(organizationIdentifierLocalValue).get();
	}

	@Override
	public Map<String, Endpoint> getDefaultEndpointsByOrganizationIdentifier()
	{
		Bundle b = getLocalWebserviceClient().searchWithStrictHandling(Organization.class, Map.of("active",
				Collections.singletonList("true"), "_include", Collections.singletonList("Organization:endpoint")));

		return toEndpointsByOrganizationIdentifier(b);
	}

	private Map<String, Endpoint> toEndpointsByOrganizationIdentifier(Bundle b)
	{
		Map<String, String> orgsById = b.getEntry().stream().filter(BundleEntryComponent::hasResource)
				.map(BundleEntryComponent::getResource).filter(r -> r instanceof Organization)
				.map(r -> (Organization) r).filter(Organization::getActive)
				.collect(Collectors.toMap(o -> o.getIdElement().toUnqualifiedVersionless().toString(), o -> o
						.getIdentifier().stream()
						.filter(i -> ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER.equals(i.getSystem()))
						.filter(Identifier::hasValue).map(Identifier::getValue).findFirst().get()));

		return b.getEntry().stream().filter(BundleEntryComponent::hasResource).map(BundleEntryComponent::getResource)
				.filter(r -> r instanceof Endpoint).map(r -> (Endpoint) r)
				.filter(e -> EndpointStatus.ACTIVE.equals(e.getStatus()))
				.filter(e -> orgsById.containsKey(
						e.getManagingOrganization().getReferenceElement().toUnqualifiedVersionless().toString()))
				.collect(Collectors.toMap(e -> orgsById
						.get(e.getManagingOrganization().getReferenceElement().toUnqualifiedVersionless().toString()),
						Function.identity(), (e1, e2) -> e1));
	}

	@Override
	public Optional<Endpoint> getFirstDefaultEndpoint(String organizationIdentifierValue)
	{
		Bundle b = getLocalWebserviceClient().searchWithStrictHandling(Organization.class,
				Map.of("active", Collections.singletonList("true"), "identifier",
						Collections.singletonList(ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER + "|"
								+ organizationIdentifierValue),
						"_include", Collections.singletonList("Organization:endpoint")));

		return b.getEntry().stream().filter(BundleEntryComponent::hasResource).map(BundleEntryComponent::getResource)
				.filter(r -> r instanceof Endpoint).map(r -> (Endpoint) r)
				.filter(e -> EndpointStatus.ACTIVE.equals(e.getStatus())).findFirst();
	}

	@Override
	public Map<String, Endpoint> getConsortiumEndpointsByOrganizationIdentifier(String consortiumIdentifierValue)
	{
		Bundle b = getLocalWebserviceClient().searchWithStrictHandling(OrganizationAffiliation.class,
				Map.of("active", Collections.singletonList("true"), "primary-organization:identifier",
						Collections.singletonList(ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER + "|"
								+ consortiumIdentifierValue),
						"_include", Arrays.asList("OrganizationAffiliation:endpoint",
								"OrganizationAffiliation:participating-organization")));

		return toEndpointsByOrganizationIdentifier(b);
	}

	@Override
	public Map<String, Endpoint> getConsortiumEndpointsByOrganizationIdentifier(String consortiumIdentifierValue,
			String roleSystem, String roleCode)
	{
		Bundle b = getLocalWebserviceClient().searchWithStrictHandling(OrganizationAffiliation.class,
				Map.of("active", Collections.singletonList("true"), "primary-organization:identifier",
						Collections.singletonList(ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER + "|"
								+ consortiumIdentifierValue),
						"role", Collections.singletonList(roleSystem + "|" + roleCode), "_include",
						Arrays.asList("OrganizationAffiliation:endpoint",
								"OrganizationAffiliation:participating-organization")));

		return toEndpointsByOrganizationIdentifier(b);
	}

	@Override
	public Optional<Endpoint> getFirstConsortiumEndpoint(String consortiumIdentifierValue, String roleSystem,
			String roleCode, String organizationIdentifierValue)
	{
		Bundle b = getLocalWebserviceClient().searchWithStrictHandling(OrganizationAffiliation.class, Map.of("active",
				Collections.singletonList("true"), "primary-organization:identifier",
				Collections.singletonList(
						ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER + "|" + consortiumIdentifierValue),
				"participating-organization:identifier",
				Collections.singletonList(
						ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER + "|" + organizationIdentifierValue),
				"role", Collections.singletonList(roleSystem + "|" + roleCode), "_include",
				Collections.singletonList("OrganizationAffiliation:endpoint")));

		return b.getEntry().stream().filter(BundleEntryComponent::hasResource).map(BundleEntryComponent::getResource)
				.filter(r -> r instanceof Endpoint).map(r -> (Endpoint) r)
				.filter(e -> EndpointStatus.ACTIVE.equals(e.getStatus())).findFirst();
	}

	@Override
	public Optional<Endpoint> getEndpoint(String endpointIdentifierValue)
	{
		Bundle resultSet = getLocalWebserviceClient().searchWithStrictHandling(Endpoint.class,
				Map.of("status", Collections.singletonList("active"), "identifier", Collections.singletonList(
						ConstantsBase.NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER + "|" + endpointIdentifierValue)));

		return resultSet.getEntry().stream().map(bundleEntry -> bundleEntry.getResource())
				.filter(resource -> resource instanceof Endpoint).map(endpoint -> (Endpoint) endpoint).findFirst();
	}
}
