package org.highmed.dsf.bpe.camunda;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.impl.variable.serializer.AbstractTypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializerFactory;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class FallbackSerializerFactory implements VariableSerializerFactory, InitializingBean
{
	public static final class TypedValueSerializerWrapper<T extends TypedValue> extends AbstractTypedValueSerializer<T>
	{
		final TypedValueSerializer<T> delegate;

		TypedValueSerializerWrapper(TypedValueSerializer<T> delegate)
		{
			super(delegate.getType());

			this.delegate = delegate;
		}

		ClassLoader getClassLoader()
		{
			return delegate.getClass().getClassLoader();
		}

		@Override
		public String getName()
		{
			return getClassLoader().getName() + "/" + delegate.getName();
		}

		@Override
		public void writeValue(T value, ValueFields valueFields)
		{
			delegate.writeValue(value, valueFields);
		}

		@Override
		public T readValue(ValueFields valueFields, boolean deserializeValue, boolean isTransient)
		{
			return delegate.readValue(valueFields, deserializeValue, isTransient);
		}

		@Override
		public T convertToTypedValue(UntypedValueImpl untypedValue)
		{
			return delegate.convertToTypedValue(untypedValue);
		}

		@Override
		public boolean canHandle(TypedValue value)
		{
			return delegate.canHandle(value);
		}

		@Override
		protected boolean canWriteValue(TypedValue value)
		{
			throw new UnsupportedOperationException("canWriteValue method not supported");
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(FallbackSerializerFactory.class);

	@SuppressWarnings("rawtypes")
	private final Map<ClassLoader, List<TypedValueSerializer>> serializersByClassLoader;
	@SuppressWarnings("rawtypes")
	private final Map<String, TypedValueSerializer> serializersByName;

	@SuppressWarnings("unchecked")
	public FallbackSerializerFactory(@SuppressWarnings("rawtypes") List<TypedValueSerializer> serializers)
	{
		if (serializers != null)
		{
			serializers = serializers.stream().map(TypedValueSerializerWrapper::new).collect(Collectors.toList());

			serializersByClassLoader = serializers.stream()
					.collect(Collectors.groupingBy(s -> s.getType().getClass().getClassLoader(),
							Collectors.mapping(Function.identity(), Collectors.toList())));
			serializersByName = serializers.stream()
					.collect(Collectors.toMap(TypedValueSerializer::getName, Function.identity()));
		}
		else
		{
			serializersByClassLoader = new HashMap<>();
			serializersByName = new HashMap<>();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		int serializersCount = serializersByClassLoader.values().stream().mapToInt(List::size).sum();
		String serializers = serializersByClassLoader
				.entrySet().stream().map(e -> e.getKey().getName() + ": " + e.getValue().stream()
						.map(s -> s.getClass().getName()).collect(Collectors.joining(", ", "[", "]")))
				.collect(Collectors.joining(", "));

		logger.info("{} variable process plugin serializer{} configured", serializersCount,
				serializersCount != 1 ? "s" : "");
		logger.debug("Variable serializer{}: {}", serializersCount != 1 ? "s" : "", serializers);
	}

	@Override
	public TypedValueSerializer<?> getSerializer(String serializerName)
	{
		if (serializerName == null)
			return null;

		logger.debug("Getting serializer for {}", serializerName);
		return serializersByName.getOrDefault(serializerName, null);
	}

	@Override
	public TypedValueSerializer<?> getSerializer(TypedValue value)
	{
		if (value == null)
			return null;

		ClassLoader classLoader = getClassLoader(value);
		if (classLoader != null)
		{
			logger.debug("Getting serializer for {} from class loader {}", getName(value), classLoader.getName());

			return serializersByClassLoader.getOrDefault(classLoader, Collections.emptyList()).stream()
					.filter(s -> s.canHandle(value)).findFirst().orElse(null);
		}
		else
			return null;
	}

	@SuppressWarnings("rawtypes")
	private String getName(TypedValue value)
	{
		if (value == null)
			return null;

		if (value instanceof PrimitiveValue)
			return ((PrimitiveValue) value).getType().getJavaType().getName();
		else if (value.getValue() != null)
			return value.getClass().getName();
		else if (value.getType() != null)
			return value.getType().getName();
		else
			return "?";
	}

	private ClassLoader getClassLoader(TypedValue value)
	{
		if (value == null)
			return null;

		if (value instanceof PrimitiveValue)
			return value.getType().getClass().getClassLoader();
		else if (value.getValue() != null)
			return value.getValue().getClass().getClassLoader();
		else
			return null;
	}
}
