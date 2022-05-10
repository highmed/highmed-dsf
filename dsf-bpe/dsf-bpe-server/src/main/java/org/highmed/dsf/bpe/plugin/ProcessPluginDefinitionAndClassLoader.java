package org.highmed.dsf.bpe.plugin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.highmed.dsf.bpe.ProcessPluginDefinition;
import org.highmed.dsf.bpe.process.BpmnFileAndModel;
import org.highmed.dsf.bpe.process.ProcessKeyAndVersion;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertyResolver;

import ca.uhn.fhir.context.FhirContext;

public class ProcessPluginDefinitionAndClassLoader
{
	private static final Logger logger = LoggerFactory.getLogger(ProcessPluginDefinitionAndClassLoader.class);

	private static final String VERSION_PATTERN_STRING = "#{version}";
	private static final Pattern VERSION_PATTERN = Pattern.compile(Pattern.quote(VERSION_PATTERN_STRING));

	private static final String DATE_PATTERN_STRING = "#{date}";
	private static final Pattern DATE_PATTERN = Pattern.compile(Pattern.quote(DATE_PATTERN_STRING));
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private static final String PLACEHOLDER_PREFIX_SPRING = "${";
	private static final String PLACEHOLDER_PREFIX_SPRING_ESCAPED = "\\${";
	private static final String PLACEHOLDER_PREFIX_TMP = "§{";
	private static final String PLACEHOLDER_PREFIX = "#{";

	private static final Pattern PLACEHOLDER_PREFIX_PATTERN_SPRING = Pattern
			.compile(Pattern.quote(PLACEHOLDER_PREFIX_SPRING));
	private static final Pattern PLACEHOLDER_PREFIX_PATTERN_TMP = Pattern
			.compile(Pattern.quote(PLACEHOLDER_PREFIX_TMP));
	private static final Pattern PLACEHOLDER_PREFIX_PATTERN = Pattern.compile(Pattern.quote(PLACEHOLDER_PREFIX));

	private final FhirContext fhirContext;
	private final List<Path> jars = new ArrayList<>();
	private final ProcessPluginDefinition definition;
	private final ClassLoader classLoader;
	private final boolean draft;

	private final PropertyResolver resolver;

	private List<BpmnFileAndModel> models;
	private AnnotationConfigApplicationContext context;
	private ResourceProvider resourceProvider;

	public ProcessPluginDefinitionAndClassLoader(FhirContext fhirContext, List<Path> jars,
			ProcessPluginDefinition definition, ClassLoader classLoader, boolean draft, PropertyResolver resolver)
	{
		this.fhirContext = fhirContext;

		if (jars != null)
			this.jars.addAll(jars);

		this.definition = definition;
		this.classLoader = classLoader;
		this.draft = draft;
		this.resolver = resolver;
	}

	public List<Path> getJars()
	{
		return Collections.unmodifiableList(jars);
	}

	public ProcessPluginDefinition getDefinition()
	{
		return definition;
	}

	public ClassLoader getClassLoader()
	{
		return classLoader;
	}

	public ApplicationContext getPluginApplicationContext(ApplicationContext mainContext)
	{
		if (context == null)
		{
			context = new AnnotationConfigApplicationContext();
			context.setParent(mainContext);
			context.setClassLoader(getClassLoader());
			context.register(getDefinition().getSpringConfigClasses().toArray(Class<?>[]::new));
			context.setEnvironment((ConfigurableEnvironment) mainContext.getEnvironment());

			tryContextRefresh(mainContext);
		}

		return context;
	}

	private void tryContextRefresh(ApplicationContext mainContext)
	{
		try
		{
			context.refresh();
		}
		catch (BeanCreationException e)
		{
			logger.error("Unable to create spring application context for plugin {}: {} {}",
					getDefinition().getNameAndVersion(), e.getClass().getSimpleName(), e.getMessage());
			logger.debug("Unable to create spring application context for plugin " + getDefinition().getNameAndVersion()
					+ ", bean with error " + e.getBeanName(), e);

			// using empty (aka no config classes registered) application context for this failed plugin
			context = new AnnotationConfigApplicationContext();
			context.setParent(mainContext);
			context.setClassLoader(getClassLoader());
			context.setEnvironment((ConfigurableEnvironment) mainContext.getEnvironment());
			context.refresh();
		}
	}

	public List<ProcessKeyAndVersion> getProcessKeysAndVersions()
	{
		return getAndValidateModels().stream().flatMap(fileAndModel ->
		{
			Collection<Process> processes = fileAndModel.getModel().getModelElementsByType(Process.class);
			return processes.stream().map(p -> new ProcessKeyAndVersion(p.getId(), p.getCamundaVersionTag()));
		}).collect(Collectors.toList());
	}

	public List<BpmnFileAndModel> getAndValidateModels()
	{
		return getAndValidateModels(jars, getDefinition().getBpmnFiles(), getDefinition().getVersion(),
				getDefinition().getReleaseDate(), getClassLoader());
	}

	private List<BpmnFileAndModel> getAndValidateModels(List<Path> jars, Stream<String> bpmnFiles,
			String processPluginVersion, LocalDate processPluginDate, ClassLoader classLoader)
	{
		if (models == null)
			models = bpmnFiles
					.map(file -> loadAndValidateModel(file, processPluginVersion, processPluginDate, classLoader, jars))
					.collect(Collectors.toList());

		return models;
	}

	private BpmnFileAndModel loadAndValidateModel(String bpmnFile, String processPluginVersion,
			LocalDate processPluginDate, ClassLoader classLoader, List<Path> jars)
	{
		logger.debug("Reading BPMN from {} and replacing all occurrences of {} with {}", bpmnFile,
				VERSION_PATTERN_STRING, processPluginVersion);

		String processPluginDateValue = null;
		if (processPluginDate != null && !LocalDate.MIN.equals(processPluginDate))
		{
			processPluginDateValue = processPluginDate.format(DATE_FORMAT);
			logger.debug("Replacing all occurrences of {} with {}", DATE_PATTERN_STRING, processPluginDateValue);
		}

		try (InputStream in = classLoader.getResourceAsStream(bpmnFile))
		{
			String read = IOUtils.toString(in, StandardCharsets.UTF_8);

			read = VERSION_PATTERN.matcher(read).replaceAll(processPluginVersion);

			if (processPluginDateValue != null)
				read = DATE_PATTERN.matcher(read).replaceAll(processPluginDateValue);

			// escape bpmn placeholders
			read = PLACEHOLDER_PREFIX_PATTERN_SPRING.matcher(read).replaceAll(PLACEHOLDER_PREFIX_TMP);
			// make dsf placeholders look like spring placeholders
			// when calling replaceAll with ${ the $ needs to be escaped using \${
			read = PLACEHOLDER_PREFIX_PATTERN.matcher(read).replaceAll(PLACEHOLDER_PREFIX_SPRING_ESCAPED);
			// resolve dsf placeholders
			read = resolver.resolveRequiredPlaceholders(read);
			// revert bpmn placeholders
			// when calling replaceAll with ${ the $ needs to be escaped using \${
			read = PLACEHOLDER_PREFIX_PATTERN_TMP.matcher(read).replaceAll(PLACEHOLDER_PREFIX_SPRING_ESCAPED);

			BpmnModelInstance model = Bpmn
					.readModelFromStream(new ByteArrayInputStream(read.getBytes(StandardCharsets.UTF_8)));
			Bpmn.validateModel(model);
			validateModelVersionTagsAndProcessCount(bpmnFile, model);

			return new BpmnFileAndModel(bpmnFile, model, jars);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error while reading " + bpmnFile, e);
		}
	}

	private void validateModelVersionTagsAndProcessCount(String bpmnFile, BpmnModelInstance model)
	{
		Collection<Process> processes = model.getModelElementsByType(Process.class);

		if (processes.size() != 1)
			throw new RuntimeException(
					"BPMN file " + bpmnFile + " contains " + processes.size() + " processes, expected 1");

		processes.forEach(p ->
		{
			if (!definition.getVersion().equals(p.getCamundaVersionTag()))
				throw new RuntimeException("Camunda version tag in process '" + p.getId()
						+ "' does not match process plugin version (tag: " + p.getCamundaVersionTag() + " vs. plugin: "
						+ definition.getVersion() + ")");
		});
	}

	public ResourceProvider getResourceProvider()
	{
		if (resourceProvider == null)
			resourceProvider = getDefinition().getResourceProvider(fhirContext, getClassLoader(), resolver);

		return resourceProvider;
	}

	public boolean isDraft()
	{
		return draft;
	}
}
