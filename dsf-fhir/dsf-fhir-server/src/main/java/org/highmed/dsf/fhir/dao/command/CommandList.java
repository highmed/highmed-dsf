package org.highmed.dsf.fhir.dao.command;

import javax.ws.rs.WebApplicationException;

import org.hl7.fhir.r4.model.Bundle;

public interface CommandList
{
	Bundle execute() throws WebApplicationException;
}
