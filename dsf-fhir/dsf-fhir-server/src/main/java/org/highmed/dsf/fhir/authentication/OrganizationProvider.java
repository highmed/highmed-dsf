package org.highmed.dsf.fhir.authentication;

import java.security.cert.X509Certificate;
import java.util.Optional;

import org.hl7.fhir.r4.model.Organization;

public interface OrganizationProvider
{
	/**
	 * @param certificate
	 *            may be <code>null</code>
	 * @return {@link Optional#empty()} if no {@link Organization} is found, or the given {@link X509Certificate} is
	 *         <code>null</code>
	 */
	Optional<User> getOrganization(X509Certificate certificate);

	Optional<Organization> getLocalOrganization();
}
