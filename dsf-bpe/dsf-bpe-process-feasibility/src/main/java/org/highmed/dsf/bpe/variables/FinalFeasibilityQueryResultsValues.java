package org.highmed.dsf.bpe.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class FinalFeasibilityQueryResultsValues
{
	public static interface FinalFeasibilityQueryResultsValue extends PrimitiveValue<FinalFeasibilityQueryResults>
	{
	}

	private static class FinalFeasibilityQueryResultsValueImpl
			extends PrimitiveTypeValueImpl<FinalFeasibilityQueryResults>
			implements FinalFeasibilityQueryResultsValues.FinalFeasibilityQueryResultsValue
	{
		private static final long serialVersionUID = 1L;

		public FinalFeasibilityQueryResultsValueImpl(FinalFeasibilityQueryResults value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class FinalFeasibilityQueryResultsValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private FinalFeasibilityQueryResultsValueTypeImpl()
		{
			super(FinalFeasibilityQueryResults.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new FinalFeasibilityQueryResultsValues.FinalFeasibilityQueryResultsValueImpl(
					(FinalFeasibilityQueryResults) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new FinalFeasibilityQueryResultsValues.FinalFeasibilityQueryResultsValueTypeImpl();

	private FinalFeasibilityQueryResultsValues()
	{
	}

	public static FinalFeasibilityQueryResultsValues.FinalFeasibilityQueryResultsValue create(
			FinalFeasibilityQueryResults value)
	{
		return new FinalFeasibilityQueryResultsValues.FinalFeasibilityQueryResultsValueImpl(value, VALUE_TYPE);
	}
}
