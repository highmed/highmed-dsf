package org.highmed.dsf.fhir.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public final class MultiInstanceTargetsValues
{
	public static interface MultiInstanceTargetsValue extends PrimitiveValue<MultiInstanceTargets>
	{
	}

	private static class MultiInstanceTargetsValueImpl extends PrimitiveTypeValueImpl<MultiInstanceTargets>
			implements MultiInstanceTargetsValue
	{
		private static final long serialVersionUID = 1L;

		public MultiInstanceTargetsValueImpl(MultiInstanceTargets value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class MultiInstanceTargetsValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private MultiInstanceTargetsValueTypeImpl()
		{
			super(MultiInstanceTargets.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new MultiInstanceTargetsValueImpl((MultiInstanceTargets) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new MultiInstanceTargetsValueTypeImpl();

	private MultiInstanceTargetsValues()
	{
	}

	public static MultiInstanceTargetsValue create(MultiInstanceTargets value)
	{
		return new MultiInstanceTargetsValueImpl(value, VALUE_TYPE);
	}
}
