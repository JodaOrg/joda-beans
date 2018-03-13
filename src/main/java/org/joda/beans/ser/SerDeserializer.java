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
package org.joda.beans.ser;

import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

/**
 * Assists with deserialization allowing migration of data from an old data format to a new one.
 * <p>
 * This allows beans stored under an old version to be read in by a newer version.
 * <p>
 * Methods are called in order as follows:
 * <ol>
 * <li>{@code lookupMetaBean}
 * <li>{@code createBuilder}
 * <li>{@code lookupMetaProperty}, then {@code setValue} - once per property
 * <li>{@code build}
 * </ol>
 * <p>
 * A renamed property can be handled by overriding the {@code lookupMetaProperty}:
 * <pre>
 *  public MetaProperty&lt;?&gt; findMetaProperty(Class&lt;?&gt; beanType, MetaBean metaBean, String propertyName) {
 *    if ("firstName".equals(propertyName)) {
 *      return metaBean.metaProperty("forename");
 *    }
 *    return super.findMetaProperty(beanType, metaBean, propertyName);
 *  }
 * </pre>
 * <p>
 * A property type change can be handled by overriding the {@code lookupMetaProperty}
 * and {@code setValue}:
 * <pre>
 *  private MetaProperty<String> NUMBER_OF_CARS_STRING =
 *    StandaloneMetaProperty.of("numberOfCars", SimplePerson.meta(), String.class);
 *  
 *  public MetaProperty&lt;?&gt; findMetaProperty(Class&lt;?&gt; beanType, MetaBean metaBean, String propertyName) {
 *    if ("numberOfCars".equals(propertyName)) {
 *      return NUMBER_OF_CARS_STRING;  // replica of the old property
 *    }
 *    return super.findMetaProperty(beanType, metaBean, propertyName);
 *  }
 *
 *  public void setValue(BeanBuilder builder, MetaProperty metaProp, Object value) {
 *    if (metaProp == NUMBER_OF_CARS_STRING &amp;&amp; value != null) {
 *      String oldValue = value.toString();
 *      switch (oldValue) {
 *        case "One": value = 1; break;
 *        case "Two": value = 2; break;
 *        case "Lots": value = 3; break;
 *        default: value = 0; break;
 *      }
 *    }
 *    super.setValue(builder, metaProp, value);
 *  }
 * </pre>
 * <p>
 * A semantic change can be handled by overriding the {@code createBuilder}
 * and {@code build}, buffering the input to process at the end of the bean:
 * <pre>
 *  public BeanBuilder createBuilder(Class beanType, MetaBean metaBean) {
 *    return BufferingBeanBuilder.of(metaBean);
 *  }
 *
 *  public Object build(Class&lt;?&gt; beanType, BeanBuilder&lt;?&gt; builder) {
 *    BufferingBeanBuilder&lt;?&gt; bld = (BufferingBeanBuilder&lt;?&gt;) builder;
 *    if ("Stephen".equals(bld.getBuffer().get(SimplePerson.meta().forename())) &amp;&amp;
 *         "Colebourne".equals(bld.getBuffer().get(SimplePerson.meta().surname()))) {
 *      bld.set(SimplePerson.meta().forename(), "Steve");
 *    }
 *    return bld.build();
 *  }
 * </pre>
 *
 * @author Stephen Colebourne
 */
public interface SerDeserializer {

    /**
     * Lookup the meta-bean for the speecified type.
     * <p>
     * If the type is not a bean, then null may be returned.
     * 
     * @param beanType  the type being processed, not null
     * @return the meta-bean, null if not a bean type
     */
    public abstract MetaBean findMetaBean(Class<?> beanType);

    /**
     * Creates the stateful builder that captures state as the parse progresses.
     * <p>
     * This is normally a {@code BeanBuilder} however any type may be returned.
     * 
     * @param beanType  the type being processed, not null
     * @param metaBean  the meta-bean, null if not a bean type
     * @return the builder, null if not interested in the parse progress
     */
    public abstract BeanBuilder<?> createBuilder(Class<?> beanType, MetaBean metaBean);

    /**
     * Lookup the meta-property for the specified property name.
     * <p>
     * Return null if a property has been deleted, which will cause the parser
     * to discard the property.
     * <p>
     * Return a non-null meta-property to parse the property.
     * If the property was renamed, or had a type change, then the meta-property
     * should match the property as originally stored.
     * 
     * @param beanType  the type being processed, not null
     * @param metaBean  the meta-bean, null if not a bean type
     * @param propertyName  the property name being parsed, not null
     * @return the meta-property, null to ignore the property
     */
    public abstract MetaProperty<?> findMetaProperty(Class<?> beanType, MetaBean metaBean, String propertyName);

    /**
     * Sets the parsed value into the builder.
     * 
     * @param builder  the builder, null if not interested in the parse progress
     * @param metaProp  the meta-property, not null
     * @param value  the parsed value, may be null
     */
    public abstract void setValue(BeanBuilder<?> builder, MetaProperty<?> metaProp, Object value);

    /**
     * Builds the resulting object.
     * <p>
     * This method finishes the builder and returns the final object.
     * The migrator could validate or manipulate data here once all data is parsed,
     * for example to default a missing field.
     * 
     * @param beanType  the type being processed, not null
     * @param builder  the builder, null if not interested in the parse progress
     * @return the final built object, may be null
     */
    public abstract Object build(Class<?> beanType, BeanBuilder<?> builder);

}
