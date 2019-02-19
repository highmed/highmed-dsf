package org.highmed.fhir.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.highmed.fhir.dao.StructureDefinitionDao;
import org.highmed.fhir.dao.StructureDefinitionSnapshotDao;
import org.highmed.fhir.function.SupplierWithSqlException;
import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;

public class DefaultProfileValidationSupportWithCustomStructureDefinitionsFromDb extends DefaultProfileValidationSupport
		implements InitializingBean
{
	private static final Logger logger = LoggerFactory
			.getLogger(DefaultProfileValidationSupportWithCustomStructureDefinitionsFromDb.class);

	private final StructureDefinitionDao structureDefinitionDao;
	private final StructureDefinitionSnapshotDao structureDefinitionSnapshotDao;

	public DefaultProfileValidationSupportWithCustomStructureDefinitionsFromDb(FhirContext context,
			StructureDefinitionDao structureDefinitionDao,
			StructureDefinitionSnapshotDao structureDefinitionSnapshotDao)
	{
		this.structureDefinitionDao = structureDefinitionDao;
		this.structureDefinitionSnapshotDao = structureDefinitionSnapshotDao;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(structureDefinitionDao, "structureDefinitionDao");
		Objects.requireNonNull(structureDefinitionSnapshotDao, "structureDefinitionSnapshotDao");
	}

	@Override
	public List<StructureDefinition> fetchAllStructureDefinitions(FhirContext theContext)
	{
		List<StructureDefinition> structureDefinitions = new ArrayList<>();
		structureDefinitions.addAll(throwRuntimeException(() -> structureDefinitionDao.readAll()));
		structureDefinitions.addAll(throwRuntimeException(() -> structureDefinitionSnapshotDao.readAll()));

		structureDefinitions.addAll(super.fetchAllStructureDefinitions(theContext));

		return structureDefinitions;
	}

	@Override
	public StructureDefinition fetchStructureDefinition(FhirContext theContext, String theUrl)
	{
		StructureDefinition structureDefinition = null;
		structureDefinition = throwRuntimeException(() -> structureDefinitionDao.readByUrl(theUrl));
		if (structureDefinition != null)
			return structureDefinition;

		structureDefinition = throwRuntimeException(() -> structureDefinitionSnapshotDao.readByUrl(theUrl));
		if (structureDefinition != null)
			return structureDefinition;

		return super.fetchStructureDefinition(theContext, theUrl);
	}

	private <R> R throwRuntimeException(SupplierWithSqlException<R> reader)
	{
		try
		{
			return reader.get();
		}
		catch (SQLException e)
		{
			logger.warn("Error while accessing DB", e);
			throw new RuntimeException(e);
		}
	}
}
