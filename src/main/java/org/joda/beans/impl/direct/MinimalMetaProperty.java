/*
 *  Copyright 2001-present Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.beans.impl.direct;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaBean;
import org.joda.beans.PropertyStyle;
import org.joda.beans.impl.BasicMetaProperty;

/**
 * An immutable meta-property based on a functional interface.
 * 
 * @param <P>  the type of the property content
 * @author Stephen Colebourne
 */
final class MinimalMetaProperty<P> extends BasicMetaProperty<P> {

    /** The meta-bean. */
    private final MetaBean metaBean;
    /** The type of the property. */
    private final Class<P> propertyType;
    /** The type of the property. */
    private final Type propertyGenericType;
    /** The annotations. */
    private final List<Annotation> annotations;
    /** The read method. */
    private final Function<Bean, Object> getter;
    /** The write method. */
    private final BiConsumer<Bean, Object> setter;
    /** The property style. */
    private final PropertyStyle style;

    //-----------------------------------------------------------------------
    /**
     * Creates an instance.
     * 
     * @param metaBean  the meta bean, not null
     * @param propertyName  the property name, not empty
     * @param field  the field, not null
     * @param getter  the property getter, not null
     * @param setter  the property setter, null if read only
     */
    @SuppressWarnings("unchecked")
    MinimalMetaProperty(
            MetaBean metaBean,
            String propertyName,
            Field field,
            Function<? extends Bean, Object> getter,
            BiConsumer<? extends Bean, Object> setter) {
        
        super(propertyName);
        this.metaBean = metaBean;
        this.propertyType = (Class<P>) field.getType();
        this.propertyGenericType = field.getGenericType();
        this.annotations = Arrays.asList(field.getAnnotations());
        this.getter = (Function<Bean, Object>) getter;
        this.setter = (BiConsumer<Bean, Object>) setter;
        if (ImmutableBean.class.isAssignableFrom(metaBean.beanType())) {
            this.style = PropertyStyle.IMMUTABLE;
        } else {
            this.style = setter != null ? PropertyStyle.READ_WRITE : PropertyStyle.READ_ONLY;
        }
    }

    /**
     * Creates an instance.
     * 
     * @param metaBean  the meta bean, not null
     * @param method  the method, not null
     * @param propertyName  the property name, not empty
     */
    @SuppressWarnings("unchecked")
    MinimalMetaProperty(
            MetaBean metaBean,
            Method method,
            String propertyName) {
        
        super(propertyName);
        this.metaBean = metaBean;
        this.propertyType = (Class<P>) method.getReturnType();
        this.propertyGenericType = method.getGenericReturnType();
        this.annotations = Arrays.asList(method.getAnnotations());
        this.getter = b -> {
            try {
                return method.invoke(b);
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
        this.setter = null;
        this.style = PropertyStyle.DERIVED;
    }

    //-----------------------------------------------------------------------
    @Override
    public MetaBean metaBean() {
        return metaBean;
    }

    @Override
    public Class<?> declaringType() {
        return metaBean.beanType();
    }

    @Override
    public Class<P> propertyType() {
        return propertyType;
    }

    @Override
    public Type propertyGenericType() {
        return propertyGenericType;
    }

    @Override
    public PropertyStyle style() {
        return style;
    }

    @Override
    public List<Annotation> annotations() {
        return annotations;
    }

    //-----------------------------------------------------------------------
    @Override
    @SuppressWarnings("unchecked")
    public P get(Bean bean) {
        try {
            return (P) getter.apply(bean);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void set(Bean bean, Object value) {
        if (setter == null) {
            throw new UnsupportedOperationException("Property cannot be written: " + name());
        }
        try {
            setter.accept(bean, value);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

}
