package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

public interface AuthorizationRuleProvider
{
	Optional<AuthorizationRule<?>> getAuthorizationRule(Class<?> resourceClass);

	Optional<AuthorizationRule<?>> getAuthorizationRule(String resourceTypeName);
}
