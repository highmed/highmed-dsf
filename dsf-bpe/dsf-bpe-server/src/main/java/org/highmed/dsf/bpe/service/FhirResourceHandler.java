package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Map;

import org.highmed.dsf.bpe.plugin.ProcessPluginDefinitionAndClassLoader;
import org.highmed.dsf.bpe.process.ProcessKeyAndVersion;
import org.highmed.dsf.bpe.process.ProcessStateChangeOutcome;

public interface FhirResourceHandler
{
	void applyStateChangesAndStoreNewResourcesInDb(
			Map<ProcessKeyAndVersion, ProcessPluginDefinitionAndClassLoader> definitionByProcessKeyAndVersion,
			List<ProcessStateChangeOutcome> changes);
}
