package org.highmed.dsf.fhir.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public final class MultiInstanceTargetValues
{
	public static interface MultiInstanceTargetValue extends PrimitiveValue<MultiInstanceTarget>
	{
	}

	private static class MultiInstanceTargetValueImpl extends PrimitiveTypeValueImpl<MultiInstanceTarget>
			implements MultiInstanceTargetValue
	{
		private static final long serialVersionUID = 1L;

		public MultiInstanceTargetValueImpl(MultiInstanceTarget value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class MultiInstanceTargetValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private MultiInstanceTargetValueTypeImpl()
		{
			super(MultiInstanceTarget.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new MultiInstanceTargetValueImpl((MultiInstanceTarget) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new MultiInstanceTargetValueTypeImpl();

	private MultiInstanceTargetValues()
	{
	}

	public static MultiInstanceTargetValue create(MultiInstanceTarget value)
	{
		return new MultiInstanceTargetValueImpl(value, VALUE_TYPE);
	}
}
