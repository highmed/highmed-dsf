package org.highmed.dsf.fhir.organization;

import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Identifier;

public interface EndpointProvider
{
	Endpoint getLocalEndpoint();

	default String getLocalEndpointAddress()
	{
		return getLocalEndpoint().getAddress();
	}

	default Identifier getLocalEndpointIdentifier()
	{
		return getLocalEndpoint().getIdentifier().stream()
				.filter(i -> NAMINGSYSTEM_HIGHMED_ENDPOINT_IDENTIFIER.equals(i.getSystem())).findFirst().get();
	}

	Map<String, Endpoint> getDefaultEndpointsByOrganizationIdentifier();

	default Map<String, String> getDefaultEndpointAdressesByOrganizationIdentifier()
	{
		return getDefaultEndpointsByOrganizationIdentifier().entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getAddress()));
	}

	Optional<Endpoint> getFirstDefaultEndpoint(String organizationIdentifierValue);

	default Optional<String> getFirstDefaultEndpointAddress(String organizationIdentifierValue)
	{
		return getFirstDefaultEndpoint(organizationIdentifierValue).map(Endpoint::getAddress);
	}

	Map<String, Endpoint> getConsortiumEndpointsByOrganizationIdentifier(String consortiumIdentifierValue);

	default Map<String, String> getConsortiumEndpointAdressesByOrganizationIdentifier(String consortiumIdentifierValue)
	{
		return getConsortiumEndpointsByOrganizationIdentifier(consortiumIdentifierValue).entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getAddress()));
	}

	Map<String, Endpoint> getConsortiumEndpointsByOrganizationIdentifier(String consortiumIdentifierValue,
			String roleSystem, String roleCode);

	default Map<String, String> getConsortiumEndpointAdressesByOrganizationIdentifier(String consortiumIdentifierValue,
			String roleSystem, String roleCode)
	{
		return getConsortiumEndpointsByOrganizationIdentifier(consortiumIdentifierValue, roleSystem, roleCode)
				.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getAddress()));
	}

	Optional<Endpoint> getFirstConsortiumEndpoint(String consortiumIdentifierValue, String roleSystem, String roleCode,
			String organizationIdentifierValue);

	default Optional<String> getFirstConsortiumEndpointAdress(String consortiumIdentifierValue, String roleSystem,
			String roleCode, String organizationIdentifierValue)
	{
		return getFirstConsortiumEndpoint(consortiumIdentifierValue, roleSystem, roleCode, organizationIdentifierValue)
				.map(Endpoint::getAddress);
	}

	Optional<Endpoint> getEndpoint(String endpointIdentifierValue);

	default Optional<String> getEndpointAddress(String endpointIdentifierValue)
	{
		return getEndpoint(endpointIdentifierValue).map(Endpoint::getAddress);
	}
}
