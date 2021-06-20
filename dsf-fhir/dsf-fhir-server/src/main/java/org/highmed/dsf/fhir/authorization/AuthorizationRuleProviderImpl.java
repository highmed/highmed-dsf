package org.highmed.dsf.fhir.authorization;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class AuthorizationRuleProviderImpl implements AuthorizationRuleProvider
{
	private final Map<Class<? extends Resource>, AuthorizationRule<?>> authorizationRulesByResourecClass;
	private final Map<String, AuthorizationRule<?>> authorizationRulesByResourceTypeName = new HashMap<>();

	public AuthorizationRuleProviderImpl(AuthorizationRule<?>... rules)
	{
		authorizationRulesByResourecClass = Arrays.stream(rules)
				.collect(Collectors.toMap(AuthorizationRule::getResourceType, Function.identity()));

		authorizationRulesByResourecClass.forEach(
				(k, v) -> authorizationRulesByResourceTypeName.put(k.getAnnotation(ResourceDef.class).name(), v));
	}

	@Override
	public Optional<AuthorizationRule<?>> getAuthorizationRule(Class<?> resourceClass)
	{
		AuthorizationRule<?> authorizationRule = authorizationRulesByResourecClass.get(resourceClass);
		return Optional.ofNullable(authorizationRule);
	}

	@Override
	public Optional<AuthorizationRule<?>> getAuthorizationRule(String resourceTypeName)
	{
		AuthorizationRule<?> authorizationRule = authorizationRulesByResourceTypeName.get(resourceTypeName);
		return Optional.ofNullable(authorizationRule);
	}
}
