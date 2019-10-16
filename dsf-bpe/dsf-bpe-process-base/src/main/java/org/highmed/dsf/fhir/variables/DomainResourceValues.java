package org.highmed.dsf.fhir.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.hl7.fhir.r4.model.DomainResource;

public final class DomainResourceValues
{
	public static interface DomainResourceValue extends PrimitiveValue<DomainResource>
	{
	}

	private static class DomainResourceValueImpl extends PrimitiveTypeValueImpl<DomainResource>
			implements DomainResourceValue
	{
		private static final long serialVersionUID = 1L;

		public DomainResourceValueImpl(DomainResource value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class DomainResourceTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private DomainResourceTypeImpl()
		{
			super(DomainResource.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new DomainResourceValueImpl((DomainResource) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new DomainResourceTypeImpl();

	private DomainResourceValues()
	{
	}

	public static DomainResourceValue create(DomainResource value)
	{
		return new DomainResourceValueImpl(value, VALUE_TYPE);
	}
}
