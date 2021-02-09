package org.highmed.dsf.fhir.variables;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.hl7.fhir.r4.model.Resource;

public final class FhirResourcesListValues
{
	public static interface FhirResourcesListValue extends PrimitiveValue<FhirResourcesList>
	{
		@SuppressWarnings("unchecked")
		default <R extends Resource> List<R> getFhirResources()
		{
			return (List<R>) getValue().getResources();
		}
	}

	private static class FhirResourcesListValueImpl extends PrimitiveTypeValueImpl<FhirResourcesList>
			implements FhirResourcesListValue
	{
		private static final long serialVersionUID = 1L;

		public FhirResourcesListValueImpl(FhirResourcesList value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class FhirResourcesListTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private FhirResourcesListTypeImpl()
		{
			super(FhirResourcesList.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new FhirResourcesListValueImpl((FhirResourcesList) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new FhirResourcesListTypeImpl();

	private FhirResourcesListValues()
	{
	}

	public static FhirResourcesListValue create(Resource... resources)
	{
		return new FhirResourcesListValueImpl(new FhirResourcesList(resources), VALUE_TYPE);
	}

	public static FhirResourcesListValue create(Collection<? extends Resource> resources)
	{
		return new FhirResourcesListValueImpl(new FhirResourcesList(resources), VALUE_TYPE);
	}

	public static FhirResourcesListValue create(FhirResourcesList value)
	{
		return new FhirResourcesListValueImpl(value, VALUE_TYPE);
	}
}
