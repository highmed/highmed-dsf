package org.highmed.dsf.fhir.service;

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
import org.highmed.dsf.fhir.dao.MeasureDao;
import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.dao.ValueSetDao;
import org.highmed.dsf.fhir.function.SupplierWithSqlException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;

public class ValidationSupportWithFetchFromDb implements IValidationSupport, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(ValidationSupportWithFetchFromDb.class);

	private final FhirContext context;

	private final StructureDefinitionDao structureDefinitionDao;
	private final StructureDefinitionDao structureDefinitionSnapshotDao;
	private final CodeSystemDao codeSystemDao;
	private final ValueSetDao valueSetDao;
	private final MeasureDao measureDao;

	public ValidationSupportWithFetchFromDb(FhirContext context, StructureDefinitionDao structureDefinitionDao,
			StructureDefinitionDao structureDefinitionSnapshotDao, CodeSystemDao codeSystemDao, ValueSetDao valueSetDao,
			MeasureDao measureDao)
	{
		this.context = context;

		this.structureDefinitionDao = structureDefinitionDao;
		this.structureDefinitionSnapshotDao = structureDefinitionSnapshotDao;
		this.codeSystemDao = codeSystemDao;
		this.valueSetDao = valueSetDao;
		this.measureDao = measureDao;
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
				.concat(throwRuntimeException(() -> codeSystemDao.readAll()).stream(),
						Stream.concat(fetchAllStructureDefinitions().stream(),
								throwRuntimeException(() -> valueSetDao.readAll()).stream()))
				.collect(Collectors.toList());
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<StructureDefinition> fetchAllStructureDefinitions()
	{
		Map<String, StructureDefinition> byUrl = new HashMap<>();
		throwRuntimeException(() -> structureDefinitionSnapshotDao.readAll()).forEach(s -> byUrl.put(s.getUrl(), s));
		throwRuntimeException(() -> structureDefinitionDao.readAll()).forEach(s -> byUrl.putIfAbsent(s.getUrl(), s));

		return new ArrayList<>(byUrl.values());
	}

	@Override
	public <T extends IBaseResource> T fetchResource(Class<T> theClass, String theUri)
	{
		T resource = IValidationSupport.super.fetchResource(theClass, theUri);
		if (resource != null)
		{
			return resource;
		}

		if (Measure.class.equals(theClass))
		{
			return theClass.cast(fetchMeasure(theUri));
		}

		return null;
	}

	@Override
	public StructureDefinition fetchStructureDefinition(String url)
	{
		Optional<StructureDefinition> structureDefinition = null;
		structureDefinition = throwRuntimeException(() -> structureDefinitionSnapshotDao.readByUrlAndVersion(url));
		if (structureDefinition.isPresent())
			return structureDefinition.get();

		structureDefinition = throwRuntimeException(() -> structureDefinitionDao.readByUrlAndVersion(url));
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
		Optional<CodeSystem> codeSystem = throwRuntimeException(() -> codeSystemDao.readByUrlAndVersion(url));
		if (codeSystem.isPresent())
			return codeSystem.get();
		else
			return null;
	}

	@Override
	public ValueSet fetchValueSet(String url)
	{
		Optional<ValueSet> valueSet = throwRuntimeException(() -> valueSetDao.readByUrlAndVersion(url));
		if (valueSet.isPresent())
			return valueSet.get();
		else
			return null;
	}

	public Measure fetchMeasure(String url)
	{
		Optional<Measure> measure = throwRuntimeException(() -> measureDao.readByUrlAndVersion(url));
		if (measure.isPresent())
			return measure.get();
		else
			return null;
	}
}
