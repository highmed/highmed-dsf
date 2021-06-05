package org.highmed.dsf.fhir.authentication;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.codec.binary.Hex;
import org.highmed.dsf.fhir.dao.OrganizationDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.hl7.fhir.r4.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class OrganizationProviderWithDbBackend implements OrganizationProvider, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(OrganizationProviderWithDbBackend.class);

	private final OrganizationDao dao;
	private final ExceptionHandler exceptionHandler;
	private final List<String> localUserThumbprints = new ArrayList<String>();
	private final String localIdentifierValue;

	public OrganizationProviderWithDbBackend(OrganizationDao dao, ExceptionHandler exceptionHandler,
			List<String> localUserThumbprints, String localIdentifier)
	{
		this.dao = dao;
		this.exceptionHandler = exceptionHandler;

		if (localUserThumbprints != null)
			localUserThumbprints.stream().map(t -> t.toLowerCase()).forEach(this.localUserThumbprints::add);
		this.localIdentifierValue = localIdentifier;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(dao, "dao");
		Objects.requireNonNull(exceptionHandler, "exceptionHandler");

		if (localUserThumbprints.isEmpty())
			logger.warn("No local users configured");
		else
			logger.info("{} local user{} configured with tumbprint{}: {}", localUserThumbprints.size(),
					localUserThumbprints.size() != 1 ? "s" : "", localUserThumbprints.size() != 1 ? "s" : "",
					localUserThumbprints.stream().collect(Collectors.joining(", ", "[", "]")));

		Objects.requireNonNull(localIdentifierValue, "localIdentifierValue");
		if (getLocalOrganization().isEmpty())
			logger.warn("Local organization not found by identifier: {}", localIdentifierValue);
	}

	@Override
	public Optional<User> getOrganization(X509Certificate certificate)
	{
		if (certificate == null)
			return Optional.empty();

		String loginThumbprintHex = Hex.encodeHexString(getThumbprint(certificate));
		String subjectDn = certificate.getSubjectX500Principal().getName(X500Principal.RFC1779);
		logger.debug("Reading user-role of '{}', thumbprint '{}' (SHA-512)", subjectDn, loginThumbprintHex);

		UserRole userRole = localUserThumbprints.contains(loginThumbprintHex.toLowerCase()) ? UserRole.LOCAL
				: UserRole.REMOTE;

		switch (userRole)
		{
			case LOCAL:
				return getLocalOrganization().map(org -> new User(org, userRole, subjectDn));
			case REMOTE:
				return getOrganization(loginThumbprintHex).map(org -> new User(org, userRole, subjectDn));
			default:
				logger.warn("UserRole {} not supported", userRole);
				return Optional.empty();
		}
	}

	private Optional<Organization> getOrganization(String loginThumbprintHex)
	{
		return exceptionHandler.catchAndLogSqlExceptionAndIfReturn(
				() -> dao.readActiveNotDeletedByThumbprint(loginThumbprintHex), Optional::empty);
	}

	@Override
	public Optional<Organization> getLocalOrganization()
	{
		return exceptionHandler.catchAndLogSqlExceptionAndIfReturn(
				() -> dao.readActiveNotDeletedByIdentifier(localIdentifierValue), Optional::empty);
	}

	private byte[] getThumbprint(X509Certificate certificate)
	{
		try
		{
			return MessageDigest.getInstance("SHA-512").digest(certificate.getEncoded());
		}
		catch (CertificateEncodingException | NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}
}
