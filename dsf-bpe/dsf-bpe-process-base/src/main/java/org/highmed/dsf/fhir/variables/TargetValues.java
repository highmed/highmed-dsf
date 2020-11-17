package org.highmed.dsf.fhir.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public final class TargetValues
{
	public static interface TargetValue extends PrimitiveValue<Target>
	{
	}

	private static class TargetValueImpl extends PrimitiveTypeValueImpl<Target> implements TargetValue
	{
		private static final long serialVersionUID = 1L;

		public TargetValueImpl(Target value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class TargetValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private TargetValueTypeImpl()
		{
			super(Target.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new TargetValueImpl((Target) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new TargetValueTypeImpl();

	private TargetValues()
	{
	}

	public static TargetValue create(Target value)
	{
		return new TargetValueImpl(value, VALUE_TYPE);
	}
}
