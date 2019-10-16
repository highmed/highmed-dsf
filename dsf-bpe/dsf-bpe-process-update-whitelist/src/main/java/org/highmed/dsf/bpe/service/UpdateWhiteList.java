package org.highmed.dsf.bpe.service;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.client.WebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.beans.factory.InitializingBean;

public class UpdateWhiteList implements JavaDelegate, InitializingBean
{
	private final WebserviceClientProvider clientProvider;
	private final OrganizationProvider organizationProvider;

	public UpdateWhiteList(WebserviceClientProvider clientProvider, OrganizationProvider organizationProvider)
	{
		this.clientProvider = clientProvider;
		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(clientProvider, "clientProvider");
		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception
	{
		WebserviceClient client = clientProvider.getLocalWebserviceClient();

		Bundle searchSet = client.search(Organization.class,
				Map.of("active", Collections.singletonList("true"), "identifier",
						Collections.singletonList(organizationProvider.getDefaultSystem() + "|"), "_include",
						Collections.singletonList("Organization:endpoint")));

		Bundle transaction = new Bundle().setType(BundleType.TRANSACTION);
		transaction.getIdentifier().setSystem(Constants.BUNDLE_IDENTIFIER_SYSTEM)
				.setValue(Constants.WHITE_LIST_BUNDLE_IDENTIFIER_VALUE);
		searchSet.getEntry().stream()
				.filter(e -> e.hasSearch() && SearchEntryMode.MATCH.equals(e.getSearch().getMode()) && e.hasResource()
						&& e.getResource() instanceof Organization)
				.map(e -> (Organization) e.getResource()).forEach(addWhiteListEntry(transaction, searchSet));

		client.updateConditionaly(transaction, Map.of("identifier", Collections.singletonList(
				Constants.BUNDLE_IDENTIFIER_SYSTEM + "|" + Constants.WHITE_LIST_BUNDLE_IDENTIFIER_VALUE)));
	}

	private Consumer<? super Organization> addWhiteListEntry(Bundle transaction, Bundle searchSet)
	{
		return organization ->
		{
			Identifier identifier = getDefaultIdentifier(organization).get();

			BundleEntryComponent organizationEntry = transaction.addEntry();
			String organizationId = "urn:uuid:" + UUID.randomUUID();
			organizationEntry.setFullUrl(organizationId);
			organizationEntry.getRequest().setMethod(HTTPVerb.PUT)
					.setUrl("Organization?identifier=" + identifier.getSystem() + "|" + identifier.getValue());

			organization.setIdElement(new IdType(organizationId));
			organization.getMeta().setVersionIdElement(null).setLastUpdatedElement(null);
			organizationEntry.setResource(organization);

			organization.setEndpoint(organization.getEndpoint().stream()
					.map(addWhiteListEntryReturnReference(transaction, organizationId, searchSet))
					.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()));
		};
	}

	private Function<Reference, Optional<Reference>> addWhiteListEntryReturnReference(Bundle transaction,
			String organizationId, Bundle searchSet)
	{
		return endpointRef -> getEndpoint(endpointRef, searchSet).map(endpoint ->
		{
			Identifier identifier = getDefaultIdentifier(endpoint).get();

			BundleEntryComponent endpointEntry = transaction.addEntry();
			String endpointId = "urn:uuid:" + UUID.randomUUID();
			endpointEntry.setFullUrl(endpointId);
			endpointEntry.getRequest().setMethod(HTTPVerb.PUT)
					.setUrl("Endpoint?identifier=" + identifier.getSystem() + "|" + identifier.getValue());

			endpoint.setIdElement(new IdType(endpointId));
			endpoint.getMeta().setVersionIdElement(null).setLastUpdatedElement(null);
			endpoint.setManagingOrganization(new Reference().setReference(organizationId).setType("Organization"));
			endpointEntry.setResource(endpoint);

			return new Reference().setReference(endpointId).setType("Endpoint");
		});
	}

	private Optional<Identifier> getDefaultIdentifier(Organization org)
	{
		return org.getIdentifier().stream().filter(i -> organizationProvider.getDefaultSystem().equals(i.getSystem()))
				.findFirst();
	}

	private Optional<Identifier> getDefaultIdentifier(Endpoint ept)
	{
		return ept.getIdentifier().stream().filter(i -> Constants.ENDPOINT_IDENTIFIER_SYSTEM.equals(i.getSystem()))
				.findFirst();
	}

	private Optional<Endpoint> getEndpoint(Reference endpoint, Bundle searchSet)
	{
		return searchSet.getEntry().stream()
				.filter(e -> e.hasResource() && e.getResource() instanceof Endpoint
						&& e.getFullUrl().endsWith(endpoint.getReference()))
				.map(e -> (Endpoint) e.getResource()).findFirst();
	}
}
