/*
 *  Copyright 2001-2010 Stephen Colebourne
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

import org.joda.beans.Bean;
import org.joda.beans.PropertyReadWrite;
import org.joda.beans.impl.BasicMetaProperty;

/**
 * A meta-property implemented using the methods of {@code DirectBean}.
 * <p>
 * The property descriptor class is part of the JDK JavaBean standard.
 * It provides access to get and set a property on a bean.
 * 
 * @param <P>  the type of the property content
 * @author Stephen Colebourne
 */
public final class DirectMetaProperty<P> extends BasicMetaProperty<P> {

    /** The type of the property. */
    private final Class<P> propertyType;
    /** The read-write type. */
    private final PropertyReadWrite readWrite;

    /**
     * Factory to create a read-write meta-property avoiding duplicate generics.
     * 
     * @param beanType  the bean type, not null
     * @param propertyName  the property name, not empty
     * @param propertyType  the property type, not null
     * @param readWrite  the read-write type, not null
     */
    public static <P> DirectMetaProperty<P> ofReadWrite(
            Class<? extends DirectBean> beanType, String propertyName, Class<P> propertyType) {
        return new DirectMetaProperty<P>(beanType, propertyName, propertyType, PropertyReadWrite.READ_WRITE);
    }

    /**
     * Factory to create a read-write meta-property avoiding duplicate generics.
     * 
     * @param beanType  the bean type, not null
     * @param propertyName  the property name, not empty
     * @param propertyType  the property type, not null
     * @param readWrite  the read-write type, not null
     */
    public static <P> DirectMetaProperty<P> ofReadOnly(
            Class<? extends DirectBean> beanType, String propertyName, Class<P> propertyType) {
        return new DirectMetaProperty<P>(beanType, propertyName, propertyType, PropertyReadWrite.READ_ONLY);
    }

    /**
     * Factory to create a read-write meta-property avoiding duplicate generics.
     * 
     * @param beanType  the bean type, not null
     * @param propertyName  the property name, not empty
     * @param propertyType  the property type, not null
     * @param readWrite  the read-write type, not null
     */
    public static <P> DirectMetaProperty<P> ofWriteOnly(
            Class<? extends DirectBean> beanType, String propertyName, Class<P> propertyType) {
        return new DirectMetaProperty<P>(beanType, propertyName, propertyType, PropertyReadWrite.WRITE_ONLY);
    }

    /**
     * Constructor.
     * 
     * @param beanType  the bean type, not null
     * @param propertyName  the property name, not empty
     * @param propertyType  the property type, not null
     * @param readWrite  the read-write type, not null
     */
    private DirectMetaProperty(Class<? extends DirectBean> beanType, String propertyName, Class<P> propertyType, PropertyReadWrite readWrite) {
        super(beanType, propertyName);
        if (propertyType == null) {
            throw new NullPointerException("Property type must not be null");
        }
        if (readWrite == null) {
            throw new NullPointerException("PropertyReadWrite must not be null");
        }
        this.propertyType = propertyType;
        this.readWrite = readWrite;
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends DirectBean> beanType() {
        return (Class<? extends DirectBean>) super.beanType();
    }

    @Override
    public Class<P> propertyType() {
        return propertyType;
    }

    @Override
    public PropertyReadWrite readWrite() {
        return readWrite;
    }

    //-----------------------------------------------------------------------
    @Override
    @SuppressWarnings("unchecked")
    public P get(Bean bean) {
        if (readWrite.isReadable() == false) {
            throw new UnsupportedOperationException("Property cannot be read: " + name());
        }
        return (P) beanType().cast(bean).propertyGet(name());
    }

    @Override
    public void set(Bean bean, P value) {
        if (readWrite.isWritable() == false) {
            throw new UnsupportedOperationException("Property cannot be written: " + name());
        }
        beanType().cast(bean).propertySet(name(), value);
    }

}
