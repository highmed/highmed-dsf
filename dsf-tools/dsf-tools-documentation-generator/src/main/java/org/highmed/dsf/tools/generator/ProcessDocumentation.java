package org.highmed.dsf.tools.generator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for documenting DSF process plugin properties
 *
 * @see Documentation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ProcessDocumentation
{
	/**
	 * @return <code>true</code> if this property is required for processes listed in
	 *         {@link ProcessDocumentation#processNames}
	 */
	boolean required() default false;

	/**
	 * @return an empty array if all processes use this property or an array of length {@literal >= 1} containing only
	 *         specific processes that use this property, but not all
	 */
	String[] processNames() default {};

	/**
	 * @return description helping to configure this property
	 */
	String description();

	/**
	 * @return example value helping to configure this property
	 */
	String example() default "";

	/**
	 * @return recommendation helping to configure this property
	 */
	String recommendation() default "";
}
