package org.highmed.dsf.bpe.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class FeasibilityQueryResultValues
{
	public static interface FeasibilityQueryResultValue extends PrimitiveValue<FeasibilityQueryResult>
	{
	}

	private static class FeasibilityQueryResultValueImpl extends PrimitiveTypeValueImpl<FeasibilityQueryResult>
			implements FeasibilityQueryResultValues.FeasibilityQueryResultValue
	{
		private static final long serialVersionUID = 1L;

		public FeasibilityQueryResultValueImpl(FeasibilityQueryResult value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class FeasibilityQueryResultValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private FeasibilityQueryResultValueTypeImpl()
		{
			super(FeasibilityQueryResult.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new FeasibilityQueryResultValues.FeasibilityQueryResultValueImpl((FeasibilityQueryResult) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new FeasibilityQueryResultValues.FeasibilityQueryResultValueTypeImpl();

	private FeasibilityQueryResultValues()
	{
	}

	public static FeasibilityQueryResultValues.FeasibilityQueryResultValue create(FeasibilityQueryResult value)
	{
		return new FeasibilityQueryResultValues.FeasibilityQueryResultValueImpl(value, VALUE_TYPE);
	}
}
