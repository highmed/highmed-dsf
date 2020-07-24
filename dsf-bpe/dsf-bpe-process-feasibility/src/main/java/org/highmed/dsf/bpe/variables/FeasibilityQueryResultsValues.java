package org.highmed.dsf.bpe.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class FeasibilityQueryResultsValues
{
	public static interface FeasibilityQueryResultsValue extends PrimitiveValue<FeasibilityQueryResults>
	{
	}

	private static class FeasibilityQueryResultsValueImpl extends PrimitiveTypeValueImpl<FeasibilityQueryResults>
			implements FeasibilityQueryResultsValues.FeasibilityQueryResultsValue
	{
		private static final long serialVersionUID = 1L;

		public FeasibilityQueryResultsValueImpl(FeasibilityQueryResults value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class FeasibilityQueryResultsValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private FeasibilityQueryResultsValueTypeImpl()
		{
			super(FeasibilityQueryResults.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new FeasibilityQueryResultsValues.FeasibilityQueryResultsValueImpl((FeasibilityQueryResults) value,
					VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new FeasibilityQueryResultsValues.FeasibilityQueryResultsValueTypeImpl();

	private FeasibilityQueryResultsValues()
	{
	}

	public static FeasibilityQueryResultsValues.FeasibilityQueryResultsValue create(FeasibilityQueryResults value)
	{
		return new FeasibilityQueryResultsValues.FeasibilityQueryResultsValueImpl(value, VALUE_TYPE);
	}
}
