package org.highmed.dsf.fhir.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.highmed.dsf.fhir.dao.CodeSystemDao;
import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.dao.StructureDefinitionSnapshotDao;
import org.highmed.dsf.fhir.dao.ValueSetDao;
import org.highmed.dsf.fhir.function.SupplierWithSqlException;
import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
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
	private final CodeSystemDao codeSystemDao;
	private final ValueSetDao valueSetDao;

	public DefaultProfileValidationSupportWithFetchFromDb(FhirContext context,
			StructureDefinitionDao structureDefinitionDao,
			StructureDefinitionSnapshotDao structureDefinitionSnapshotDao, CodeSystemDao codeSystemDao,
			ValueSetDao valueSetDao)
	{
		this.structureDefinitionDao = structureDefinitionDao;
		this.structureDefinitionSnapshotDao = structureDefinitionSnapshotDao;
		this.codeSystemDao = codeSystemDao;
		this.valueSetDao = valueSetDao;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(structureDefinitionDao, "structureDefinitionDao");
		Objects.requireNonNull(structureDefinitionSnapshotDao, "structureDefinitionSnapshotDao");
		Objects.requireNonNull(codeSystemDao, "codeSystemDao");
		Objects.requireNonNull(valueSetDao, "valueSetDao");
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
	public CodeSystem fetchCodeSystem(FhirContext context, String url)
	{
		Optional<CodeSystem> codeSystem = throwRuntimeException(() -> codeSystemDao.readByUrl(url));
		if (codeSystem.isPresent())
			return codeSystem.get();

		return super.fetchCodeSystem(context, url);
	}

	@Override
	public ValueSet fetchValueSet(FhirContext context, String url)
	{
		Optional<ValueSet> valueSet = throwRuntimeException(() -> valueSetDao.readByUrl(url));
		if (valueSet.isPresent())
			return valueSet.get();

		return super.fetchValueSet(context, url);
	}
}
