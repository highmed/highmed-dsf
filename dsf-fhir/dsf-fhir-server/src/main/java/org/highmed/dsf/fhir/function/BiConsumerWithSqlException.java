package org.highmed.dsf.fhir.function;

import java.sql.SQLException;
import java.util.Objects;

@FunctionalInterface
public interface BiConsumerWithSqlException<T, U>
{
	void accept(T t, U u) throws SQLException;

	default BiConsumerWithSqlException<T, U> andThen(BiConsumerWithSqlException<? super T, ? super U> after)
	{
		Objects.requireNonNull(after);
		return (T t, U u) ->
		{
			SQLException suppressed = null;
			try
			{
				accept(t, u);
			}
			catch (SQLException e)
			{
				suppressed = e;
			}
			finally
			{
				try
				{
					after.accept(t, u);
				}
				catch (SQLException e)
				{
					if (suppressed != null)
						e.addSuppressed(suppressed);
					throw e;
				}
			}
		};
	}
}