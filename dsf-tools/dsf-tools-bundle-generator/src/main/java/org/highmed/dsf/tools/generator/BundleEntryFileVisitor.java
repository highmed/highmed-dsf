package org.highmed.dsf.tools.generator;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleEntryFileVisitor implements FileVisitor<Path>
{
	private static final Logger logger = LoggerFactory.getLogger(BundleEntryFileVisitor.class);

	private final Path baseFolder;
	private final BundleEntryPutReader putReader;

	private Class<Resource> resource;

	public BundleEntryFileVisitor(Path baseFolder, BundleEntryPutReader putReader)
	{
		this.baseFolder = baseFolder;
		this.putReader = putReader;
	}

	@Override
	@SuppressWarnings("unchecked")
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
	{
		if (dir.equals(baseFolder))
			return FileVisitResult.CONTINUE;
		else if (dir.getParent().equals(baseFolder))
		{
			try
			{
				Class<?> c = Class.forName(Resource.class.getPackageName() + "." + dir.getFileName());
				if (Resource.class.isAssignableFrom(c))
				{
					resource = (Class<Resource>) c;
				}
				else
					logger.error("{} not a Resource", c.getName());

				return FileVisitResult.CONTINUE;
			}
			catch (ClassNotFoundException e)
			{
				logger.error("Error while visiting folder {}, class with name org.hl7.fhir.r4.model.{} not found.",
						dir.toString(), dir.getFileName().toString());
				return FileVisitResult.SKIP_SUBTREE;
			}
		}
		else
		{
			logger.warn("Skipping subtree {}", dir.toString());
			return FileVisitResult.SKIP_SUBTREE;
		}
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
	{
		if (resource != null && file.getFileName().toString().endsWith(".xml"))
		{
			Path putFile = file.resolveSibling(file.getFileName().toString() + ".put");
			if (!Files.isReadable(putFile))
			{
				logger.error("Put file for {} at {} not readable. Redable file {} expected", resource.getSimpleName(),
						file.toString(), putFile.toString());
				throw new IOException("Put file " + putFile.toString() + " not readable");
			}
			else
				putReader.read(resource, file, putFile);
		}
		else
			logger.debug("Ignoring {}", file.toString());

		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
	{
		logger.error("Error while reading file at " + file.toString(), exc);
		return FileVisitResult.TERMINATE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
	{
		resource = null;
		return FileVisitResult.CONTINUE;
	}
}