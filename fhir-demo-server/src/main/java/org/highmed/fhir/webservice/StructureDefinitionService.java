package org.highmed.fhir.webservice;

import javax.ws.rs.Path;

import org.highmed.fhir.dao.StructureDefinitionDao;
import org.hl7.fhir.r4.model.StructureDefinition;

@Path(StructureDefinitionService.RESOURCE_TYPE_NAME)
public class StructureDefinitionService extends AbstractService<StructureDefinitionDao, StructureDefinition>
{
	public static final String RESOURCE_TYPE_NAME = "StructureDefinition";

	public StructureDefinitionService(String serverBase, int defaultPageCount,
			StructureDefinitionDao structureDefinitionDao)
	{
		super(serverBase, defaultPageCount, RESOURCE_TYPE_NAME, structureDefinitionDao);
	}
}
