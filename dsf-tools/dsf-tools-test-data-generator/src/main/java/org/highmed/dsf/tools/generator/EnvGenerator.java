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

	private static final String USER_THUMBPRINTS = "USER_THUMBPRINTS";
	private static final String USER_THUMBPRINTS_PERMANENTDELETE = "USER_THUMBPRINTS_PERMANENT_DELETE";

	private static final class EnvEntry
	{
		final String userThumbprintsVariableName;
		final Stream<String> userThumbprints;
		final String userThumbprintsPermanentDeleteVariableName;
		final Stream<String> userThumbprintsPermanentDelete;

		EnvEntry(String userThumbprintsVariableName, Stream<String> userThumbprints,
				String userThumbprintsPermanentDeleteVariableName, Stream<String> userThumbprintsPermanentDelete)
		{
			this.userThumbprintsVariableName = userThumbprintsVariableName;
			this.userThumbprints = userThumbprints;
			this.userThumbprintsPermanentDeleteVariableName = userThumbprintsPermanentDeleteVariableName;
			this.userThumbprintsPermanentDelete = userThumbprintsPermanentDelete;
		}
	}

	public void generateAndWriteDockerTestFhirEnvFile(Map<String, CertificateFiles> clientCertificateFilesByCommonName)
	{
		Stream<String> userThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName, "test-client",
				"Webbrowser Test User");
		Stream<String> userThumbprintsPermanentDelete = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"test-client", "Webbrowser Test User");

		writeEnvFile(Paths.get("../../dsf-docker-test-setup/fhir/.env"), userThumbprints,
				userThumbprintsPermanentDelete);
	}

	public void generateAndWriteDockerTest3MedicTtpFhirEnvFiles(
			Map<String, CertificateFiles> clientCertificateFilesByCommonName)
	{
		Stream<String> medic1UserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic1-client", "Webbrowser Test User");
		Stream<String> medic1UserThumbprintsPermanentDelete = filterAndMapToThumbprint(
				clientCertificateFilesByCommonName, "medic1-client", "Webbrowser Test User");

		writeEnvFile(Paths.get("../../dsf-docker-test-setup-3medic-ttp/medic1/fhir/.env"), medic1UserThumbprints,
				medic1UserThumbprintsPermanentDelete);

		Stream<String> medic2UserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic2-client", "Webbrowser Test User");
		Stream<String> medic2UserThumbprintsPermanentDelete = filterAndMapToThumbprint(
				clientCertificateFilesByCommonName, "medic2-client", "Webbrowser Test User");

		writeEnvFile(Paths.get("../../dsf-docker-test-setup-3medic-ttp/medic2/fhir/.env"), medic2UserThumbprints,
				medic2UserThumbprintsPermanentDelete);

		Stream<String> medic3UserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic3-client", "Webbrowser Test User");
		Stream<String> medic3UserThumbprintsPermanentDelete = filterAndMapToThumbprint(
				clientCertificateFilesByCommonName, "medic3-client", "Webbrowser Test User");

		writeEnvFile(Paths.get("../../dsf-docker-test-setup-3medic-ttp/medic3/fhir/.env"), medic3UserThumbprints,
				medic3UserThumbprintsPermanentDelete);

		Stream<String> ttpUserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName, "ttp-client",
				"Webbrowser Test User");
		Stream<String> ttpUserThumbprintsPermanentDelete = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"ttp-client", "Webbrowser Test User");

		writeEnvFile(Paths.get("../../dsf-docker-test-setup-3medic-ttp/ttp/fhir/.env"), ttpUserThumbprints,
				ttpUserThumbprintsPermanentDelete);
	}

	public void generateAndWriteDockerTest3MedicTtpDockerFhirEnvFiles(
			Map<String, CertificateFiles> clientCertificateFilesByCommonName)
	{
		Stream<String> medic1UserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic1-client", "Webbrowser Test User");
		Stream<String> medic1UserThumbprintsPermanentDelete = filterAndMapToThumbprint(
				clientCertificateFilesByCommonName, "medic1-client", "Webbrowser Test User");

		Stream<String> medic2UserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic2-client", "Webbrowser Test User");
		Stream<String> medic2UserThumbprintsPermanentDelete = filterAndMapToThumbprint(
				clientCertificateFilesByCommonName, "medic2-client", "Webbrowser Test User");

		Stream<String> medic3UserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"medic3-client", "Webbrowser Test User");
		Stream<String> medic3UserThumbprintsPermanentDelete = filterAndMapToThumbprint(
				clientCertificateFilesByCommonName, "medic3-client", "Webbrowser Test User");

		Stream<String> ttpUserThumbprints = filterAndMapToThumbprint(clientCertificateFilesByCommonName, "ttp-client",
				"Webbrowser Test User");
		Stream<String> ttpUserThumbprintsPermanentDelete = filterAndMapToThumbprint(clientCertificateFilesByCommonName,
				"ttp-client", "Webbrowser Test User");

		List<EnvEntry> entries = List.of(
				new EnvEntry("MEDIC1_" + USER_THUMBPRINTS, medic1UserThumbprints,
						"MEDIC1_" + USER_THUMBPRINTS_PERMANENTDELETE, medic1UserThumbprintsPermanentDelete),
				new EnvEntry("MEDIC2_" + USER_THUMBPRINTS, medic2UserThumbprints,
						"MEDIC2_" + USER_THUMBPRINTS_PERMANENTDELETE, medic2UserThumbprintsPermanentDelete),
				new EnvEntry("MEDIC3_" + USER_THUMBPRINTS, medic3UserThumbprints,
						"MEDIC3_" + USER_THUMBPRINTS_PERMANENTDELETE, medic3UserThumbprintsPermanentDelete),
				new EnvEntry("TTP_" + USER_THUMBPRINTS, ttpUserThumbprints, "TTP_" + USER_THUMBPRINTS_PERMANENTDELETE,
						ttpUserThumbprintsPermanentDelete));

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

	private void writeEnvFile(Path traget, Stream<String> userThumbprints,
			Stream<String> userThumbprintsPermanentDelete)
	{
		writeEnvFile(traget, Collections.singletonList(new EnvEntry(USER_THUMBPRINTS, userThumbprints,
				USER_THUMBPRINTS_PERMANENTDELETE, userThumbprintsPermanentDelete)));
	}

	private void writeEnvFile(Path target, List<? extends EnvEntry> entries)
	{
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < entries.size(); i++)
		{
			EnvEntry entry = entries.get(i);

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
