/*
 * Copyright (C) 2019 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package org.joda.beans;

import java.lang.annotation.*;

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
