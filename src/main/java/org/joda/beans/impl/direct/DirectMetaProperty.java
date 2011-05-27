/*
 *  Copyright 2001-2011 Stephen Colebourne
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
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.joda.beans.PropertyReadWrite;
import org.joda.beans.impl.BasicMetaProperty;

/**
 * A meta-property implementation used by the code generator.
 * <p>
 * This meta-property is designed for use with subclasses of {@link DirectBean}.
 * 
 * @param <P>  the type of the property content
 * @author Stephen Colebourne
 */
public final class DirectMetaProperty<P> extends BasicMetaProperty<P> {

    /** The meta-bean. */
    private final MetaBean metaBean;
    /** The field implementing the property. */
    private final Field field;
    /** The read-write type. */
    private final PropertyReadWrite readWrite;

    /**
     * Factory to create a read-write meta-property avoiding duplicate generics.
     * 
     * @param metaBean  the meta-bean, not null
     * @param propertyName  the property name, not empty
     * @param propertyType  the property type, not null
     */
    public static <P> DirectMetaProperty<P> ofReadWrite(
            MetaBean metaBean, String propertyName, Class<P> propertyType) {
        return new DirectMetaProperty<P>(metaBean, propertyName, propertyType, PropertyReadWrite.READ_WRITE);
    }

    /**
     * Factory to create a read-write meta-property avoiding duplicate generics.
     * 
     * @param metaBean  the meta-bean, not null
     * @param propertyName  the property name, not empty
     * @param propertyType  the property type, not null
     */
    public static <P> DirectMetaProperty<P> ofReadOnly(
            MetaBean metaBean, String propertyName, Class<P> propertyType) {
        return new DirectMetaProperty<P>(metaBean, propertyName, propertyType, PropertyReadWrite.READ_ONLY);
    }

    /**
     * Factory to create a read-write meta-property avoiding duplicate generics.
     * 
     * @param metaBean  the meta-bean, not null
     * @param propertyName  the property name, not empty
     * @param propertyType  the property type, not null
     */
    public static <P> DirectMetaProperty<P> ofWriteOnly(
            MetaBean metaBean, String propertyName, Class<P> propertyType) {
        return new DirectMetaProperty<P>(metaBean, propertyName, propertyType, PropertyReadWrite.WRITE_ONLY);
    }

    /**
     * Constructor.
     * 
     * @param metaBean  the meta-bean, not null
     * @param propertyName  the property name, not empty
     * @param propertyType  the property type, not null
     * @param readWrite  the read-write type, not null
     */
    private DirectMetaProperty(MetaBean metaBean, String propertyName, Class<P> propertyType, PropertyReadWrite readWrite) {
        super(propertyName);
        if (metaBean == null) {
            throw new NullPointerException("MetaBean must not be null");
        }
        if (propertyType == null) {
            throw new NullPointerException("Property type must not be null");
        }
        if (readWrite == null) {
            throw new NullPointerException("PropertyReadWrite must not be null");
        }
        this.metaBean = metaBean;
        Field field = null;
        Class<?> cls = metaBean.beanType();
        while (cls != DirectBean.class) {
            try {
                field = cls.getDeclaredField(propertyName);
                break;
            } catch (NoSuchFieldException ex) {
                try {
                    field = cls.getDeclaredField("_" + propertyName);
                    break;
                } catch (NoSuchFieldException ex2) {
                    cls = cls.getSuperclass();
                }
            }
        }
        if (field == null) {
            throw new IllegalStateException("Unable to find field for property: " + metaBean.beanType().getName() + "#" + propertyName);
        }
        this.field = field;
        this.readWrite = readWrite;
    }

    //-----------------------------------------------------------------------
    @Override
    public MetaBean metaBean() {
        return metaBean;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<P> propertyType() {
        return (Class<P>) field.getType();
    }

    @Override
    public Type propertyGenericType() {
        return field.getGenericType();
    }

    @Override
    public PropertyReadWrite readWrite() {
        return readWrite;
    }

    @Override
    public <A extends Annotation> A annotation(Class<A> annotationClass) {
        A annotation = field.getAnnotation(annotationClass);
        if (annotation == null) {
            throw new NoSuchElementException("Unknown annotation: " + annotationClass.getName());
        }
        return annotation;
    }

    @Override
    public List<Annotation> annotations() {
        return Arrays.asList(field.getDeclaredAnnotations());
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public P get(Bean bean) {
        if (readWrite.isReadable() == false) {
            throw new UnsupportedOperationException("Property cannot be read: " + name());
        }
        return (P) ((DirectBean) bean).propertyGet(name());
    }

    @Override
    public void set(Bean bean, Object value) {
        if (readWrite.isWritable() == false) {
            throw new UnsupportedOperationException("Property cannot be written: " + name());
        }
        ((DirectBean) bean).propertySet(name(), value);
    }

}
