package org.highmed.dsf.fhir.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.hl7.fhir.r4.model.Resource;

public final class FhirResourceValues
{
	public static interface FhirResourceValue extends PrimitiveValue<Resource>
	{
	}

	private static class FhirResourceValueImpl extends PrimitiveTypeValueImpl<Resource> implements FhirResourceValue
	{
		private static final long serialVersionUID = 1L;

		public FhirResourceValueImpl(Resource value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class FhirResourceTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private FhirResourceTypeImpl()
		{
			super(Resource.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new FhirResourceValueImpl((Resource) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new FhirResourceTypeImpl();

	private FhirResourceValues()
	{
	}

	public static FhirResourceValue create(Resource resource)
	{
		return new FhirResourceValueImpl(resource, VALUE_TYPE);
	}
}
