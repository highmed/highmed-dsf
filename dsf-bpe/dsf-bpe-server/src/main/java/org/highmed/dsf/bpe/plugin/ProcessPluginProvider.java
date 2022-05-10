package org.highmed.dsf.bpe.plugin;

import java.util.List;
import java.util.Map;

import org.highmed.dsf.bpe.process.ProcessKeyAndVersion;
import org.highmed.dsf.bpe.process.ProcessStateChangeOutcome;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.springframework.context.ApplicationContext;

public interface ProcessPluginProvider
{
	/**
	 * Expected folder/file structure:
	 *
	 * <pre>
	 * pluginDirectory/foo_plugin.jar
	 *
	 * pluginDirectory/bar_plugin.jar
	 *
	 * pluginDirectory/baz_plugin/plugin.jar
	 * pluginDirectory/baz_plugin/helper.jar
	 * </pre>
	 *
	 * The folder structure above will result in three separate class loaders being used. The jar files foo_plugin.jar
	 * and bar_plugin.jar will be loaded in a separate class loader each. All jar files in the baz_plugin directory will
	 * be loaded within the same class loader.
	 *
	 * @return Plug in definitions found in jar files within a pluginDirectory or jar files within folders within a
	 *         pluginDirectory and associated class loaders
	 */
	List<ProcessPluginDefinitionAndClassLoader> getDefinitions();

	/**
	 * @return definitions by {@link ProcessKeyAndVersion}
	 * @see #getDefinitions()
	 */
	Map<ProcessKeyAndVersion, ProcessPluginDefinitionAndClassLoader> getDefinitionByProcessKeyAndVersion();

	/**
	 * @return class loaders for process plugin jars by process definition key / version
	 * @see #getDefinitions()
	 */
	Map<ProcessKeyAndVersion, ClassLoader> getClassLoadersByProcessDefinitionKeyAndVersion();

	/**
	 * @return application contexts for process plugin jars by process definition key / version
	 * @see #getDefinitions()
	 */
	Map<ProcessKeyAndVersion, ApplicationContext> getApplicationContextsByProcessDefinitionKeyAndVersion();

	Map<String, ResourceProvider> getResouceProvidersByDpendencyNameAndVersion();

	List<ProcessKeyAndVersion> getProcessKeyAndVersions();

	List<ProcessKeyAndVersion> getDraftProcessKeyAndVersions();

	void onProcessesDeployed(List<ProcessStateChangeOutcome> changes);
}
