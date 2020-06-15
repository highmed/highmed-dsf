package org.highmed.dsf.tools.generator;

import java.nio.file.Path;

import org.hl7.fhir.r4.model.Resource;

public interface BundleEntryPostReader
{
	void read(Class<? extends Resource> resource, Path resourceFile, Path putFile);
}
