package org.highmed.dsf.tools.generator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for documenting DSF properties
 *
 * @see ProcessDocumentation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Documentation
{
	/**
	 * @return <code>true</code> if this property is required for the DSF
	 */
	boolean required() default false;

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
