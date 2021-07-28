package org.highmed.dsf.fhir.function;

import java.util.Objects;
import java.util.function.Function;

/**
 * Helper Class to create a Function with three arguments and one result. {@link java.util.function.BiFunction}
 *
 * @param <A>
 *            the type of the first argument to the function
 * @param <B>
 *            the type of the second argument to the function
 * @param <C>
 *            the type of the third argument to the function
 * @param <R>
 *            the type of the result of the function
 */
@FunctionalInterface
public interface TriFunction<A, B, C, R>
{

	/**
	 * @param a
	 *            the first function argument
	 * @param b
	 *            the second function argument
	 * @param c
	 *            the third function argument
	 * @return the function result
	 */
	R apply(A a, B b, C c);

	/**
	 * @param <O>
	 *            the type of output
	 * @param after
	 *            the function to apply after the following function is applied
	 * @return a function composes the first and second function are applicable
	 * @throws NullPointerException
	 *             if after is null
	 */
	default <O> TriFunction<A, B, C, O> andThen(Function<? super R, ? extends O> after)
	{
		Objects.requireNonNull(after);
		return (A a, B b, C c) -> after.apply(apply(a, b, c));
	}
}