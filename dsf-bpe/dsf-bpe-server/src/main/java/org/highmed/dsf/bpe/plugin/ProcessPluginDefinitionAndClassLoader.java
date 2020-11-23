package org.highmed.dsf.bpe.plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.highmed.dsf.ProcessPluginDefinition;
import org.highmed.dsf.bpe.process.BpmnFileAndModel;
import org.highmed.dsf.bpe.process.ProcessKeyAndVersion;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import ca.uhn.fhir.context.FhirContext;

public class ProcessPluginDefinitionAndClassLoader
{
	private final FhirContext fhirContext;
	private final List<Path> jars = new ArrayList<>();
	private final ProcessPluginDefinition definition;
	private final ClassLoader classLoader;
	private final boolean draft;

	private List<BpmnFileAndModel> models;
	private ResourceProvider resourceProvider;

	public ProcessPluginDefinitionAndClassLoader(FhirContext fhirContext, List<Path> jars,
			ProcessPluginDefinition definition, ClassLoader classLoader, boolean draft)
	{
		this.fhirContext = fhirContext;

		if (jars != null)
			this.jars.addAll(jars);

		this.definition = definition;
		this.classLoader = classLoader;
		this.draft = draft;
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

	public ApplicationContext createPluginApplicationContext(ApplicationContext mainContext)
	{
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.setParent(mainContext);
		context.setClassLoader(getClassLoader());
		context.register(getDefinition().getSpringConfigClasses().toArray(Class<?>[]::new));
		context.refresh();
		return context;
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
		return getAndValidateModels(jars, getDefinition().getBpmnFiles(), getClassLoader());
	}

	private List<BpmnFileAndModel> getAndValidateModels(List<Path> jars, Stream<String> bpmnFiles,
			ClassLoader classLoader)
	{
		if (models == null)
			models = bpmnFiles.map(file -> loadAndValidateModel(file, classLoader, jars)).collect(Collectors.toList());

		return models;
	}

	private BpmnFileAndModel loadAndValidateModel(String bpmnFile, ClassLoader classLoader, List<Path> jars)
	{
		BpmnModelInstance model = Bpmn.readModelFromStream(classLoader.getResourceAsStream(bpmnFile));
		Bpmn.validateModel(model);
		validateModelVersionTags(model);
		return new BpmnFileAndModel(bpmnFile, model, jars);
	}

	private void validateModelVersionTags(BpmnModelInstance model)
	{
		Collection<Process> processes = model.getModelElementsByType(Process.class);
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
			resourceProvider = getDefinition().getResourceProvider(fhirContext, getClassLoader());

		return resourceProvider;
	}

	public boolean isDraft()
	{
		return draft;
	}
}
