package org.highmed.dsf.fhir.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public final class TargetsValues
{
	public static interface TargetsValue extends PrimitiveValue<Targets>
	{
	}

	private static class TargetsValueImpl extends PrimitiveTypeValueImpl<Targets> implements TargetsValue
	{
		private static final long serialVersionUID = 1L;

		public TargetsValueImpl(Targets value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class TargetsValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private TargetsValueTypeImpl()
		{
			super(Targets.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new TargetsValueImpl((Targets) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new TargetsValueTypeImpl();

	private TargetsValues()
	{
	}

	public static TargetsValue create(Targets value)
	{
		return new TargetsValueImpl(value, VALUE_TYPE);
	}
}
