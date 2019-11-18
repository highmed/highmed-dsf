package org.highmed.dsf.fhir.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public final class OutputValues
{
	public static final PrimitiveValueType VALUE_TYPE = new OutputValueTypeImpl();

	private OutputValues()
	{
	}

	public static OutputValue create(Output value)
	{
		return new OutputValueImpl(value, VALUE_TYPE);
	}

	public static interface OutputValue extends PrimitiveValue<Output>
	{
	}

	private static class OutputValueImpl extends PrimitiveTypeValueImpl<Output> implements OutputValue
	{
		private static final long serialVersionUID = 1L;

		public OutputValueImpl(Output value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class OutputValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private OutputValueTypeImpl()
		{
			super(Output.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new OutputValueImpl((Output) value, VALUE_TYPE);
		}
	}
}
