package org.highmed.dsf.fhir.validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;

public class StructureDefinitionReader
{
	private static final String VERSION_PATTERN_STRING1 = "#{version}";
	private static final Pattern VERSION_PATTERN1 = Pattern.compile(Pattern.quote(VERSION_PATTERN_STRING1));
	// ${...} pattern to be backwards compatible
	private static final String VERSION_PATTERN_STRING2 = "${version}";
	private static final Pattern VERSION_PATTERN2 = Pattern.compile(Pattern.quote(VERSION_PATTERN_STRING2));

	private static final String DATE_PATTERN_STRING1 = "#{date}";
	private static final Pattern DATE_PATTERN1 = Pattern.compile(Pattern.quote(DATE_PATTERN_STRING1));
	// ${...} pattern to be backwards compatible
	private static final String DATE_PATTERN_STRING2 = "${date}";
	private static final Pattern DATE_PATTERN2 = Pattern.compile(Pattern.quote(DATE_PATTERN_STRING2));
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final FhirContext context;
	private final String version;
	private final LocalDate date;

	public StructureDefinitionReader(FhirContext context)
	{
		this(context, null);
	}

	public StructureDefinitionReader(FhirContext context, String version)
	{
		this(context, version, LocalDate.MIN);
	}

	public StructureDefinitionReader(FhirContext context, String version, LocalDate date)
	{
		this.context = context;
		this.version = version;
		this.date = date;
	}

	public List<StructureDefinition> readXml(Path... xmlPaths)
	{
		return readXml(Arrays.asList(xmlPaths));
	}

	public List<StructureDefinition> readXml(String... xmlOnClassPaths)
	{
		return readXmlFromClassPath(Arrays.asList(xmlOnClassPaths));
	}

	public List<StructureDefinition> readXml(List<Path> xmlPaths)
	{
		return readXml(xmlPaths.stream()).collect(Collectors.toList());
	}

	public List<StructureDefinition> readXmlFromClassPath(List<String> xmlOnClassPaths)
	{
		return readXmlFromClassPath(xmlOnClassPaths.stream()).collect(Collectors.toList());
	}

	public Stream<StructureDefinition> readXml(Stream<Path> xmlPaths)
	{
		return xmlPaths.map(this::readXml);
	}

	public Stream<StructureDefinition> readXmlFromClassPath(Stream<String> xmlOnClassPaths)
	{
		return xmlOnClassPaths.map(this::readXml);
	}

	public StructureDefinition readXml(Path xmlPath)
	{
		return version == null ? doReadXml(xmlPath) : doReadXmlAndReplaceVersion(xmlPath, version, date);
	}

	private StructureDefinition doReadXml(Path xmlPath)
	{
		try (InputStream in = Files.newInputStream(xmlPath))
		{
			return context.newXmlParser().parseResource(StructureDefinition.class, in);
		}
		catch (DataFormatException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private StructureDefinition doReadXmlAndReplaceVersion(Path xmlPath, String version, LocalDate date)
	{
		try (InputStream in = Files.newInputStream(xmlPath))
		{
			String read = IOUtils.toString(in, StandardCharsets.UTF_8);
			read = replaceVersionAndDate(read, version, date);

			return context.newXmlParser().parseResource(StructureDefinition.class, read);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private String replaceVersionAndDate(String read, String version, LocalDate date)
	{
		read = VERSION_PATTERN1.matcher(read).replaceAll(version);
		read = VERSION_PATTERN2.matcher(read).replaceAll(version);

		if (date != null && !LocalDate.MIN.equals(date))
		{
			String dateValue = date.format(DATE_FORMAT);
			read = DATE_PATTERN1.matcher(read).replaceAll(dateValue);
			read = DATE_PATTERN2.matcher(read).replaceAll(dateValue);
		}
		return read;
	}

	public StructureDefinition readXml(String xmlOnClassPath)
	{
		return version == null ? doReadXml(xmlOnClassPath) : doReadXmlAndReplaceVersion(xmlOnClassPath, version, date);
	}

	private StructureDefinition doReadXml(String xmlOnClassPath)
	{
		try (InputStream in = StructureDefinitionReader.class.getResourceAsStream(xmlOnClassPath))
		{
			return context.newXmlParser().parseResource(StructureDefinition.class, in);
		}
		catch (DataFormatException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private StructureDefinition doReadXmlAndReplaceVersion(String xmlOnClassPath, String version, LocalDate date)
	{
		try (InputStream in = StructureDefinitionReader.class.getResourceAsStream(xmlOnClassPath))
		{
			String read = IOUtils.toString(in, StandardCharsets.UTF_8);
			read = replaceVersionAndDate(read, version, date);

			return context.newXmlParser().parseResource(StructureDefinition.class, read);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
