package org.highmed.dsf.fhir.authentication;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
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
	private static final String THUMBPRINT_PATTERN_STRING = "[a-f0-9]{128}";
	private static final Pattern THUMBPRINT_PATTERN = Pattern.compile(THUMBPRINT_PATTERN_STRING);

	private static final Logger logger = LoggerFactory.getLogger(OrganizationProviderWithDbBackend.class);

	private final OrganizationDao dao;
	private final ExceptionHandler exceptionHandler;
	private final List<String> localUserThumbprints = new ArrayList<String>();
	private final List<String> localPermanentDeleteUserThumbprints = new ArrayList<String>();
	private final String localIdentifierValue;

	public OrganizationProviderWithDbBackend(OrganizationDao dao, ExceptionHandler exceptionHandler,
			List<String> localUserThumbprints, List<String> localPermanentDeleteUserThumbprints, String localIdentifier)
	{
		this.dao = dao;
		this.exceptionHandler = exceptionHandler;

		if (localUserThumbprints != null)
			localUserThumbprints.stream().map(String::toLowerCase).map(s -> s.replace(":", ""))
					.forEach(this.localUserThumbprints::add);
		if (localPermanentDeleteUserThumbprints != null)
			localPermanentDeleteUserThumbprints.stream().map(String::toLowerCase).map(s -> s.replace(":", ""))
					.forEach(this.localPermanentDeleteUserThumbprints::add);

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

		if (!localUserThumbprints.containsAll(localPermanentDeleteUserThumbprints))
			logger.warn("At least one local permanent delete user thumbprint not part of local user thumbprints!");

		List<String> notMatchingLocalUserThumbprints = localUserThumbprints.stream()
				.filter(s -> !THUMBPRINT_PATTERN.matcher(s).matches()).collect(Collectors.toList());
		if (!notMatchingLocalUserThumbprints.isEmpty())
		{
			logger.warn("Local user thumbprints contains entr{} not matching {}: {}",
					notMatchingLocalUserThumbprints.size() == 1 ? "y" : "ies", THUMBPRINT_PATTERN_STRING,
					notMatchingLocalUserThumbprints);
		}

		List<String> notMatchingLocalPermanentDeleteUserThumbprints = localPermanentDeleteUserThumbprints.stream()
				.filter(s -> !THUMBPRINT_PATTERN.matcher(s).matches()).collect(Collectors.toList());
		if (!notMatchingLocalPermanentDeleteUserThumbprints.isEmpty())
		{
			logger.warn("Local permanent delete user thumbprints contains entr{} not matching {}: {}",
					notMatchingLocalPermanentDeleteUserThumbprints.size() == 1 ? "y" : "ies", THUMBPRINT_PATTERN_STRING,
					notMatchingLocalPermanentDeleteUserThumbprints);
		}
	}

	@Override
	public Optional<User> getOrganization(X509Certificate certificate)
	{
		if (certificate == null)
			return Optional.empty();

		String loginThumbprintHex = Hex.encodeHexString(getThumbprint(certificate));
		String subjectDn = certificate.getSubjectX500Principal().getName(X500Principal.RFC1779);

		logger.debug("Reading user-role and deleteAllowed status of '{}', thumbprint '{}' (SHA-512)", subjectDn,
				loginThumbprintHex);
		UserRole userRole = localUserThumbprints.contains(loginThumbprintHex.toLowerCase()) ? UserRole.LOCAL
				: UserRole.REMOTE;
		boolean deleteAllowed = localPermanentDeleteUserThumbprints.contains(loginThumbprintHex.toLowerCase());

		switch (userRole)
		{
			case LOCAL:
				return getLocalOrganization().map(User.local(deleteAllowed, subjectDn));
			case REMOTE:
				return getOrganization(loginThumbprintHex).map(User.remote(subjectDn));
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
