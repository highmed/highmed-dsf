package org.highmed.dsf.fhir.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.dao.CodeSystemDao;
import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.dao.ValueSetDao;
import org.highmed.dsf.fhir.function.SupplierWithSqlException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;

public class ValidationSupportWithFetchFromDbWithTransaction implements IValidationSupport, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(ValidationSupportWithFetchFromDbWithTransaction.class);

	private final FhirContext context;

	private final StructureDefinitionDao structureDefinitionDao;
	private final StructureDefinitionDao structureDefinitionSnapshotDao;
	private final CodeSystemDao codeSystemDao;
	private final ValueSetDao valueSetDao;

	private final Connection connection;

	public ValidationSupportWithFetchFromDbWithTransaction(FhirContext context,
			StructureDefinitionDao structureDefinitionDao, StructureDefinitionDao structureDefinitionSnapshotDao,
			CodeSystemDao codeSystemDao, ValueSetDao valueSetDao, Connection connection)
	{
		this.context = context;

		this.structureDefinitionDao = structureDefinitionDao;
		this.structureDefinitionSnapshotDao = structureDefinitionSnapshotDao;
		this.codeSystemDao = codeSystemDao;
		this.valueSetDao = valueSetDao;

		this.connection = connection;
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
	public FhirContext getFhirContext()
	{
		return context;
	}

	@Override
	public List<IBaseResource> fetchAllConformanceResources()
	{
		return Stream
				.concat(throwRuntimeException(() -> codeSystemDao.readAllWithTransaction(connection)).stream(),
						Stream.concat(fetchAllStructureDefinitions().stream(),
								throwRuntimeException(() -> valueSetDao.readAllWithTransaction(connection)).stream()))
				.collect(Collectors.toList());
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<StructureDefinition> fetchAllStructureDefinitions()
	{
		Map<String, StructureDefinition> byUrl = new HashMap<>();
		throwRuntimeException(() -> structureDefinitionSnapshotDao.readAllWithTransaction(connection))
				.forEach(s -> byUrl.put(s.getUrl(), s));
		throwRuntimeException(() -> structureDefinitionDao.readAllWithTransaction(connection))
				.forEach(s -> byUrl.putIfAbsent(s.getUrl(), s));

		return new ArrayList<>(byUrl.values());
	}

	@Override
	public StructureDefinition fetchStructureDefinition(String url)
	{
		Optional<StructureDefinition> structureDefinition = null;
		structureDefinition = throwRuntimeException(
				() -> structureDefinitionSnapshotDao.readByUrlAndVersionWithTransaction(connection, url));
		if (structureDefinition.isPresent())
			return structureDefinition.get();

		structureDefinition = throwRuntimeException(
				() -> structureDefinitionDao.readByUrlAndVersionWithTransaction(connection, url));
		if (structureDefinition.isPresent())
			return structureDefinition.get();

		return null;
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
	public CodeSystem fetchCodeSystem(String url)
	{
		Optional<CodeSystem> codeSystem = throwRuntimeException(
				() -> codeSystemDao.readByUrlAndVersionWithTransaction(connection, url));
		if (codeSystem.isPresent())
			return codeSystem.get();
		else
			return null;
	}

	@Override
	public ValueSet fetchValueSet(String url)
	{
		Optional<ValueSet> valueSet = throwRuntimeException(
				() -> valueSetDao.readByUrlAndVersionWithTransaction(connection, url));
		if (valueSet.isPresent())
			return valueSet.get();
		else
			return null;
	}
}
