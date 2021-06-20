package org.highmed.dsf.fhir.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
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
		return Stream.of("highmed-read-access-tag-0.5.0.xml", "highmed-bpmn-message-0.5.0.xml")
				.map(file -> "/fhir/CodeSystem/" + file).map(this::readCodeSystem).collect(Collectors.toList());
	}

	private CodeSystem readCodeSystem(String file)
	{
		try (InputStream in = ValueSetExpanderTest.class.getResourceAsStream(file))
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
				.expand(readValueSet("/fhir/ValueSet/highmed-read-access-tag-0.5.0.xml"));

		assertNotNull(out);
		assertNull(out.getError());
		assertNull(out.getErrorClass());
		assertNotNull(out.getValueset());

		assertNotNull(out.getValueset().hasExpansion());
		assertEquals(4, out.getValueset().getExpansion().getTotal());
		assertEquals(4, out.getValueset().getExpansion().getContains().size());
	}

	private ValueSet readValueSet(String file)
	{
		try (InputStream in = ValueSetExpanderTest.class.getResourceAsStream(file))
		{
			return fhirContext.newXmlParser().parseResource(ValueSet.class, in);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
