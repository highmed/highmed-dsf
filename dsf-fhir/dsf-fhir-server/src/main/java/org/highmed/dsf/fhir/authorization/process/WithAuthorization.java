package org.highmed.dsf.fhir.authorization.process;

import org.hl7.fhir.r4.model.Coding;

public interface WithAuthorization
{
	Coding getProcessAuthorizationCode();

	boolean matches(Coding processAuthorizationCode);
}
