package org.highmed.dsf.fhir.group;

import org.hl7.fhir.r4.model.Group;

public interface GroupHelper
{
	public String extractAqlQuery(Group group);
}
