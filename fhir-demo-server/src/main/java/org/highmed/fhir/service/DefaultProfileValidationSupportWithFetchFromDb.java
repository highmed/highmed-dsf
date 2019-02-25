package org.highmed.fhir.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.highmed.fhir.dao.StructureDefinitionDao;
import org.highmed.fhir.dao.StructureDefinitionSnapshotDao;
import org.highmed.fhir.function.SupplierWithSqlException;
import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;

public class DefaultProfileValidationSupportWithFetchFromDb extends DefaultProfileValidationSupport
		implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(DefaultProfileValidationSupportWithFetchFromDb.class);

	private final StructureDefinitionDao structureDefinitionDao;
	private final StructureDefinitionSnapshotDao structureDefinitionSnapshotDao;

	public DefaultProfileValidationSupportWithFetchFromDb(FhirContext context,
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
	public List<StructureDefinition> fetchAllStructureDefinitions(FhirContext context)
	{
		List<StructureDefinition> structureDefinitions = new ArrayList<>();
		structureDefinitions.addAll(throwRuntimeException(() -> structureDefinitionDao.readAll()));
		structureDefinitions.addAll(throwRuntimeException(() -> structureDefinitionSnapshotDao.readAll()));

		structureDefinitions.addAll(super.fetchAllStructureDefinitions(context));

		return structureDefinitions;
	}

	@Override
	public StructureDefinition fetchStructureDefinition(FhirContext context, String url)
	{
		Optional<StructureDefinition> structureDefinition = null;
		structureDefinition = throwRuntimeException(() -> structureDefinitionSnapshotDao.readByUrl(url));
		if (structureDefinition.isPresent())
			return structureDefinition.get();

		structureDefinition = throwRuntimeException(() -> structureDefinitionDao.readByUrl(url));
		if (structureDefinition.isPresent())
			return structureDefinition.get();

		return super.fetchStructureDefinition(context, url);
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

	@Override
	public CodeSystem fetchCodeSystem(FhirContext context, String system)
	{
		// TODO Auto-generated method stub
		return super.fetchCodeSystem(context, system);
	}

	// TODO add fetchValueSystem as soon as its available in IValidationSupport
	// @Override
	// public ValueSet fetchValueSet(FhirContext context, String system)
	// {
	// TODO Auto-generated method stub
	// return super.fetchValueSet(context, theSystem);
	// }
}
