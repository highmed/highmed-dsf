package org.highmed.dsf.fhir.subscription;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ReadWriteMap<K, V>
{
	private final Map<K, V> map = new HashMap<>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock r = lock.readLock();
	private final Lock w = lock.writeLock();

	public Optional<V> get(Object key)
	{
		r.lock();
		try
		{
			return Optional.ofNullable(map.get(key));
		}
		finally
		{
			r.unlock();
		}
	}

	public Set<K> getAllKeys()
	{
		r.lock();
		try
		{
			return map.keySet();
		}
		finally
		{
			r.unlock();
		}

	}

	public void replaceAll(Map<K, V> map)
	{
		w.lock();
		try
		{
			this.map.clear();
			this.map.putAll(map);
		}
		finally
		{
			w.unlock();
		}
	}

	public void replace(K key, Function<V, V> put)
	{
		w.lock();
		try
		{
			map.put(key, put.apply(map.get(key)));
		}
		finally
		{
			w.unlock();
		}
	}

	public void removeWhereValueMatches(Predicate<V> remove, Consumer<V> change)
	{
		w.lock();
		try
		{
			for (Iterator<Entry<K, V>> iterator = map.entrySet().iterator(); iterator.hasNext();)
			{
				Entry<K, V> entry = iterator.next();
				change.accept(entry.getValue());
				if (remove.test(entry.getValue()))
					iterator.remove();
			}
		}
		finally
		{
			w.unlock();
		}
	}

	public boolean containsKey(Object key)
	{
		r.lock();
		try
		{
			return map.containsKey(key);
		}
		finally
		{
			r.unlock();
		}
	}
}
