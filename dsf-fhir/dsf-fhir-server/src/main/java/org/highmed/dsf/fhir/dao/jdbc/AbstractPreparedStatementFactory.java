package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;
import org.postgresql.util.PGobject;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;

abstract class AbstractPreparedStatementFactory<R extends Resource> implements PreparedStatementFactory<R>
{
	private final FhirContext fhirContext;
	private final Class<R> resourceType;

	private final String createSql;
	private final String readByIdSql;
	private final String readByIdAndVersionSql;
	private final String updateNewRowSql;
	private final String updateSameRowSql;

	protected AbstractPreparedStatementFactory(FhirContext fhirContext, Class<R> resourceType, String createSql,
			String readByIdSql, String readByIdAndVersionSql, String updateNewRowSql, String updateSameRowSql)
	{
		this.fhirContext = Objects.requireNonNull(fhirContext, "fhirContext");
		this.resourceType = Objects.requireNonNull(resourceType, "resourceType");
		this.createSql = Objects.requireNonNull(createSql, "createSql");
		this.readByIdSql = Objects.requireNonNull(readByIdSql, "readByIdSql");
		this.readByIdAndVersionSql = Objects.requireNonNull(readByIdAndVersionSql, "readByIdAndVersionSql");
		this.updateNewRowSql = Objects.requireNonNull(updateNewRowSql, "updateNewRowSql");
		this.updateSameRowSql = Objects.requireNonNull(updateSameRowSql, "updateSameRowSql");
	}

	@Override
	public IParser getJsonParser()
	{
		IParser p = fhirContext.newJsonParser();
		p.setStripVersionsFromReferences(false);
		return p;
	}

	protected final R jsonToResource(String json)
	{
		R resource = getJsonParser().parseResource(resourceType, json);

		// TODO Bugfix HAPI is not setting version information from bundle.id while parsing non DomainResource
		if (!(resource instanceof DomainResource))
		{
			IdType fixedId = new IdType(resource.getResourceType().name(), resource.getIdElement().getIdPart(),
					resource.getMeta().getVersionId());
			resource.setIdElement(fixedId);
		}

		return resource;
	}

	@Override
	public final PGobject resourceToPgObject(R resource)
	{
		if (resource == null)
			return null;

		try
		{
			PGobject o = new PGobject();
			o.setType("JSONB");
			o.setValue(getJsonParser().encodeResourceToString(resource));
			return o;
		}
		catch (DataFormatException | SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public final PGobject uuidToPgObject(UUID uuid)
	{
		if (uuid == null)
			return null;

		try
		{
			PGobject o = new PGobject();
			o.setType("UUID");
			o.setValue(uuid.toString());
			return o;
		}
		catch (DataFormatException | SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public final String getCreateSql()
	{
		return createSql;
	}

	@Override
	public final String getReadByIdSql()
	{
		return readByIdSql;
	}

	@Override
	public final String getReadByIdAndVersionSql()
	{
		return readByIdAndVersionSql;
	}

	@Override
	public final String getUpdateNewRowSql()
	{
		return updateNewRowSql;
	}

	@Override
	public final String getUpdateSameRowSql()
	{
		return updateSameRowSql;
	}
}
