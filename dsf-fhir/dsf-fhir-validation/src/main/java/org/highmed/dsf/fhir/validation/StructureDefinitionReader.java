package org.highmed.dsf.fhir.validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
	private static final String VERSION_PATTERN_STRING = "${version}";
	private static final Pattern VERSION_PATTERN = Pattern.compile(Pattern.quote(VERSION_PATTERN_STRING));

	private final FhirContext context;
	private final String version;

	public StructureDefinitionReader(FhirContext context)
	{
		this(context, null);
	}

	public StructureDefinitionReader(FhirContext context, String version)
	{
		this.context = context;
		this.version = version;
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
		return version == null ? doReadXml(xmlPath) : doReadXmlAndReplaceVersion(xmlPath, version);
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

	private StructureDefinition doReadXmlAndReplaceVersion(Path xmlPath, String version)
	{
		try (InputStream in = Files.newInputStream(xmlPath))
		{
			String read = IOUtils.toString(in, StandardCharsets.UTF_8);
			read = VERSION_PATTERN.matcher(read).replaceAll(version);

			return context.newXmlParser().parseResource(StructureDefinition.class, read);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public StructureDefinition readXml(String xmlOnClassPath)
	{
		return version == null ? doReadXml(xmlOnClassPath) : doReadXmlAndReplaceVersion(xmlOnClassPath, version);
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

	private StructureDefinition doReadXmlAndReplaceVersion(String xmlOnClassPath, String version)
	{
		try (InputStream in = StructureDefinitionReader.class.getResourceAsStream(xmlOnClassPath))
		{
			String read = IOUtils.toString(in, StandardCharsets.UTF_8);
			read = VERSION_PATTERN.matcher(read).replaceAll(version);

			return context.newXmlParser().parseResource(StructureDefinition.class, read);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
