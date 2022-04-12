package org.highmed.dsf.tools.generator;

import static java.util.stream.Collectors.toList;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.highmed.dsf.bpe.ProcessPluginDefinition;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class DocumentationGenerator
{
	private static final Logger logger = LoggerFactory.getLogger(DocumentationGenerator.class);

	private static final String ENV_VARIABLE_PLACEHOLDER = "${env_variable}";
	private static final String PROPERTY_NAME_PLACEHOLDER = " ${property_name}";

	public static void main(String[] args)
	{
		new DocumentationGenerator().execute(args);
	}

	public void execute(String[] args)
	{
		Arrays.asList(args).forEach(this::generateDocumentation);
	}

	private void generateDocumentation(String workingPackage)
	{
		String filename = "target/Documentation_" + workingPackage + ".md";
		logger.info("Generating documentation for package {} in file {}", workingPackage, filename);

		Reflections reflections = createReflections(workingPackage);

		List<String> pluginProcessNames = getPluginProcessNames(reflections, workingPackage);
		List<Field> fields = getFields(reflections);

		writeFields(fields, pluginProcessNames, filename, workingPackage);
	}

	private Reflections createReflections(String workingPackage)
	{
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
				.setUrls(ClasspathHelper.forPackage(workingPackage))
				.setScanners(Scanners.FieldsAnnotated, Scanners.SubTypes);
		return new Reflections(configurationBuilder);
	}

	private List<String> getPluginProcessNames(Reflections reflections, String workingPackage)
	{
		List<Class<? extends ProcessPluginDefinition>> pluginDefinitionClasses = new ArrayList<>(
				reflections.getSubTypesOf(ProcessPluginDefinition.class));

		if (pluginDefinitionClasses.size() < 1)
		{
			logger.warn("No ProcessPluginDefinitions found in package {}", workingPackage);
			return Collections.emptyList();
		}

		if (pluginDefinitionClasses.size() > 1)
			logger.warn("Found {} ProcessPluginDefinitions ({}) in package {}, using only the first",
					pluginDefinitionClasses.size(), pluginDefinitionClasses, workingPackage);

		try
		{
			ProcessPluginDefinition processPluginDefinition = pluginDefinitionClasses.get(0).getConstructor()
					.newInstance();

			return processPluginDefinition.getBpmnFiles().map(this::getProcessName).filter(Optional::isPresent)
					.map(Optional::get).collect(toList());
		}
		catch (Exception exception)
		{
			logger.warn(
					"Could not read process names from package {} and ProcessPluginDefinition with name {}, reason is '{}'",
					workingPackage, pluginDefinitionClasses.get(0).getName(), exception.getMessage());
			return Collections.emptyList();
		}
	}

	private Optional<String> getProcessName(String bpmnFile)
	{
		try
		{
			InputStream resource = getClass().getClassLoader().getResource(bpmnFile).openStream();
			return Bpmn.readModelFromStream(resource).getModelElementsByType(Process.class).stream()
					.map(BaseElement::getId).findFirst();
		}
		catch (Exception exception)
		{
			logger.warn("Could not read process name from resource file {}, reason is '{}'", bpmnFile,
					exception.getMessage());
			return Optional.empty();
		}
	}

	private List<Field> getFields(Reflections reflections)
	{
		List<Field> fields = new ArrayList<>(reflections.getFieldsAnnotatedWith(Documentation.class));
		Collections.reverse(fields);
		return fields;
	}

	private void writeFields(List<Field> fields, List<String> pluginProcessNames, String filename,
			String workingPackage)
	{
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename)))
		{
			fields.stream().map(field -> createDocumentation(field, pluginProcessNames))
					.forEach(d -> write(writer, filename, d));
		}
		catch (Exception exception)
		{
			logger.warn("Could not generate documentation for package {}, reason is '{}'", workingPackage,
					exception.getMessage());
		}
	}

	private String createDocumentation(Field field, List<String> pluginProcessNames)
	{
		Documentation documentation = field.getAnnotation(Documentation.class);
		Value value = field.getAnnotation(Value.class);

		String[] valueSplit = getValueDefaultArray(value.value());

		String initialProperty = valueSplit[0];
		String property = getDocumentationString("Property", initialProperty);

		String initialEnvironment = initialProperty.replace(".", "_").toUpperCase();
		String environment = documentation.filePropertySupported()
				? String.format("%s or %s_FILE", initialEnvironment, initialEnvironment)
				: initialEnvironment;

		String required = getDocumentationString("Required", String.valueOf(documentation.required()));

		String[] processNames = documentation.processNames();
		boolean processProperty = documentation.processProperty();
		String processes = processProperty
				? getDocumentationString("Processes", getProcessNamesAsString(processNames, pluginProcessNames))
				: "";

		String description = getDocumentationString("Description", documentation.description());
		String recommendation = getDocumentationString("Recommendation", documentation.recommendation());
		String example = getDocumentationString("Example", documentation.example());

		String defaultValue = (valueSplit.length == 2 && !"null".equals(valueSplit[1]))
				? getDocumentationString("Default", valueSplit[1])
				: "";

		return String
				.format("### %s\n%s%s%s%s%s%s%s\n", environment, property, required, processes, description,
						recommendation, example, defaultValue)
				.replace(ENV_VARIABLE_PLACEHOLDER, initialEnvironment)
				.replace(PROPERTY_NAME_PLACEHOLDER, initialProperty);
	}

	private String[] getValueDefaultArray(String value)
	{
		if (value.startsWith("#{'"))
			value = value.substring(value.indexOf("#{'") + 4, value.indexOf("}'"));

		return value.replaceAll("\\$", "").replace("#", "").replace("{", "").replace("}", "").split(":");
	}

	private String getDocumentationString(String title, String value)
	{
		if (title == null || title.isBlank() || value == null || value.isBlank())
			return "";

		return String.format("- **%s:** %s\n", title, value);
	}

	private String getProcessNamesAsString(String[] documentationProcessNames, List<String> pluginProcessNames)
	{
		if (pluginProcessNames.size() == 0)
			return "Could not read process names from ProcessPluginDefinition";

		if (documentationProcessNames.length == 0)
			return String.join(", ", pluginProcessNames);

		for (String documentationProcessName : documentationProcessNames)
		{
			if (!pluginProcessNames.contains(documentationProcessName))
				logger.warn(
						"Documentation contains process with name '{}' which"
								+ " is not part of the processes {} defined in the ProcessPluginDefinition",
						documentationProcessName, pluginProcessNames);
		}

		return String.join(", ", documentationProcessNames);
	}

	private void write(BufferedWriter writer, String filename, String string)
	{
		try
		{
			writer.append(string);
		}
		catch (IOException e)
		{
			logger.warn("Writing the following string to file {} failed: \n {} reason is '{}'", filename, string,
					e.getMessage());
		}
	}
}
