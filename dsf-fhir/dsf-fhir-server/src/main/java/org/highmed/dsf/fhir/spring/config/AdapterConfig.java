package org.highmed.dsf.fhir.spring.config;

import java.util.Set;

import javax.ws.rs.ext.Provider;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;

@Configuration
public class AdapterConfig implements BeanDefinitionRegistryPostProcessor
{
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
	{
		// nothing to do
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException
	{
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Provider.class));
		Set<BeanDefinition> adapters = scanner.findCandidateComponents("org.highmed.dsf.fhir.adapter");
		adapters.forEach(def -> registry.registerBeanDefinition(def.getBeanClassName(), def));
	}
}
