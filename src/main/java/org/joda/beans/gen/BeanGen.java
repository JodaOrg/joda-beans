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
package org.joda.beans.gen;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.TypedMetaBean;
import org.joda.beans.impl.BasicBeanBuilder;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.joda.beans.impl.direct.DirectPrivateBeanBuilder;
import org.joda.beans.impl.direct.MinimalMetaBean;
import org.joda.beans.impl.light.LightMetaBean;

/**
 * Code generator for a bean.
 * 
 * @author Stephen Colebourne
 */
class BeanGen {

    /** Constructor style for none. */
    static final int CONSTRUCTOR_NONE = 0;
    /** Constructor style for builder-based. */
    static final int CONSTRUCTOR_BY_BUILDER = 1;
    /** Constructor style for argument-based. */
    static final int CONSTRUCTOR_BY_ARGS = 2;
    /** Class constant, avoiding module dependency in Java 9. */
    private static final Class<?> CLASS_CONSTRUCTOR_PROPERTIES;
    /** Class constant, avoiding module dependency in Java 9. */
    private static final Class<?> CLASS_PROPERTY_CHANGE_SUPPORT;
    static {
        Class<?> cls1 = null;
        Class<?> cls2 = null;
        try {
            cls1 = Class.forName("java.beans.ConstructorProperties");
            cls2 = Class.forName("java.beans.PropertyChangeSupport");
        } catch (ClassNotFoundException ex) {
            // ignore
        }
        CLASS_CONSTRUCTOR_PROPERTIES = cls1;
        CLASS_PROPERTY_CHANGE_SUPPORT = cls2;
    }
    /** Line separator. */
    private static final String LINE_SEPARATOR = "\t//-----------------------------------------------------------------------";
    /** Line separator. */
    private static final String LINE_SEPARATOR_INDENTED = "\t\t//-----------------------------------------------------------------------";
    /** Types with primitive equals. */
    private static final Set<String> PRIMITIVE_EQUALS = new HashSet<>();
    static {
        PRIMITIVE_EQUALS.add("boolean");
        PRIMITIVE_EQUALS.add("char");
        PRIMITIVE_EQUALS.add("byte");
        PRIMITIVE_EQUALS.add("short");
        PRIMITIVE_EQUALS.add("int");
        PRIMITIVE_EQUALS.add("long");
        // not float or double, as Double.equals is not the same as double ==
    }

    /** The content to process. */
    private final File file;
    /** The content to process. */
    private final List<String> content;
    /** The config. */
    private final BeanGenConfig config;
    /** The data model of the bean. */
    private final BeanData data;
    /** The list of property generators. */
    private final List<PropertyGen> properties;
    /** The region to insert into. */
    private final List<String> insertRegion;

    /**
     * Constructor used when file is not a bean.
     * @param file  the file, not null
     * @param content  the content to process, not null
     * @param config  the config to use, not null
     */
    BeanGen(File file, List<String> content, BeanGenConfig config) {
        this.file = file;
        this.content = content;
        this.config = config;
        this.data = null;
        this.properties = null;
        this.insertRegion = null;
    }

    /**
     * Constructor used when file is a parsed bean.
     * @param file  the file, not null
     * @param content  the content to process, not null
     * @param config  the config to use, not null
     * @param data  the parsed data
     * @param properties  the parsed properties
     * @param autoEndIndex  the start of the autogen area
     * @param autoStartIndex   the end of the autogen area
     */
    BeanGen(
            File file, List<String> content, BeanGenConfig config,
            BeanData data, List<PropertyGen> properties, int autoStartIndex, int autoEndIndex) {
        this.file = file;
        this.content = content;
        this.config = config;
        this.data = data;
        this.properties = properties;
        this.insertRegion = content.subList(autoStartIndex + 1, autoEndIndex);
    }

    //-----------------------------------------------------------------------
    void process() {
        if (insertRegion != null) {
            data.ensureImport(BeanDefinition.class);
            if (properties.size() > 0) {
                data.ensureImport(PropertyDefinition.class);
            }
            removeOld();
            if (data.isRootClass() && data.isExtendsDirectBean()) {
                data.ensureImport(DirectBean.class);
            }
            generateMeta();
            generateSerializationVersionId();
            generatePropertyChangeSupportField();
            generateHashCodeField();
            generateFactory();
            generateImmutableBuilderMethod();
            generateArgBasedConstructor();
            generateBuilderBasedConstructor();
            generateMetaBean();
            generateGettersSetters();
            generateSeparator();
            generateImmutableToBuilder();
            generateClone();
            generateEquals();
            generateHashCode();
            generateToString();
            generateMetaClass();
            generateBuilderClass();
            resolveImports();
            resolveIndents();
        }
    }

    private void resolveImports() {
        if (data.getNewImports().size() > 0) {
            int pos = data.getImportInsertLocation() + 1;
            for (String imp : data.getNewImports()) {
                content.add(pos++, "import " + imp + ";");
            }
        }
    }

    private void resolveIndents() {
        for (ListIterator<String> it = content.listIterator(); it.hasNext(); ) {
            it.set(it.next().replace("\t", config.getIndent()));
        }
    }

    private void removeOld() {
        insertRegion.clear();
    }

    //-----------------------------------------------------------------------
    private void generateSeparator() {
        if (insertRegion.size() > 0 && insertRegion.get(insertRegion.size() - 1).equals(LINE_SEPARATOR)) {
            return;
        }
        insertRegion.add(LINE_SEPARATOR);
    }

    private void generateIndentedSeparator() {
        if (insertRegion.size() > 0 && insertRegion.get(insertRegion.size() - 1).equals(LINE_SEPARATOR_INDENTED)) {
            return;
        }
        insertRegion.add(LINE_SEPARATOR_INDENTED);
    }

    private void generateFactory() {
        if (data.isFactoryRequired()) {
            List<PropertyGen> nonDerived = nonDerivedProperties();
            insertRegion.add("\t/**");
            insertRegion.add("\t * Obtains an instance.");
            if (nonDerived.size() > 0) {
                if (data.isTypeGeneric()) {
                    for (int j = 0; j < data.getTypeGenericCount(); j++) {
                        insertRegion.add("\t * @param " + data.getTypeGenericName(j, true) + "  the type");
                    }
                }
                for (int i = 0; i < nonDerived.size(); i++) {
                    PropertyData prop = nonDerived.get(i).getData();
                    insertRegion.add("\t * @param " + prop.getPropertyName() + "  the value of the property" + prop.getNotNullJavadoc());
                }
            }
            insertRegion.add("\t * @return the instance");
            insertRegion.add("\t */");
            if (nonDerived.isEmpty()) {
                insertRegion.add("\tpublic static " + data.getTypeNoExtends() + " " + data.getFactoryName() + "() {");
                insertRegion.add("\t\treturn new " + data.getTypeNoExtends() + "();");
                
            } else {
                if (data.isTypeGeneric()) {
                    insertRegion.add("\tpublic static " + data.getTypeGeneric(true) + " " +
                                    data.getTypeNoExtends() + " " + data.getFactoryName() + "(");
                } else {
                    insertRegion.add("\tpublic static " + data.getTypeNoExtends() + " " + data.getFactoryName() + "(");
                }
                for (int i = 0; i < nonDerived.size(); i++) {
                    PropertyGen prop = nonDerived.get(i);
                    insertRegion.add("\t\t\t" + prop.getBuilderType() + " " + prop.getData().getPropertyName() + (i < nonDerived.size() - 1 ? "," : ") {"));
                }
                insertRegion.add("\t\treturn new " + data.getTypeWithDiamond() + "(");
                for (int i = 0; i < nonDerived.size(); i++) {
                    insertRegion.add("\t\t\t" + nonDerived.get(i).generateBuilderFieldName() + (i < nonDerived.size() - 1 ? "," : ");"));
                }
            }
            insertRegion.add("\t}");
            insertRegion.add("");
        }
    }

    private void generateImmutableBuilderMethod() {
        if (data.isConstructable() &&
                ((data.isImmutable() && data.isEffectiveBuilderScopeVisible()) || (data.isMutable() && data.isBuilderScopeVisible()))) {
            insertRegion.add("\t/**");
            insertRegion.add("\t * Returns a builder used to create an instance of the bean.");
            if (data.isTypeGeneric()) {
                for (int j = 0; j < data.getTypeGenericCount(); j++) {
                    insertRegion.add("\t * @param " + data.getTypeGenericName(j, true) + "  the type");
                }
            }
            insertRegion.add("\t * @return the builder, not null");
            insertRegion.add("\t */");
            if (data.isTypeGeneric()) {
                insertRegion.add("\t" + data.getEffectiveBuilderScope() + "static " + data.getTypeGeneric(true) +
                                " " + data.getTypeRaw() + ".Builder" + data.getTypeGenericName(true) + " builder() {");
            } else {
                insertRegion.add("\t" + data.getEffectiveBuilderScope() + "static " + data.getTypeRaw() + ".Builder builder() {");
            }
            insertRegion.add("\t\treturn new " + data.getTypeRaw() + ".Builder" + data.getTypeGenericDiamond() + "();");
            insertRegion.add("\t}");
            insertRegion.add("");
        }
    }

    private void generateBuilderBasedConstructor() {
        if (data.getConstructorStyle() == CONSTRUCTOR_BY_BUILDER && data.getImmutableConstructor() == CONSTRUCTOR_NONE && 
                ((data.isMutable() && data.isBuilderScopeVisible()) || data.isImmutable())) {
            List<PropertyGen> nonDerived = nonDerivedProperties();
            String scope = (data.isTypeFinal() ? "private" : "protected");
            // signature
            insertRegion.add("\t/**");
            insertRegion.add("\t * Restricted constructor.");
            insertRegion.add("\t * @param builder  the builder to copy from, not null");
            insertRegion.add("\t */");
            insertRegion.add("\t" + scope + " " + data.getTypeRaw() + "(" + data.getTypeRaw() + ".Builder" + data.getTypeGenericName(true) + " builder) {");
            // super
            if (data.isSubClass()) {
                insertRegion.add("\t\tsuper(builder);");
            }
            // validate
            for (PropertyGen prop : properties) {
                if (prop.getData().isValidated()) {
                    insertRegion.add("\t\t" + prop.getData().getValidationMethodName() +
                            "(builder." + prop.generateBuilderFieldName() +
                            ", \"" + prop.getData().getPropertyName() + "\");");
                }
            }
            // assign
            if (data.isImmutable()) {
                // assign
                for (int i = 0; i < nonDerived.size(); i++) {
                    insertRegion.addAll(nonDerived.get(i).generateConstructorAssign("builder."));
                }
            } else {
                for (int i = 0; i < nonDerived.size(); i++) {
                    PropertyGen propGen = nonDerived.get(i);
                    PropertyData prop = propGen.getData();
                    if (prop.isCollectionType()) {
                        if (prop.isNotNull()) {
                            insertRegion.add("\t\tthis." + prop.getPropertyName() + ".addAll(builder." + propGen.generateBuilderFieldName() + ");");
                        } else {
                            insertRegion.add("\t\tthis." + prop.getPropertyName() + " = builder." + propGen.generateBuilderFieldName() + ";");
                        }
                    } else if (prop.isMapType()) {
                        if (prop.isNotNull()) {
                            insertRegion.add("\t\tthis." + prop.getPropertyName() + ".putAll(builder." + propGen.generateBuilderFieldName() + ");");
                        } else {
                            insertRegion.add("\t\tthis." + prop.getPropertyName() + " = builder." + propGen.generateBuilderFieldName() + ";");
                        }
                    } else {
                        insertRegion.add("\t\tthis." + prop.getPropertyName() + " = builder." + propGen.generateBuilderFieldName() + ";");
                    }
                }
            }
            if (data.getImmutableValidator() != null) {
                insertRegion.add("\t\t" + data.getImmutableValidator() + "();");
            }
            insertRegion.add("\t}");
            insertRegion.add("");
        }
    }

    private void generateArgBasedConstructor() {
        if (data.getConstructorStyle() == CONSTRUCTOR_BY_ARGS && data.getImmutableConstructor() == CONSTRUCTOR_NONE && 
                ((data.isMutable() && (data.isBuilderScopeVisible() || data.isBeanStyleLight())) || data.isImmutable())) {
            String scope = data.getEffectiveConstructorScope();
            boolean generateAnnotation = data.isConstructorPropertiesAnnotation();
            boolean generateJavadoc = !"private ".equals(scope);
            List<PropertyGen> nonDerived = nonDerivedProperties();
            if (nonDerived.size() == 0) {
                if (generateJavadoc) {
                    insertRegion.add("\t/**");
                    insertRegion.add("\t * Creates an instance.");
                    insertRegion.add("\t */");
                }
                if (generateAnnotation) {
                    data.ensureImport(CLASS_CONSTRUCTOR_PROPERTIES);
                    insertRegion.add("\t@ConstructorProperties({})");
                }
                insertRegion.add("\t" + scope + data.getTypeRaw() + "() {");
            } else {
                // signature
                if (generateJavadoc) {
                    insertRegion.add("\t/**");
                    insertRegion.add("\t * Creates an instance.");
                    for (int i = 0; i < nonDerived.size(); i++) {
                        PropertyData prop = nonDerived.get(i).getData();
                        insertRegion.add("\t * @param " + prop.getPropertyName() + "  the value of the property" + prop.getNotNullJavadoc());
                    }
                    insertRegion.add("\t */");
                }
                if (generateAnnotation) {
                    data.ensureImport(CLASS_CONSTRUCTOR_PROPERTIES);
                    StringBuilder buf = new StringBuilder();
                    for (int i = 0; i < nonDerived.size(); i++) {
                        if (i > 0) {
                            buf.append(", ");
                        }
                        buf.append('"').append(nonDerived.get(i).getData().getPropertyName()).append('"');
                    }
                    insertRegion.add("\t@ConstructorProperties({" + buf.toString() + "})");
                }
                insertRegion.add("\t" + scope + data.getTypeRaw() + "(");
                for (int i = 0; i < nonDerived.size(); i++) {
                    PropertyGen prop = nonDerived.get(i);
                    insertRegion.add("\t\t\t" + prop.getBuilderType() + " " + prop.getData().getPropertyName() + (i < nonDerived.size() - 1 ? "," : ") {"));
                }
                // validate (mutable light beans call setters which validate)
                if (!(data.isMutable() && data.isBeanStyleLight())) {
                    for (PropertyGen prop : properties) {
                        if (prop.getData().isValidated()) {
                            insertRegion.add("\t\t" + prop.getData().getValidationMethodName() +
                                    "(" + prop.getData().getPropertyName() +
                                    ", \"" + prop.getData().getPropertyName() + "\");");
                        }
                    }
                }
                // assign
                for (int i = 0; i < nonDerived.size(); i++) {
                    PropertyGen prop = nonDerived.get(i);
                    if (data.isMutable() && data.isBeanStyleLight()) {
                        String generateSetInvoke = prop.getData().getSetterGen().generateSetInvoke(
                                prop.getData(), prop.getData().getPropertyName());
                        insertRegion.add("\t\t" + generateSetInvoke + ";");
                    } else {
                        insertRegion.addAll(prop.generateConstructorAssign(""));
                    }
                }
            }
            if (data.getImmutableValidator() != null) {
                insertRegion.add("\t\t" + data.getImmutableValidator() + "();");
            }
            insertRegion.add("\t}");
            insertRegion.add("");
        }
    }

    //-----------------------------------------------------------------------
    private void generateMeta() {
        if (data.isBeanStyleLightOrMinimal()) {
            insertRegion.add("\t/**");
            insertRegion.add("\t * The meta-bean for {@code " + data.getTypeRaw() + "}.");
            insertRegion.add("\t */");
            boolean genericProps = data.getProperties().stream()
                    .filter(p -> p.isGeneric())
                    .findAny()
                    .isPresent();
            boolean unchecked = data.isBeanStyleMinimal() && data.isMutable() && genericProps;
            unchecked |= data.isBeanStyleMinimal() && data.isTypeGeneric() && !data.isSkipBuilderGeneration();
            boolean rawtypes = data.isBeanStyleMinimal() && data.isTypeGeneric();
            if (unchecked && rawtypes) {
                insertRegion.add("\t@SuppressWarnings({\"unchecked\", \"rawtypes\" })");
            } else if (rawtypes) {
                insertRegion.add("\t@SuppressWarnings(\"rawtypes\")");
            } else if (unchecked) {
                insertRegion.add("\t@SuppressWarnings(\"unchecked\")");
            }
            if (data.isTypeGeneric()) {
                data.ensureImport(MetaBean.class);
                insertRegion.add("\tprivate static final MetaBean META_BEAN =");
            } else {
                data.ensureImport(TypedMetaBean.class);
                insertRegion.add("\tprivate static final TypedMetaBean<" + data.getTypeNoExtends() + "> META_BEAN =");
            }
            List<PropertyGen> nonDerived = nonDerivedProperties();
            if (data.isBeanStyleLight()) {
                // light
                data.ensureImport(LightMetaBean.class);
                data.ensureImport(MethodHandles.class);
                boolean specialInit = nonDerived.stream().filter(p -> p.isSpecialInit()).findAny().isPresent();
                if (nonDerived.isEmpty() || !specialInit) {
                    insertRegion.add("\t\t\tLightMetaBean.of(" + data.getTypeRaw() + ".class, MethodHandles.lookup());");
                } else {
                    insertRegion.add("\t\t\tLightMetaBean.of(");
                    insertRegion.add("\t\t\t\t\t" + data.getTypeRaw() + ".class,");
                    insertRegion.add("\t\t\t\t\tMethodHandles.lookup(),");
                    for (int i = 0; i < nonDerived.size(); i++) {
                        insertRegion.add(
                                "\t\t\t\t\t" + nonDerived.get(i).generateInit() + (i < nonDerived.size() - 1 ? "," : ");"));
                    }
                }
            } else {
                data.ensureImport(MinimalMetaBean.class);
                insertRegion.add("\t\t\tMinimalMetaBean.of(");
                insertRegion.add("\t\t\t\t\t" + data.getTypeRaw() + ".class,");
                String builderLambda = "\t\t\t\t\t() -> new " + data.getTypeRaw() + ".Builder()";
                if (data.isSkipBuilderGeneration()) {
                    data.ensureImport(BasicBeanBuilder.class);
                    builderLambda = "\t\t\t\t\t() -> new BasicBeanBuilder<>(new " + data.getTypeWithDiamond() + "())";
                }
                if (nonDerived.isEmpty()) {
                    if (data.isImmutable()) {
                        insertRegion.add(builderLambda + ");");
                    } else {
                        data.ensureImport(Collections.class);
                        data.ensureImport(Function.class);
                        data.ensureImport(BiConsumer.class);
                        insertRegion.add(builderLambda + ",");
                        insertRegion.add("\t\t\t\t\tCollections.<Function<" + data.getTypeRaw() + ", Object>>emptyList(),");
                        insertRegion.add("\t\t\t\t\tCollections.<BiConsumer<" + data.getTypeRaw() + ", Object>>emptyList());");
                    }
                } else {
                    insertRegion.add(builderLambda + ",");
                    if (data.isImmutable()) {
                        for (int i = 0; i < nonDerived.size(); i++) {
                            if (i < nonDerived.size() - 1) {
                                insertRegion.add("\t\t\t\t\t" + nonDerived.get(i).generateLambdaGetter() + ",");
                            } else {
                                insertRegion.add("\t\t\t\t\t" + nonDerived.get(i).generateLambdaGetter() + ");");
                            }
                        }
                    } else {
                        data.ensureImport(Arrays.class);
                        data.ensureImport(Function.class);
                        data.ensureImport(BiConsumer.class);
                        insertRegion.add("\t\t\t\t\tArrays.<Function<" + data.getTypeRaw() + ", Object>>asList(");
                        for (int i = 0; i < nonDerived.size(); i++) {
                            if (i < nonDerived.size() - 1) {
                                insertRegion.add("\t\t\t\t\t\t\t" + nonDerived.get(i).generateLambdaGetter() + ",");
                            } else {
                                insertRegion.add("\t\t\t\t\t\t\t" + nonDerived.get(i).generateLambdaGetter() + "),");
                            }
                        }
                        insertRegion.add("\t\t\t\t\tArrays.<BiConsumer<" + data.getTypeRaw() + ", Object>>asList(");
                        for (int i = 0; i < nonDerived.size(); i++) {
                            if (i < nonDerived.size() - 1) {
                                insertRegion.add("\t\t\t\t\t\t\t" + nonDerived.get(i).generateLambdaSetter() + ",");
                            } else {
                                insertRegion.add("\t\t\t\t\t\t\t" + nonDerived.get(i).generateLambdaSetter() + "));");
                            }
                        }
                    }
                }
            }
            insertRegion.add("");
            insertRegion.add("\t/**");
            insertRegion.add("\t * The meta-bean for {@code " + data.getTypeRaw() + "}.");
            insertRegion.add("\t * @return the meta-bean, not null");
            insertRegion.add("\t */");
            if (data.isTypeGeneric()) {
                insertRegion.add("\tpublic static MetaBean meta() {");
            } else {
                insertRegion.add("\tpublic static TypedMetaBean<" + data.getTypeNoExtends() + "> meta() {");
            }
            insertRegion.add("\t\treturn META_BEAN;");
            insertRegion.add("\t}");
            insertRegion.add("");
            insertRegion.add("\tstatic {");
            data.ensureImport(MetaBean.class);
            insertRegion.add("\t\tMetaBean.register(META_BEAN);");
            insertRegion.add("\t}");
            insertRegion.add("");
            
        } else {
            // this cannot be generified without either Eclipse or javac complaining
            // raw types forever
            insertRegion.add("\t/**");
            insertRegion.add("\t * The meta-bean for {@code " + data.getTypeRaw() + "}.");
            insertRegion.add("\t * @return the meta-bean, not null");
            if (data.isMetaScopePrivate()) {
                data.ensureImport(MetaBean.class);
                insertRegion.add("\t */");
                insertRegion.add("\tpublic static MetaBean meta() {");
            } else if (data.isTypeGeneric()) {
                insertRegion.add("\t */");
                insertRegion.add("\t@SuppressWarnings(\"rawtypes\")");
                insertRegion.add("\tpublic static " + data.getTypeRaw() + ".Meta meta() {");
            } else {
                insertRegion.add("\t */");
                insertRegion.add("\tpublic static " + data.getTypeRaw() + ".Meta meta() {");
            }
            insertRegion.add("\t\treturn " + data.getTypeRaw() + ".Meta.INSTANCE;");
            insertRegion.add("\t}");
            
            if (data.isTypeGeneric()) {
                generateMetaForGenericType();
            }
            
            insertRegion.add("");
            insertRegion.add("\tstatic {");
            data.ensureImport(MetaBean.class);
            insertRegion.add("\t\tMetaBean.register(" + data.getTypeRaw() + ".Meta.INSTANCE);");
            insertRegion.add("\t}");
            insertRegion.add("");
        }
    }

    private void generateMetaForGenericType() {
        // this works around an Eclipse bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=397462
        // long name needed for uniqueness as static overriding is borked
        insertRegion.add("");
        insertRegion.add("\t/**");
        insertRegion.add("\t * The meta-bean for {@code " + data.getTypeRaw() + "}.");
        if (data.getTypeGenericCount() == 1) {
            insertRegion.add("\t * @param <R>  the bean's generic type");
            insertRegion.add("\t * @param cls  the bean's generic type");
        } else if (data.getTypeGenericCount() == 2) {
            insertRegion.add("\t * @param <R>  the first generic type");
            insertRegion.add("\t * @param <S>  the second generic type");
            insertRegion.add("\t * @param cls1  the first generic type");
            insertRegion.add("\t * @param cls2  the second generic type");
        } else if (data.getTypeGenericCount() == 3) {
            insertRegion.add("\t * @param <R>  the first generic type");
            insertRegion.add("\t * @param <S>  the second generic type");
            insertRegion.add("\t * @param <T>  the second generic type");
            insertRegion.add("\t * @param cls1  the first generic type");
            insertRegion.add("\t * @param cls2  the second generic type");
            insertRegion.add("\t * @param cls3  the third generic type");
        }
        insertRegion.add("\t * @return the meta-bean, not null");
        insertRegion.add("\t */");
        insertRegion.add("\t@SuppressWarnings(\"unchecked\")");
        String[] typeNames = new String[] {"R", "S", "T"};
        if (data.getTypeGenericCount() == 1) {
            insertRegion.add("\tpublic static <R" + data.getTypeGenericExtends(0, typeNames) +
                    "> " + data.getTypeRaw() + ".Meta<R> meta" + data.getTypeRaw() + "(Class<R> cls) {");
        } else if (data.getTypeGenericCount() == 2) {
            insertRegion.add("\tpublic static <R" + data.getTypeGenericExtends(0, typeNames) +
                    ", S" + data.getTypeGenericExtends(1, typeNames) + "> " + data.getTypeRaw() +
                    ".Meta<R, S> meta" + data.getTypeRaw() + "(Class<R> cls1, Class<S> cls2) {");
        } else if (data.getTypeGenericCount() == 3) {
            insertRegion.add("\tpublic static <R" + data.getTypeGenericExtends(0, typeNames) +
                    ", S" + data.getTypeGenericExtends(1, typeNames) +
                    ", T" + data.getTypeGenericExtends(2, typeNames) +
                    "> " + data.getTypeRaw() +
                    ".Meta<R, S, T> meta" + data.getTypeRaw() + "(Class<R> cls1, Class<S> cls2, Class<T> cls3) {");
        }
        insertRegion.add("\t\treturn " + data.getTypeRaw() + ".Meta.INSTANCE;");
        insertRegion.add("\t}");
    }

    private void generateSerializationVersionId() {
        if (data.isSerializable() && !data.isManualSerializationId()) {
            insertRegion.add("\t/**");
            insertRegion.add("\t * The serialization version id.");
            insertRegion.add("\t */");
            insertRegion.add("\tprivate static final long serialVersionUID = 1L;");
            insertRegion.add("");
        }
    }

    private void generatePropertyChangeSupportField() {
        if (data.isPropertyChangeSupport()) {
            data.ensureImport(CLASS_PROPERTY_CHANGE_SUPPORT);
            insertRegion.add("\t/**");
            insertRegion.add("\t * The property change support field.");
            insertRegion.add("\t */");
            insertRegion.add("\tprivate final transient PropertyChangeSupport " + config.getPrefix() + "propertyChangeSupport = new PropertyChangeSupport(this);");
            insertRegion.add("");
        }
    }

    private void generateHashCodeField() {
        if (data.isCacheHashCode()) {
            insertRegion.add("\t/**");
            insertRegion.add("\t * The cached hash code, using the racy single-check idiom.");
            insertRegion.add("\t */");
            insertRegion.add("\tprivate int " + config.getPrefix() + "cachedHashCode;");
            insertRegion.add("");
        }
    }

    private void generateMetaBean() {
        if (data.isMetaScopePrivate() || data.isBeanStyleMinimal()) {
            insertRegion.add("\t@Override");
            if (data.isBeanStyleLightOrMinimal()) {
                data.ensureImport(TypedMetaBean.class);
                if (data.isTypeGeneric()) {
                    insertRegion.add("\t@SuppressWarnings(\"unchecked\")");
                }
                insertRegion.add("\tpublic TypedMetaBean<" + data.getTypeNoExtends() + "> metaBean() {");
                if (data.isTypeGeneric()) {
                    insertRegion.add("\t\treturn (TypedMetaBean<" + data.getTypeNoExtends() + ">) META_BEAN;");
                } else {
                    insertRegion.add("\t\treturn META_BEAN;");
                }
            } else {
                data.ensureImport(MetaBean.class);
                insertRegion.add("\tpublic MetaBean metaBean() {");
                insertRegion.add("\t\treturn " + data.getTypeRaw() + ".Meta.INSTANCE;");
            }
            insertRegion.add("\t}");
            insertRegion.add("");
        } else {
            if (data.isTypeGeneric()) {
                insertRegion.add("\t@SuppressWarnings(\"unchecked\")");
            }
            insertRegion.add("\t@Override");
            insertRegion.add("\tpublic " + data.getTypeRaw() +
                    ".Meta" + data.getTypeGenericName(true) + " metaBean() {");
            insertRegion.add("\t\treturn " + data.getTypeRaw() + ".Meta.INSTANCE;");
            insertRegion.add("\t}");
            insertRegion.add("");
        }
    }

    private void generateGettersSetters() {
        for (PropertyGen prop : properties) {
            generateSeparator();
            insertRegion.addAll(prop.generateGetter());
            if (data.isMutable()) {
                insertRegion.addAll(prop.generateSetter());
            }
            if (data.isBeanStyleGenerateProperties()) {
                insertRegion.addAll(prop.generateProperty());
            }
        }
    }

    //-----------------------------------------------------------------------
    private void generateImmutableToBuilder() {
        if (data.isImmutable() && data.isEffectiveBuilderScopeVisible()) {
            if (data.isConstructable()) {
                List<PropertyGen> nonDerived = nonDerivedProperties();
                if (nonDerived.size() > 0) {
                    insertRegion.add("\t/**");
                    insertRegion.add("\t * Returns a builder that allows this bean to be mutated.");
                    insertRegion.add("\t * @return the mutable builder, not null");
                    insertRegion.add("\t */");
                    if (data.isRootClass() == false) {
                        insertRegion.add("\t@Override");
                    }
                    insertRegion.add("\t" + data.getEffectiveBuilderScope() + "Builder" + data.getTypeGenericName(true) + " toBuilder() {");
                    insertRegion.add("\t\treturn new Builder" + data.getTypeGenericDiamond() + "(this);");
                    insertRegion.add("\t}");
                    insertRegion.add("");
                }
            } else {
                insertRegion.add("\t/**");
                insertRegion.add("\t * Returns a builder that allows this bean to be mutated.");
                insertRegion.add("\t * @return the mutable builder, not null");
                insertRegion.add("\t */");
                if (data.isRootClass() == false) {
                    insertRegion.add("\t@Override");
                }
                insertRegion.add("\tpublic abstract Builder" + data.getTypeGenericName(true) + " toBuilder();");
                insertRegion.add("");
            }
        }
    }

    private void generateClone() {
        if (data.isSkipCloneGeneration() ||
                data.isManualClone() ||
                (data.isRootClass() == false && data.isConstructable() == false)) {
            return;
        }
        insertRegion.add("\t@Override");
        if (data.isImmutable()) {
            insertRegion.add("\tpublic " + data.getTypeNoExtends() + " clone() {");
            insertRegion.add("\t\treturn this;");
        } else {
            data.ensureImport(JodaBeanUtils.class);
            insertRegion.add("\tpublic " + data.getTypeNoExtends() + " clone() {");
            insertRegion.add("\t\treturn JodaBeanUtils.cloneAlways(this);");
        }
        insertRegion.add("\t}");
        insertRegion.add("");
    }

    private void generateEquals() {
        if (data.isManualEqualsHashCode()) {
            return;
        }
        insertRegion.add("\t@Override");
        insertRegion.add("\tpublic boolean equals(Object obj) {");
        insertRegion.add("\t\tif (obj == this) {");
        insertRegion.add("\t\t\treturn true;");
        insertRegion.add("\t\t}");
        insertRegion.add("\t\tif (obj != null && obj.getClass() == this.getClass()) {");
        List<PropertyGen> nonDerived = nonDerivedEqualsHashCodeProperties();
        if (nonDerived.size() == 0) {
            if (data.isSubClass()) {
                insertRegion.add("\t\t\treturn super.equals(obj);");
            } else {
                insertRegion.add("\t\t\treturn true;");
            }
        } else {
            insertRegion.add("\t\t\t" + data.getTypeWildcard() + " other = (" + data.getTypeWildcard() + ") obj;");
            for (int i = 0; i < nonDerived.size(); i++) {
                PropertyGen prop = nonDerived.get(i);
                String getter = equalsHashCodeFieldAccessor(prop);
                data.ensureImport(JodaBeanUtils.class);
                String equals = "JodaBeanUtils.equal(" + getter + ", other." + getter + ")";
                if (PRIMITIVE_EQUALS.contains(prop.getData().getType())) {
                    equals = "(" + getter + " == other." + getter + ")";
                }
                insertRegion.add(
                        (i == 0 ? "\t\t\treturn " : "\t\t\t\t\t") + equals +
                        (data.isSubClass() || i < nonDerived.size() - 1 ? " &&" : ";"));
            }
            if (data.isSubClass()) {
                insertRegion.add("\t\t\t\t\tsuper.equals(obj);");
            }
        }
        insertRegion.add("\t\t}");
        insertRegion.add("\t\treturn false;");
        insertRegion.add("\t}");
        insertRegion.add("");
    }

    private void generateHashCode() {
        if (data.isManualEqualsHashCode()) {
            return;
        }
        insertRegion.add("\t@Override");
        insertRegion.add("\tpublic int hashCode() {");
        if (data.isCacheHashCode()) {
            insertRegion.add("\t\tint hash = " + config.getPrefix() + "cachedHashCode;");
            insertRegion.add("\t\tif (hash == 0) {");
            if (data.isSubClass()) {
                insertRegion.add("\t\t\thash = 7;");
            } else {
                insertRegion.add("\t\t\thash = getClass().hashCode();");
            }
            generateHashCodeContent("\t\t\t");
            if (data.isSubClass()) {
                insertRegion.add("\t\t\thash = hash ^ super.hashCode();");
            }
            insertRegion.add("\t\t\t" + config.getPrefix() + "cachedHashCode = hash;");
            insertRegion.add("\t\t}");
            insertRegion.add("\t\treturn hash;");
        } else {
            if (data.isSubClass()) {
                insertRegion.add("\t\tint hash = 7;");
            } else {
                insertRegion.add("\t\tint hash = getClass().hashCode();");
            }
            generateHashCodeContent("\t\t");
            if (data.isSubClass()) {
                insertRegion.add("\t\treturn hash ^ super.hashCode();");
            } else {
                insertRegion.add("\t\treturn hash;");
            }
        }
        insertRegion.add("\t}");
        insertRegion.add("");
    }

    private void generateHashCodeContent(String indent) {
        List<PropertyGen> nonDerived = nonDerivedEqualsHashCodeProperties();
        for (int i = 0; i < nonDerived.size(); i++) {
            PropertyGen prop = nonDerived.get(i);
            String getter = equalsHashCodeFieldAccessor(prop);
            data.ensureImport(JodaBeanUtils.class);
            insertRegion.add(indent + "hash = hash * 31 + JodaBeanUtils.hashCode(" + getter + ");");
        }
    }

    private String equalsHashCodeFieldAccessor(PropertyGen prop) {
        if (prop.getData().getEqualsHashCodeStyle().equals("field")) {
            return prop.getData().getFieldName();
        } else {
            return prop.getData().getGetterGen().generateGetInvoke(prop.getData());
        }
    }

    private void generateToString() {
        if (data.isManualToStringCode()) {
            return;
        }
        List<PropertyGen> props = toStringProperties();
        if (data.isRootClass() && data.isTypeFinal()) {
            insertRegion.add("\t@Override");
            insertRegion.add("\tpublic String toString() {");
            insertRegion.add("\t\tStringBuilder buf = new StringBuilder(" + (props.size() * 32 + 32) + ");");
            insertRegion.add("\t\tbuf.append(\"" + data.getTypeRaw() + "{\");");
            for (int i = 0; i < props.size(); i++) {
                PropertyGen prop = props.get(i);
                String getter = toStringFieldAccessor(prop);
                if (i < props.size() - 1) {
                    insertRegion.add("\t\tbuf.append(\"" + prop.getData().getPropertyName() +
                            "\").append('=').append(" + getter + ").append(',').append(' ');");
                } else {
                    data.ensureImport(JodaBeanUtils.class);
                    insertRegion.add("\t\tbuf.append(\"" + prop.getData().getPropertyName() +
                            "\").append('=').append(JodaBeanUtils.toString(" + getter + "));");
                }
            }
            insertRegion.add("\t\tbuf.append('}');");
            insertRegion.add("\t\treturn buf.toString();");
            insertRegion.add("\t}");
            insertRegion.add("");
            return;
        }
        
        insertRegion.add("\t@Override");
        insertRegion.add("\tpublic String toString() {");
        insertRegion.add("\t\tStringBuilder buf = new StringBuilder(" + (props.size() * 32 + 32) + ");");
        insertRegion.add("\t\tbuf.append(\"" + data.getTypeRaw() + "{\");");
        insertRegion.add("\t\tint len = buf.length();");
        insertRegion.add("\t\ttoString(buf);");
        insertRegion.add("\t\tif (buf.length() > len) {");
        insertRegion.add("\t\t\tbuf.setLength(buf.length() - 2);");
        insertRegion.add("\t\t}");
        insertRegion.add("\t\tbuf.append('}');");
        insertRegion.add("\t\treturn buf.toString();");
        insertRegion.add("\t}");
        insertRegion.add("");
        
        if (data.isSubClass()) {
            insertRegion.add("\t@Override");
        }
        insertRegion.add("\tprotected void toString(StringBuilder buf) {");
        if (data.isSubClass()) {
            insertRegion.add("\t\tsuper.toString(buf);");
        }
        for (int i = 0; i < props.size(); i++) {
            PropertyGen prop = props.get(i);
            String getter = toStringFieldAccessor(prop);
            data.ensureImport(JodaBeanUtils.class);
            insertRegion.add("\t\tbuf.append(\"" + prop.getData().getPropertyName() +
                    "\").append('=').append(JodaBeanUtils.toString(" + getter + ")).append(',').append(' ');");
        }
        insertRegion.add("\t}");
        insertRegion.add("");
    }

    private String toStringFieldAccessor(PropertyGen prop) {
        if (prop.getData().isDerived()) {
            return prop.getData().getGetterGen().generateGetInvoke(prop.getData());
        } else if (prop.getData().getToStringStyle().equals("field")) {
            return prop.getData().getFieldName();
        } else {
            return prop.getData().getGetterGen().generateGetInvoke(prop.getData());
        }
    }

    //-----------------------------------------------------------------------
    private void generateMetaClass() {
        if (data.isBeanStyleLightOrMinimal()) {
            return;
        }
        generateSeparator();
        insertRegion.add("\t/**");
        insertRegion.add("\t * The meta-bean for {@code " + data.getTypeRaw() + "}.");
        if (data.isTypeGeneric()) {
            for (int j = 0; j < data.getTypeGenericCount(); j++) {
                insertRegion.add("\t * @param " + data.getTypeGenericName(j, true) + "  the type");
            }
        }
        insertRegion.add("\t */");
        String superMeta;
        if (data.isSubClass()) {
            superMeta = data.getSuperTypeRaw() + ".Meta" + data.getSuperTypeGeneric(true);
        } else {
            data.ensureImport(DirectMetaBean.class);
            superMeta = "DirectMetaBean";
        }
        String finalType = data.isTypeFinal() ? "final " : "";
        if (data.isTypeGeneric()) {
            insertRegion.add("\t" + data.getEffectiveMetaScope() + "static " + finalType + 
                    "class Meta" + data.getTypeGeneric(true) + " extends " + superMeta + " {");
        } else {
            insertRegion.add("\t" + data.getEffectiveMetaScope() + "static " + finalType + 
                    "class Meta extends " + superMeta + " {");
        }
        insertRegion.add("\t\t/**");
        insertRegion.add("\t\t * The singleton instance of the meta-bean.");
        insertRegion.add("\t\t */");
        if (data.isTypeGeneric()) {
            insertRegion.add("\t\t@SuppressWarnings(\"rawtypes\")");
        }
        insertRegion.add("\t\tstatic final Meta INSTANCE = new Meta();");
        insertRegion.add("");
        generateMetaPropertyConstants();
        generateMetaPropertyMapSetup();
        insertRegion.add("\t\t/**");
        insertRegion.add("\t\t * Restricted constructor.");
        insertRegion.add("\t\t */");
        insertRegion.add("\t\t" + data.getNestedClassConstructorScope() + " Meta() {");
        insertRegion.add("\t\t}");
        insertRegion.add("");
        generateMetaPropertyGet();
        generateMetaBuilder();
        generateMetaBeanType();
        generateMetaPropertyMap();
        generateIndentedSeparator();
        generateMetaPropertyMethods();
        generateIndentedSeparator();
        generateMetaGetPropertyValue();
        generateMetaSetPropertyValue();
        generateMetaValidate();
        insertRegion.add("\t}");
        insertRegion.add("");
    }

    private void generateMetaPropertyConstants() {
        for (PropertyGen prop : properties) {
            insertRegion.addAll(prop.generateMetaPropertyConstant());
        }
    }

    private void generateMetaPropertyMapSetup() {
        data.ensureImport(MetaProperty.class);
        data.ensureImport(DirectMetaPropertyMap.class);
        insertRegion.add("\t\t/**");
        insertRegion.add("\t\t * The meta-properties.");
        insertRegion.add("\t\t */");
        insertRegion.add("\t\tprivate final Map<String, MetaProperty<?>> " + config.getPrefix() + "metaPropertyMap$ = new DirectMetaPropertyMap(");
        if (data.isSubClass()) {
            insertRegion.add("\t\t\t\tthis, (DirectMetaPropertyMap) super.metaPropertyMap()" + (properties.size() == 0 ? ");" : ","));
        } else {
            insertRegion.add("\t\t\t\tthis, null" + (properties.size() == 0 ? ");" : ","));
        }
        for (int i = 0; i < properties.size(); i++) {
            String line = "\t\t\t\t\"" + properties.get(i).getData().getPropertyName() + "\"";
            line += (i + 1 == properties.size() ? ");" : ",");
            insertRegion.add(line);
        }
        insertRegion.add("");
    }

    private void generateMetaBuilder() {
        if (!data.isConstructable()) {
            insertRegion.add("\t\t@Override");
            insertRegion.add("\t\tpublic boolean isBuildable() {");
            insertRegion.add("\t\t\treturn false;");
            insertRegion.add("\t\t}");
            insertRegion.add("");
        }
        insertRegion.add("\t\t@Override");
        if (data.isImmutable() && data.isEffectiveBuilderScopeVisible() == false) {
            data.ensureImport(BeanBuilder.class);
            insertRegion.add("\t\tpublic BeanBuilder<? extends " + data.getTypeNoExtends() + "> builder() {");
            if (data.isConstructable()) {
                insertRegion.add("\t\t\treturn new " + data.getTypeRaw() + ".Builder" + data.getTypeGenericDiamond() + "();");
            } else {
                insertRegion.add("\t\t\tthrow new UnsupportedOperationException(\"" + data.getTypeRaw() + " is an abstract class\");");
            }
        } else if (data.isImmutable() || (data.isMutable() && data.isBuilderScopeVisible())) {
            insertRegion.add("\t\tpublic " + data.getTypeRaw() + ".Builder" + data.getTypeGenericName(true) + " builder() {");
            if (data.isConstructable()) {
                insertRegion.add("\t\t\treturn new " + data.getTypeRaw() + ".Builder" + data.getTypeGenericDiamond() + "();");
            } else {
                insertRegion.add("\t\t\tthrow new UnsupportedOperationException(\"" + data.getTypeRaw() + " is an abstract class\");");
            }
        } else {
            data.ensureImport(BeanBuilder.class);
            insertRegion.add("\t\tpublic BeanBuilder<? extends " + data.getTypeNoExtends() + "> builder() {");
            if (data.isConstructable()) {
                data.ensureImport(DirectBeanBuilder.class);
                insertRegion.add("\t\t\treturn new DirectBeanBuilder<>(new " + data.getTypeNoExtends() + "());");
            } else {
                insertRegion.add("\t\t\tthrow new UnsupportedOperationException(\"" + data.getTypeRaw() + " is an abstract class\");");
            }
        }
        insertRegion.add("\t\t}");
        insertRegion.add("");
    }

    private void generateMetaBeanType() {
        if (data.isTypeGeneric()) {
            insertRegion.add("\t\t@SuppressWarnings({\"unchecked\", \"rawtypes\" })");
        }
        insertRegion.add("\t\t@Override");
        insertRegion.add("\t\tpublic Class<? extends " + data.getTypeNoExtends() + "> beanType() {");
        if (data.isTypeGeneric()) {
            insertRegion.add("\t\t\treturn (Class) " + data.getTypeRaw() + ".class;");
        } else {
            insertRegion.add("\t\t\treturn " + data.getTypeNoExtends() + ".class;");
        }
        insertRegion.add("\t\t}");
        insertRegion.add("");
    }

    private void generateMetaPropertyGet() {
        if (properties.size() > 0) {
            data.ensureImport(MetaProperty.class);
            insertRegion.add("\t\t@Override");
            insertRegion.add("\t\tprotected MetaProperty<?> metaPropertyGet(String propertyName) {");
            insertRegion.add("\t\t\tswitch (propertyName.hashCode()) {");
            for (PropertyGen prop : properties) {
                insertRegion.addAll(prop.generateMetaPropertyGetCase());
            }
            insertRegion.add("\t\t\t}");
            insertRegion.add("\t\t\treturn super.metaPropertyGet(propertyName);");
            insertRegion.add("\t\t}");
            insertRegion.add("");
        }
    }

    private void generateMetaPropertyMap() {
        data.ensureImport(Map.class);
        insertRegion.add("\t\t@Override");
        insertRegion.add("\t\tpublic Map<String, MetaProperty<?>> metaPropertyMap() {");
        insertRegion.add("\t\t\treturn " + config.getPrefix() + "metaPropertyMap$;");
        insertRegion.add("\t\t}");
        insertRegion.add("");
    }

    private void generateMetaPropertyMethods() {
        if (data.isBeanStyleGenerateMetaProperties()) {
            for (PropertyGen prop : properties) {
                insertRegion.addAll(prop.generateMetaProperty());
            }
        }
    }

    //-----------------------------------------------------------------------
    private void generateMetaGetPropertyValue() {
        if (properties.size() == 0) {
            return;
        }
        data.ensureImport(Bean.class);
        insertRegion.add("\t\t@Override");
        insertRegion.add("\t\tprotected Object propertyGet(Bean bean, String propertyName, boolean quiet) {");
        insertRegion.add("\t\t\tswitch (propertyName.hashCode()) {");
        for (PropertyGen prop : properties) {
            insertRegion.addAll(prop.generatePropertyGetCase());
        }
        insertRegion.add("\t\t\t}");
        insertRegion.add("\t\t\treturn super.propertyGet(bean, propertyName, quiet);");
        insertRegion.add("\t\t}");
        insertRegion.add("");
    }

    private void generateMetaSetPropertyValue() {
        if (properties.size() == 0) {
            return;
        }
        data.ensureImport(Bean.class);
        if (data.isImmutable()) {
            insertRegion.add("\t\t@Override");
            insertRegion.add("\t\tprotected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {");
            insertRegion.add("\t\t\tmetaProperty(propertyName);");
            insertRegion.add("\t\t\tif (quiet) {");
            insertRegion.add("\t\t\t\treturn;");
            insertRegion.add("\t\t\t}");
            insertRegion.add("\t\t\tthrow new UnsupportedOperationException(\"Property cannot be written: \" + propertyName);");
            insertRegion.add("\t\t}");
            insertRegion.add("");
            return;
        }
        
        boolean generics = false;
        for (PropertyData prop : data.getProperties()) {
            generics |= (prop.getStyle().isWritable() &&
                    ((prop.isGeneric() && prop.isGenericWildcardParamType() == false) || data.isTypeGeneric()));
        }
        if (generics) {
            insertRegion.add("\t\t@SuppressWarnings(\"unchecked\")");
        }
        insertRegion.add("\t\t@Override");
        insertRegion.add("\t\tprotected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {");
        insertRegion.add("\t\t\tswitch (propertyName.hashCode()) {");
        for (PropertyGen prop : properties) {
            insertRegion.addAll(prop.generatePropertySetCase());
        }
        insertRegion.add("\t\t\t}");
        insertRegion.add("\t\t\tsuper.propertySet(bean, propertyName, newValue, quiet);");
        insertRegion.add("\t\t}");
        insertRegion.add("");
    }

    private void generateMetaValidate() {
        if (data.isValidated() == false || data.isImmutable()) {
            return;
        }
        data.ensureImport(Bean.class);
        insertRegion.add("\t\t@Override");
        insertRegion.add("\t\tprotected void validate(Bean bean) {");
        if (data.isValidated()) {
            for (PropertyGen prop : properties) {
                if (prop.getData().isValidated()) {
                    insertRegion.add("\t\t\t" + prop.getData().getValidationMethodName() +
                            "(((" + data.getTypeWildcard() + ") bean)." + prop.getData().getFieldName() +
                            ", \"" + prop.getData().getPropertyName() + "\");");
                }
            }
        }
        if (data.isSubClass()) {
            insertRegion.add("\t\t\tsuper.validate(bean);");
        }
        insertRegion.add("\t\t}");
        insertRegion.add("");
    }

    //-----------------------------------------------------------------------
    private void generateBuilderClass() {
        if (data.isSkipBuilderGeneration()) {
            return;
        }
        List<PropertyGen> nonDerived = nonDerivedProperties();
        generateSeparator();
        String finalType = data.isTypeFinal() ? "final " : "";
        insertRegion.add("\t/**");
        insertRegion.add("\t * The bean-builder for {@code " + data.getTypeRaw() + "}.");
        if (data.isTypeGeneric()) {
            for (int j = 0; j < data.getTypeGenericCount(); j++) {
                insertRegion.add("\t * @param " + data.getTypeGenericName(j, true) + "  the type");
            }
        }
        insertRegion.add("\t */");
        String superBuilder;
        if (data.isSubClass()) {
            superBuilder = data.getSuperTypeRaw() + ".Builder" + data.getSuperTypeGeneric(true);
        } else if (data.isEffectiveBuilderScopeVisible()) {
            data.ensureImport(DirectFieldsBeanBuilder.class);
            superBuilder = "DirectFieldsBeanBuilder<" + data.getTypeNoExtends() + ">";
        } else {
            data.ensureImport(DirectPrivateBeanBuilder.class);
            superBuilder = "DirectPrivateBeanBuilder<" + data.getTypeNoExtends() + ">";
        }
        if (data.isConstructable()) {
            insertRegion.add("\t" + data.getEffectiveBuilderScope() + "static " + finalType +
                    "class Builder" + data.getTypeGeneric(true) + " extends " + superBuilder + " {");
        } else {
            insertRegion.add("\t" + data.getEffectiveBuilderScope() + "abstract static " + finalType +
                    "class Builder" + data.getTypeGeneric(true) + " extends " + superBuilder + " {");
        }
        if (nonDerived.size() > 0) {
            insertRegion.add("");
            generateBuilderProperties();
        }
        insertRegion.add("");
        generateBuilderConstructorNoArgs();
        generateBuilderConstructorCopy();
        generateIndentedSeparator();
        generateBuilderGet();
        generateBuilderSet();
        generateBuilderOtherSets();
        if (data.isConstructable()) {
            generateBuilderBuild();
        }
        generateIndentedSeparator();
        generateBuilderPropertySetMethods();
        generateIndentedSeparator();
        generateBuilderToString();
        insertRegion.add("\t}");
        insertRegion.add("");
    }

    private void generateBuilderConstructorNoArgs() {
        insertRegion.add("\t\t/**");
        insertRegion.add("\t\t * Restricted constructor.");
        insertRegion.add("\t\t */");
        insertRegion.add("\t\t" + data.getNestedClassConstructorScope() + " Builder() {");
        if (!data.isEffectiveBuilderScopeVisible()) {
            insertRegion.add("\t\t\tsuper(meta());");
        }
        if (data.getImmutableDefaults() != null) {
            insertRegion.add("\t\t\t" + data.getImmutableDefaults() + "(this);");
        }
        insertRegion.add("\t\t}");
        insertRegion.add("");
    }

    private void generateBuilderConstructorCopy() {
        if (data.isEffectiveBuilderScopeVisible()) {
            List<PropertyGen> nonDerived = nonDerivedProperties();
            if (nonDerived.size() > 0) {
                insertRegion.add("\t\t/**");
                insertRegion.add("\t\t * Restricted copy constructor.");
                insertRegion.add("\t\t * @param beanToCopy  the bean to copy from, not null");
                insertRegion.add("\t\t */");
                insertRegion.add("\t\t" + data.getNestedClassConstructorScope() + " Builder(" + data.getTypeNoExtends() + " beanToCopy) {");
                if (data.isSubClass()) {
                    insertRegion.add("\t\t\tsuper(beanToCopy);");
                }
                for (int i = 0; i < nonDerived.size(); i++) {
                    insertRegion.addAll(nonDerived.get(i).generateBuilderConstructorAssign("beanToCopy"));
                }
                insertRegion.add("\t\t}");
                insertRegion.add("");
            }
        }
    }

    private void generateBuilderProperties() {
        for (PropertyGen prop : nonDerivedProperties()) {
            insertRegion.addAll(prop.generateBuilderField());
        }
    }

    private void generateBuilderGet() {
        List<PropertyGen> nonDerived = nonDerivedProperties();
        insertRegion.add("\t\t@Override");
        insertRegion.add("\t\tpublic Object get(String propertyName) {");
        if (nonDerived.size() > 0) {
            insertRegion.add("\t\t\tswitch (propertyName.hashCode()) {");
            for (PropertyGen prop : nonDerived) {
                insertRegion.addAll(prop.generateBuilderFieldGet());
            }
            insertRegion.add("\t\t\t\tdefault:");
            if (data.isRootClass()) {
                data.ensureImport(NoSuchElementException.class);
                insertRegion.add("\t\t\t\t\tthrow new NoSuchElementException(\"Unknown property: \" + propertyName);");
            } else {
                insertRegion.add("\t\t\t\t\treturn super.get(propertyName);");
            }
            insertRegion.add("\t\t\t}");
        } else {
            data.ensureImport(NoSuchElementException.class);
            insertRegion.add("\t\t\tthrow new NoSuchElementException(\"Unknown property: \" + propertyName);");
        }
        insertRegion.add("\t\t}");
        insertRegion.add("");
    }

    private void generateBuilderSet() {
        List<PropertyGen> nonDerived = nonDerivedProperties();
        boolean generics = data.getProperties().stream()
                .filter(p -> p.isGeneric() && p.isGenericWildcardParamType() == false)
                .findAny()
                .isPresent();
        if (generics) {
            insertRegion.add("\t\t@SuppressWarnings(\"unchecked\")");
        }
        insertRegion.add("\t\t@Override");
        insertRegion.add("\t\tpublic Builder" + data.getTypeGenericName(true) + " set(String propertyName, Object newValue) {");
        if (nonDerived.size() > 0) {
            insertRegion.add("\t\t\tswitch (propertyName.hashCode()) {");
            for (PropertyGen prop : nonDerived) {
                insertRegion.addAll(prop.generateBuilderFieldSet());
            }
            insertRegion.add("\t\t\t\tdefault:");
            if (data.isRootClass()) {
                data.ensureImport(NoSuchElementException.class);
                insertRegion.add("\t\t\t\t\tthrow new NoSuchElementException(\"Unknown property: \" + propertyName);");
            } else {
                insertRegion.add("\t\t\t\t\tsuper.set(propertyName, newValue);");
                insertRegion.add("\t\t\t\t\tbreak;");
            }
            insertRegion.add("\t\t\t}");
            insertRegion.add("\t\t\treturn this;");
        } else {
            data.ensureImport(NoSuchElementException.class);
            insertRegion.add("\t\t\tthrow new NoSuchElementException(\"Unknown property: \" + propertyName);");
        }
        insertRegion.add("\t\t}");
        insertRegion.add("");
    }

    private void generateBuilderOtherSets() {
        if (data.isEffectiveBuilderScopeVisible()) {
            insertRegion.add("\t\t@Override");
            insertRegion.add("\t\tpublic Builder" + data.getTypeGenericName(true) + " set(MetaProperty<?> property, Object value) {");
            insertRegion.add("\t\t\tsuper.set(property, value);");
            insertRegion.add("\t\t\treturn this;");
            insertRegion.add("\t\t}");
            insertRegion.add("");
            /**
             * {@inheritDoc}
             * @deprecated Use Joda-Convert in application code
             */
            
            insertRegion.add("\t\t/**");
            insertRegion.add("\t\t * @deprecated Use Joda-Convert in application code");
            insertRegion.add("\t\t */");
            insertRegion.add("\t\t@Override");
            insertRegion.add("\t\t@Deprecated");
            insertRegion.add("\t\tpublic Builder" + data.getTypeGenericName(true) + " setString(String propertyName, String value) {");
            if (data.isMetaScopePrivate()) {
                insertRegion.add("\t\t\tsetString(" + data.getTypeRaw() + ".Meta.INSTANCE.metaProperty(propertyName), value);");
            } else {
                insertRegion.add("\t\t\tsetString(meta().metaProperty(propertyName), value);");
            }
            insertRegion.add("\t\t\treturn this;");
            insertRegion.add("\t\t}");
            insertRegion.add("");
            insertRegion.add("\t\t/**");
            insertRegion.add("\t\t * @deprecated Use Joda-Convert in application code");
            insertRegion.add("\t\t */");
            insertRegion.add("\t\t@Override");
            insertRegion.add("\t\t@Deprecated");
            insertRegion.add("\t\tpublic Builder" + data.getTypeGenericName(true) + " setString(MetaProperty<?> property, String value) {");
            insertRegion.add("\t\t\tsuper.setString(property, value);");
            insertRegion.add("\t\t\treturn this;");
            insertRegion.add("\t\t}");
            insertRegion.add("");
            insertRegion.add("\t\t/**");
            insertRegion.add("\t\t * @deprecated Loop in application code");
            insertRegion.add("\t\t */");
            insertRegion.add("\t\t@Override");
            insertRegion.add("\t\t@Deprecated");
            insertRegion.add("\t\tpublic Builder" + data.getTypeGenericName(true) + " setAll(Map<String, ? extends Object> propertyValueMap) {");
            insertRegion.add("\t\t\tsuper.setAll(propertyValueMap);");
            insertRegion.add("\t\t\treturn this;");
            insertRegion.add("\t\t}");
            insertRegion.add("");
        }
    }

    private void generateBuilderBuild() {
        List<PropertyGen> nonDerived = nonDerivedProperties();
        insertRegion.add("\t\t@Override");
        insertRegion.add("\t\tpublic " + data.getTypeRaw() + data.getTypeGenericName(true) + " build() {");
        if (data.getImmutablePreBuild() != null) {
            insertRegion.add("\t\t\t" + data.getImmutablePreBuild() + "(this);");
        }
        if (data.getConstructorStyle() == CONSTRUCTOR_BY_ARGS) {
            if (nonDerived.size() == 0) {
                insertRegion.add("\t\t\treturn new " + data.getTypeWithDiamond() + "();");
            } else {
                insertRegion.add("\t\t\treturn new " + data.getTypeWithDiamond() + "(");
                for (int i = 0; i < nonDerived.size(); i++) {
                    insertRegion.add("\t\t\t\t\t" + nonDerived.get(i).generateBuilderFieldName() + (i < nonDerived.size() - 1 ? "," : ");"));
                }
            }
        } else if (data.getConstructorStyle() == CONSTRUCTOR_BY_BUILDER) {
            insertRegion.add("\t\t\treturn new " + data.getTypeWithDiamond() + "(this);");
        }
        insertRegion.add("\t\t}");
        insertRegion.add("");
    }

    private void generateBuilderPropertySetMethods() {
        if (data.isEffectiveBuilderScopeVisible()) {
            for (PropertyGen prop : nonDerivedProperties()) {
                insertRegion.addAll(prop.generateBuilderSetMethod());
            }
        }
    }

    private void generateBuilderToString() {
        List<PropertyGen> nonDerived = toStringProperties();
        if (data.isImmutable() && data.isTypeFinal()) {
            insertRegion.add("\t\t@Override");
            insertRegion.add("\t\tpublic String toString() {");
            if (nonDerived.size() == 0) {
                insertRegion.add("\t\t\treturn \"" + data.getTypeRaw() + ".Builder{}\";");
            } else {
                insertRegion.add("\t\t\tStringBuilder buf = new StringBuilder(" + (nonDerived.size() * 32 + 32) + ");");
                insertRegion.add("\t\t\tbuf.append(\"" + data.getTypeRaw() + ".Builder{\");");
                for (int i = 0; i < nonDerived.size(); i++) {
                    PropertyGen prop = nonDerived.get(i);
                    String getter = nonDerived.get(i).generateBuilderFieldName();
                    data.ensureImport(JodaBeanUtils.class);
                    String base = "\t\t\tbuf.append(\"" + prop.getData().getPropertyName() +
                            "\").append('=').append(JodaBeanUtils.toString(" + getter + "))";
                    if (i < nonDerived.size() - 1) {
                        insertRegion.add(base + ".append(',').append(' ');");
                    } else {
                        insertRegion.add(base + ";");
                    }
                }
                insertRegion.add("\t\t\tbuf.append('}');");
                insertRegion.add("\t\t\treturn buf.toString();");
            }
            insertRegion.add("\t\t}");
            insertRegion.add("");
            return;
        }
        
        insertRegion.add("\t\t@Override");
        insertRegion.add("\t\tpublic String toString() {");
        insertRegion.add("\t\t\tStringBuilder buf = new StringBuilder(" + (nonDerived.size() * 32 + 32) + ");");
        insertRegion.add("\t\t\tbuf.append(\"" + data.getTypeRaw() + ".Builder{\");");
        insertRegion.add("\t\t\tint len = buf.length();");
        insertRegion.add("\t\t\ttoString(buf);");
        insertRegion.add("\t\t\tif (buf.length() > len) {");
        insertRegion.add("\t\t\t\tbuf.setLength(buf.length() - 2);");
        insertRegion.add("\t\t\t}");
        insertRegion.add("\t\t\tbuf.append('}');");
        insertRegion.add("\t\t\treturn buf.toString();");
        insertRegion.add("\t\t}");
        insertRegion.add("");
        
        if (data.isSubClass()) {
            insertRegion.add("\t\t@Override");
        }
        insertRegion.add("\t\tprotected void toString(StringBuilder buf) {");
        if (data.isSubClass()) {
            insertRegion.add("\t\t\tsuper.toString(buf);");
        }
        for (int i = 0; i < nonDerived.size(); i++) {
            PropertyGen prop = nonDerived.get(i);
            String getter = nonDerived.get(i).generateBuilderFieldName();
            data.ensureImport(JodaBeanUtils.class);
            insertRegion.add("\t\t\tbuf.append(\"" + prop.getData().getPropertyName() +
                    "\").append('=').append(JodaBeanUtils.toString(" + getter + ")).append(',').append(' ');");
        }
        insertRegion.add("\t\t}");
        insertRegion.add("");
    }

    //-----------------------------------------------------------------------
    boolean isBean() {
        return data != null;
    }

    BeanData getData() {
        return data;
    }

    BeanGenConfig getConfig() {
        return config;
    }

    File getFile() {
        return file;
    }

    String getFieldPrefix() {
        return config.getPrefix();
    }

    private List<PropertyGen> nonDerivedProperties() {
        List<PropertyGen> nonDerived = new ArrayList<>();
        for (PropertyGen prop : properties) {
            if (prop.getData().isDerived() == false) {
                nonDerived.add(prop);
            }
        }
        return nonDerived;
    }

    private List<PropertyGen> nonDerivedEqualsHashCodeProperties() {
        List<PropertyGen> nonDerived = new ArrayList<>();
        for (PropertyGen prop : properties) {
            if (!prop.getData().isDerived() && !prop.getData().getEqualsHashCodeStyle().equals("omit")) {
                nonDerived.add(prop);
            }
        }
        return nonDerived;
    }

    private List<PropertyGen> toStringProperties() {
        List<PropertyGen> props = new ArrayList<>();
        for (PropertyGen prop : properties) {
            if (!prop.getData().isDerived() && !"omit".equals(prop.getData().getToStringStyle())) {
                props.add(prop);
            }
        }
        return props;
    }

}
