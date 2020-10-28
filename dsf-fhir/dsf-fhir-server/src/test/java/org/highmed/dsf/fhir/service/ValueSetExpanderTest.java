package org.highmed.dsf.fhir.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.terminologies.ValueSetExpander.ValueSetExpansionOutcome;
import org.junit.Before;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.i18n.HapiLocalizer;

public class ValueSetExpanderTest
{
	private static final String CODE_SYSTEM_FOLDER = "src/main/resources/fhir/CodeSystem";
	private static final String VALUE_SET_FOLDER = "src/main/resources/fhir/ValueSet";

	private FhirContext fhirContext;
	private ValueSetExpander valueSetExpander;

	@Before
	public void before() throws Exception
	{
		fhirContext = FhirContext.forR4();
		fhirContext.setLocalizer(new HapiLocalizer()
		{
			@Override
			public Locale getLocale()
			{
				return Locale.ROOT;
			}
		});

		var validationSupport = new ValidationSupportChain(new InMemoryTerminologyServerValidationSupport(fhirContext),
				new ValidationSupportWithCustomResources(fhirContext, Collections.emptyList(), readCodeSystems(),
						Collections.emptyList()),
				new DefaultProfileValidationSupport(fhirContext));

		valueSetExpander = new ValueSetExpanderImpl(fhirContext, validationSupport);
	}

	private List<CodeSystem> readCodeSystems()
	{
		return Stream
				.of("authorization-role-0.2.0.xml", "bpmn-message-0.2.0.xml", "feasibility-0.3.0.xml",
						"organization-type-0.2.0.xml", "query-type-0.3.0.xml", "update-resources-0.2.0.xml",
						"update-whitelist-0.2.0.xml")
				.map(f -> Paths.get(CODE_SYSTEM_FOLDER, f)).map(this::readCodeSystem).collect(Collectors.toList());
	}

	private CodeSystem readCodeSystem(Path file)
	{
		try (InputStream in = Files.newInputStream(file))
		{
			return fhirContext.newXmlParser().parseResource(CodeSystem.class, in);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testExpandFeasibility() throws Exception
	{
		ValueSetExpansionOutcome out = valueSetExpander
				.expand(readValueSet(Paths.get(VALUE_SET_FOLDER, "authorization-role-0.2.0.xml")));

		assertNotNull(out);
		assertNull(out.getError());
		assertNull(out.getErrorClass());
		assertNotNull(out.getValueset());

		assertNotNull(out.getValueset().hasExpansion());
		assertEquals(2, out.getValueset().getExpansion().getTotal());
		assertEquals(2, out.getValueset().getExpansion().getContains().size());
	}

	private ValueSet readValueSet(Path file)
	{
		try (InputStream in = Files.newInputStream(file))
		{
			return fhirContext.newXmlParser().parseResource(ValueSet.class, in);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
