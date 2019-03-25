package org.highmed.fhir.organization;

import java.util.List;

import org.hl7.fhir.r4.model.Organization;

public interface OrganizationProvider
{
	Organization getLocalOrganization();

	List<Organization> getRemoteOrganizations();
}
