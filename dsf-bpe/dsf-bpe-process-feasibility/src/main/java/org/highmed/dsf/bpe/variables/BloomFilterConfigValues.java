package org.highmed.dsf.bpe.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class BloomFilterConfigValues
{
	public static interface BloomFilterConfigValue extends PrimitiveValue<BloomFilterConfig>
	{
	}

	private static class BloomFilterConfigValueImpl extends PrimitiveTypeValueImpl<BloomFilterConfig>
			implements BloomFilterConfigValues.BloomFilterConfigValue
	{
		private static final long serialVersionUID = 1L;

		public BloomFilterConfigValueImpl(BloomFilterConfig value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class BloomFilterConfigValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private BloomFilterConfigValueTypeImpl()
		{
			super(BloomFilterConfig.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new BloomFilterConfigValues.BloomFilterConfigValueImpl((BloomFilterConfig) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new BloomFilterConfigValues.BloomFilterConfigValueTypeImpl();

	private BloomFilterConfigValues()
	{
	}

	public static BloomFilterConfigValues.BloomFilterConfigValue create(BloomFilterConfig value)
	{
		return new BloomFilterConfigValues.BloomFilterConfigValueImpl(value, VALUE_TYPE);
	}
}
