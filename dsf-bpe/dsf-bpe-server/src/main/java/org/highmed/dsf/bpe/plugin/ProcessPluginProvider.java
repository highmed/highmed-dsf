package org.highmed.dsf.bpe.plugin;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

public interface ProcessPluginProvider
{
	/**
	 * Expected folder/file structure:
	 * 
	 * <pre>
	 * {@link #pluginDirectory}/foo_plugin.jar
	 * 
	 * {@link #pluginDirectory}/bar_plugin.jar
	 * 
	 * {@link #pluginDirectory}/baz_plugin/plugin.jar
	 * {@link #pluginDirectory}/baz_plugin/helper.jar
	 * </pre>
	 * 
	 * The folder structure above will result in three separate class loaders being used. The jar files foo_plugin.jar
	 * and bar_plugin.jar will be loaded in a separate class loader each. All jar files in the baz_plugin directory will
	 * be loaded within the same class loader.
	 * 
	 * @return Plug in definitions found in jar files within {@link #pluginDirectory} or jar files within folders within
	 *         {@link #pluginDirectory} and associated class loaders
	 */
	List<ProcessPluginDefinitionAndClassLoader> getDefinitions();

	/**
	 * @return class loaders for process plugin jars by process definition key / version
	 * @see #getDefinitions()
	 */
	Map<String, ClassLoader> getClassLoadersByProcessDefinitionKeyAndVersion();

	/**
	 * @return application contexts for process plugin jars by process definition key / version
	 * @see #getDefinitions()
	 */
	Map<String, ApplicationContext> getApplicationContextsByProcessDefinitionKeyAndVersion();
}
