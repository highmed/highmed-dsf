package org.highmed.dsf.fhir.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public final class OutputsValues
{
	public static interface OutputsValue extends PrimitiveValue<Outputs>
	{
	}

	private static class OutputsValueImpl extends PrimitiveTypeValueImpl<Outputs> implements OutputsValue
	{
		private static final long serialVersionUID = 1L;

		public OutputsValueImpl(Outputs value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class OutputsValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private OutputsValueTypeImpl()
		{
			super(Outputs.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new OutputsValueImpl((Outputs) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new OutputsValueTypeImpl();

	private OutputsValues()
	{
	}

	public static OutputsValue create(Outputs value)
	{
		return new OutputsValueImpl(value, VALUE_TYPE);
	}
}
