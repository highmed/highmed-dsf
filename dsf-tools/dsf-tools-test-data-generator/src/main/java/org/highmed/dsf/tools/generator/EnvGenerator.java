package org.highmed.dsf.tools.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.highmed.dsf.tools.generator.CertificateGenerator.CertificateFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvGenerator
{
	private static final Logger logger = LoggerFactory.getLogger(EnvGenerator.class);

	private static final String BUNDLE_USER_THUMBPRINT = "BUNDLE_USER_THUMBPRINT";
	private static final String CONF_USER_THUMBPRINTS = "CONF_USER_THUMBPRINTS";
	private static final String CONF_USER_THUMBPRINTS_PERMANENTDELETE = "CONF_USER_THUMBPRINTS_PERMANENT_DELETE";

	private static final class EnvEntry
	{
		final String userThumbprintVariableName;
		final String userThumbprint;
		final String userThumbprintsVariableName;
		final Stream<String> userThumbprints;
		final String userThumbprintsPermanentDeleteVariableName;
		final Stream<String> userThumbprintsPermanentDelete;

		EnvEntry(String userThumbprintVariableName, String userThumbprint, String userThumbprintsVariableName,
				Stream<String> userThumbprints, String userThumbprintsPermanentDeleteVariableName,
				Stream<String> userThumbprintsPermanentDelete)
		{
			this.userThumbprintVariableName = userThumbprintVariableName;
			this.userThumbprint = userThumbprint;
			this.userThumbprintsVariableName = userThumbprintsVariableName;
			this.userThumbprints = userThumbprints;
			this.userThumbprintsPermanentDeleteVariableName = userThumbprintsPermanentDeleteVariableName;
			this.userThumbprintsPermanentDelete = userThumbprintsPermanentDelete;
		}
	}

	public void generateAndWriteDockerTestFhirEnvFile(Map<String, CertificateFiles> clientCertificateFilesByCommonName)
	{
		String bundleUserThumbprint = filterAndMapToThumbprint(clientCertificateFilesByCommonName, "test-client")
				.findFirst().get();
		Stream<String> confUserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName, "test-client",
				"Webbrowser Test User");
		Stream<String> confUserThumbprintsPermanentDelete = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"test-client", "Webbrowser Test User");

		writeEnvFile(Paths.get("../../dsf-docker-test-setup/fhir/.env"), bundleUserThumbprint, confUserThumbprints,
				confUserThumbprintsPermanentDelete);
	}

	public void generateAndWriteDockerTest3MedicTtpFhirEnvFiles(
			Map<String, CertificateFiles> clientCertificateFilesByCommonName)
	{
		String bundleMedic1UserThumbprint = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic1-client").findFirst().get();
		Stream<String> confMedic1UserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic1-client", "Webbrowser Test User");
		Stream<String> confMedic1UserThumbprintsPermanentDelete = filterAndMapToThumbprint(
				clientCertificateFilesByCommonName, "medic1-client", "Webbrowser Test User");

		writeEnvFile(Paths.get("../../dsf-docker-test-setup-3medic-ttp/medic1/fhir/.env"), bundleMedic1UserThumbprint,
				confMedic1UserThumbprints, confMedic1UserThumbprintsPermanentDelete);

		String bundleMedic2UserThumbprint = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic2-client").findFirst().get();
		Stream<String> confMedic2UserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic2-client", "Webbrowser Test User");
		Stream<String> confMedic2UserThumbprintsPermanentDelete = filterAndMapToThumbprint(
				clientCertificateFilesByCommonName, "medic2-client", "Webbrowser Test User");

		writeEnvFile(Paths.get("../../dsf-docker-test-setup-3medic-ttp/medic2/fhir/.env"), bundleMedic2UserThumbprint,
				confMedic2UserThumbprints, confMedic2UserThumbprintsPermanentDelete);

		String bundleMedic3UserThumbprint = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic3-client").findFirst().get();
		Stream<String> confMedic3UserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic3-client", "Webbrowser Test User");
		Stream<String> confMedic3UserThumbprintsPermanentDelete = filterAndMapToThumbprint(
				clientCertificateFilesByCommonName, "medic3-client", "Webbrowser Test User");

		writeEnvFile(Paths.get("../../dsf-docker-test-setup-3medic-ttp/medic3/fhir/.env"), bundleMedic3UserThumbprint,
				confMedic3UserThumbprints, confMedic3UserThumbprintsPermanentDelete);

		String bundleTtpUserThumbprint = filterAndMapToThumbprint(clientCertificateFilesByCommonName, "ttp-client")
				.findFirst().get();
		Stream<String> confTtpUserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"ttp-client", "Webbrowser Test User");
		Stream<String> confTtpUserThumbprintsPermanentDelete = filterAndMapToThumbprint(
				clientCertificateFilesByCommonName, "ttp-client", "Webbrowser Test User");

		writeEnvFile(Paths.get("../../dsf-docker-test-setup-3medic-ttp/ttp/fhir/.env"), bundleTtpUserThumbprint,
				confTtpUserThumbprints, confTtpUserThumbprintsPermanentDelete);
	}

	public void generateAndWriteDockerTest3MedicTtpDockerFhirEnvFiles(
			Map<String, CertificateFiles> clientCertificateFilesByCommonName)
	{
		String bundleMedic1UserThumbprint = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic1-client").findFirst().get();
		Stream<String> confMedic1UserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic1-client", "Webbrowser Test User");
		Stream<String> confMedic1UserThumbprintsPermanentDelete = filterAndMapToThumbprint(
				clientCertificateFilesByCommonName, "medic1-client", "Webbrowser Test User");

		String bundleMedic2UserThumbprint = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic2-client").findFirst().get();
		Stream<String> confMedic2UserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic2-client", "Webbrowser Test User");
		Stream<String> confMedic2UserThumbprintsPermanentDelete = filterAndMapToThumbprint(
				clientCertificateFilesByCommonName, "medic2-client", "Webbrowser Test User");

		String bundleMedic3UserThumbprint = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic3-client").findFirst().get();
		Stream<String> confMedic3UserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic3-client", "Webbrowser Test User");
		Stream<String> confMedic3UserThumbprintsPermanentDelete = filterAndMapToThumbprint(
				clientCertificateFilesByCommonName, "medic3-client", "Webbrowser Test User");

		String bundleTtpUserThumbprint = filterAndMapToThumbprint(clientCertificateFilesByCommonName, "ttp-client")
				.findFirst().get();
		Stream<String> confTtpUserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"ttp-client", "Webbrowser Test User");
		Stream<String> confTtpUserThumbprintsPermanentDelete = filterAndMapToThumbprint(
				clientCertificateFilesByCommonName, "ttp-client", "Webbrowser Test User");

		List<EnvEntry> entries = List.of(
				new EnvEntry("MEDIC1_" + BUNDLE_USER_THUMBPRINT, bundleMedic1UserThumbprint,
						"MEDIC1_" + CONF_USER_THUMBPRINTS, confMedic1UserThumbprints,
						"MEDIC1_" + CONF_USER_THUMBPRINTS_PERMANENTDELETE, confMedic1UserThumbprintsPermanentDelete),
				new EnvEntry("MEDIC2_" + BUNDLE_USER_THUMBPRINT, bundleMedic2UserThumbprint,
						"MEDIC2_" + CONF_USER_THUMBPRINTS, confMedic2UserThumbprints,
						"MEDIC2_" + CONF_USER_THUMBPRINTS_PERMANENTDELETE, confMedic2UserThumbprintsPermanentDelete),
				new EnvEntry("MEDIC3_" + BUNDLE_USER_THUMBPRINT, bundleMedic3UserThumbprint,
						"MEDIC3_" + CONF_USER_THUMBPRINTS, confMedic3UserThumbprints,
						"MEDIC3_" + CONF_USER_THUMBPRINTS_PERMANENTDELETE, confMedic3UserThumbprintsPermanentDelete),
				new EnvEntry("TTP_" + BUNDLE_USER_THUMBPRINT, bundleTtpUserThumbprint, "TTP_" + CONF_USER_THUMBPRINTS,
						confTtpUserThumbprints, "TTP_" + CONF_USER_THUMBPRINTS_PERMANENTDELETE,
						confTtpUserThumbprintsPermanentDelete));

		writeEnvFile(Paths.get("../../dsf-docker-test-setup-3medic-ttp-docker/.env"), entries);
	}

	private Stream<String> filterAndMapToThumbprint(Map<String, CertificateFiles> clientCertificateFilesByCommonName,
			String... commonNames)
	{
		return clientCertificateFilesByCommonName.entrySet().stream()
				.filter(entry -> Arrays.asList(commonNames).contains(entry.getKey()))
				.sorted(Comparator.comparing(e -> Arrays.asList(commonNames).indexOf(e.getKey()))).map(Entry::getValue)
				.map(CertificateFiles::getCertificateSha512ThumbprintHex);
	}

	private void writeEnvFile(Path traget, String bundleUserThumbprint, Stream<String> confUserThumbprints,
			Stream<String> confUserThumbprintsPermanentDelete)
	{
		writeEnvFile(traget,
				Collections.singletonList(new EnvEntry(BUNDLE_USER_THUMBPRINT, bundleUserThumbprint,
						CONF_USER_THUMBPRINTS, confUserThumbprints, CONF_USER_THUMBPRINTS_PERMANENTDELETE,
						confUserThumbprintsPermanentDelete)));
	}

	private void writeEnvFile(Path target, List<? extends EnvEntry> entries)
	{
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < entries.size(); i++)
		{
			EnvEntry entry = entries.get(i);

			builder.append(entry.userThumbprintVariableName);
			builder.append('=');
			builder.append(entry.userThumbprint);
			builder.append('\n');
			builder.append(entry.userThumbprintsVariableName);
			builder.append('=');
			builder.append(entry.userThumbprints.collect(Collectors.joining(",")));
			builder.append('\n');
			builder.append(entry.userThumbprintsPermanentDeleteVariableName);
			builder.append('=');
			builder.append(entry.userThumbprintsPermanentDelete.collect(Collectors.joining(",")));

			if ((i + 1) < entries.size())
				builder.append("\n\n");
		}

		try
		{
			logger.info("Writing .env file to {}", target.toString());
			Files.writeString(target, builder.toString());
		}
		catch (IOException e)
		{
			logger.error("Error while writing .env file to " + target.toString(), e);
			throw new RuntimeException(e);
		}
	}
}
