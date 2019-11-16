package org.joda.beans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the type of the {@link MetaBeanProvider} that can provide a {@link MetaBean}
 * for the annotated type.
 * <p>
 * The provider class must have a no-args constructor.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface MetaProvider {

    Class<? extends MetaBeanProvider> value();
}
