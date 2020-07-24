package org.highmed.dsf.bpe.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class FinalFeasibilityQueryResultValues
{
	public static interface FinalFeasibilityQueryResultValue extends PrimitiveValue<FinalFeasibilityQueryResult>
	{
	}

	private static class FinalFeasibilityQueryResultValueImpl
			extends PrimitiveTypeValueImpl<FinalFeasibilityQueryResult>
			implements FinalFeasibilityQueryResultValues.FinalFeasibilityQueryResultValue
	{
		private static final long serialVersionUID = 1L;

		public FinalFeasibilityQueryResultValueImpl(FinalFeasibilityQueryResult value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class FinalFeasibilityQueryResultValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private FinalFeasibilityQueryResultValueTypeImpl()
		{
			super(FinalFeasibilityQueryResult.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new FinalFeasibilityQueryResultValues.FinalFeasibilityQueryResultValueImpl(
					(FinalFeasibilityQueryResult) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new FinalFeasibilityQueryResultValues.FinalFeasibilityQueryResultValueTypeImpl();

	private FinalFeasibilityQueryResultValues()
	{
	}

	public static FinalFeasibilityQueryResultValues.FinalFeasibilityQueryResultValue create(
			FinalFeasibilityQueryResult value)
	{
		return new FinalFeasibilityQueryResultValues.FinalFeasibilityQueryResultValueImpl(value, VALUE_TYPE);
	}
}
