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

import static java.util.stream.Collectors.toList;

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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
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
    /** The list of removed imports. */
    private final SortedSet<String> removedImports = new TreeSet<>();

    /**
     * Constructor used when file is not a bean.
     * @param file  the file, not null
     * @param content  the content to process, not null
     * @param config  the config to use, not null
     * @param data  the parsed data
     */
    BeanGen(File file, List<String> content, BeanGenConfig config, BeanData data) {
        this.file = file;
        this.content = content;
        this.config = config;
        this.data = data;
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
        fixImports();
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

    void processNonBean() {
        fixImports();
        resolveImports();
    }

    private void fixImports() {
        renameImport("org.joda.beans.BeanDefinition", BeanDefinition.class);
        renameImport("org.joda.beans.DerivedProperty", DerivedProperty.class);
        renameImport("org.joda.beans.ImmutableConstructor", ImmutableConstructor.class);
        renameImport("org.joda.beans.ImmutableDefaults", ImmutableDefaults.class);
        renameImport("org.joda.beans.ImmutablePreBuild", ImmutablePreBuild.class);
        renameImport("org.joda.beans.ImmutableValidator", ImmutableValidator.class);
        renameImport("org.joda.beans.PropertyDefinition", PropertyDefinition.class);
    }

    private void renameImport(String old, Class<?> cls) {
        if (data.getCurrentImports().contains(old)) {
            removedImports.add(old);
            data.ensureImport(cls);
        }
    }

    private void resolveImports() {
        if (data.getNewImports().size() > 0) {
            int pos = data.getImportInsertLocation() + 1;
            for (String imp : data.getNewImports()) {
                content.add(pos++, "import " + imp + ";");
            }
        }
        if (removedImports.size() > 0) {
            for (ListIterator<String> it = content.listIterator(); it.hasNext(); ) {
                String line = it.next().trim();
                if (line.startsWith("import ")) {
                    String imported = line.substring(7).trim().replace(" ", "").replace(";", "");
                    if (removedImports.contains(imported)) {
                        it.remove();
                    }
                }
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
        addLine(0, LINE_SEPARATOR);
    }

    private void generateIndentedSeparator() {
        if (insertRegion.size() > 0 && insertRegion.get(insertRegion.size() - 1).equals(LINE_SEPARATOR_INDENTED)) {
            return;
        }
        addLine(0, LINE_SEPARATOR_INDENTED);
    }

    private void generateFactory() {
        if (data.isFactoryRequired()) {
            List<PropertyGen> nonDerived = nonDerivedProperties();
            addLine(1, "/**");
            addLine(1, " * Obtains an instance.");
            if (nonDerived.size() > 0) {
                if (data.isTypeGeneric()) {
                    for (int j = 0; j < data.getTypeGenericCount(); j++) {
                        addLine(1, " * @param " + data.getTypeGenericName(j, true) + "  the type");
                    }
                }
                for (int i = 0; i < nonDerived.size(); i++) {
                    PropertyData prop = nonDerived.get(i).getData();
                    addLine(1, " * @param " + prop.getPropertyName() + "  the value of the property" + prop.getNotNullJavadoc());
                }
            }
            addLine(1, " * @return the instance");
            addLine(1, " */");
            if (nonDerived.isEmpty()) {
                addLine(1, "public static " + data.getTypeNoExtends() + " " + data.getFactoryName() + "() {");
                addLine(2, "return new " + data.getTypeNoExtends() + "();");
                
            } else {
                if (data.isTypeGeneric()) {
                    addLine(1, "public static " + data.getTypeGeneric(true) + " " +
                                    data.getTypeNoExtends() + " " + data.getFactoryName() + "(");
                } else {
                    addLine(1, "public static " + data.getTypeNoExtends() + " " + data.getFactoryName() + "(");
                }
                for (int i = 0; i < nonDerived.size(); i++) {
                    PropertyGen prop = nonDerived.get(i);
                    addLine(3, prop.getBuilderType() + " " + prop.getData().getPropertyName() + joinComma(i, nonDerived, ") {"));
                }
                addLine(2, "return new " + data.getTypeWithDiamond() + "(");
                for (int i = 0; i < nonDerived.size(); i++) {
                    addLine(3, nonDerived.get(i).generateBuilderFieldName() + joinComma(i, nonDerived, ");"));
                }
            }
            addLine(1, "}");
            addBlankLine();
        }
    }

    private void generateImmutableBuilderMethod() {
        if (data.isConstructable() &&
                ((data.isImmutable() && data.isEffectiveBuilderScopeVisible()) || (data.isMutable() && data.isBuilderScopeVisible()))) {
            addLine(1, "/**");
            addLine(1, " * Returns a builder used to create an instance of the bean.");
            if (data.isTypeGeneric()) {
                for (int j = 0; j < data.getTypeGenericCount(); j++) {
                    addLine(1, " * @param " + data.getTypeGenericName(j, true) + "  the type");
                }
            }
            addLine(1, " * @return the builder, not null");
            addLine(1, " */");
            if (data.isTypeGeneric()) {
                addLine(1, data.getEffectiveBuilderScope() + "static " + data.getTypeGeneric(true) +
                                " " + data.getTypeRaw() + ".Builder" + data.getTypeGenericName(true) + " builder() {");
            } else {
                addLine(1, data.getEffectiveBuilderScope() + "static " + data.getTypeRaw() + ".Builder builder() {");
            }
            addLine(2, "return new " + data.getTypeRaw() + ".Builder" + data.getTypeGenericDiamond() + "();");
            addLine(1, "}");
            addBlankLine();
        }
    }

    private void generateBuilderBasedConstructor() {
        if (data.getConstructorStyle() == CONSTRUCTOR_BY_BUILDER && data.getImmutableConstructor() == CONSTRUCTOR_NONE && 
                ((data.isMutable() && data.isBuilderScopeVisible()) || data.isImmutable())) {
            List<PropertyGen> nonDerived = nonDerivedProperties();
            String scope = (data.isTypeFinal() ? "private" : "protected");
            // signature
            addLine(1, "/**");
            addLine(1, " * Restricted constructor.");
            addLine(1, " * @param builder  the builder to copy from, not null");
            addLine(1, " */");
            addLine(1, scope + " " + data.getTypeRaw() + "(" + data.getTypeRaw() + ".Builder" + data.getTypeGenericName(true) + " builder) {");
            // super
            if (data.isSubClass()) {
                addLine(2, "super(builder);");
            }
            // validate
            for (PropertyGen prop : properties) {
                if (prop.getData().isValidated()) {
                    addLine(2, prop.getData().getValidationMethodName() +
                            "(builder." + prop.generateBuilderFieldName() +
                            ", \"" + prop.getData().getPropertyName() + "\");");
                }
            }
            // assign
            if (data.isImmutable()) {
                // assign
                for (int i = 0; i < nonDerived.size(); i++) {
                    addLines(nonDerived.get(i).generateConstructorAssign("builder."));
                }
            } else {
                for (int i = 0; i < nonDerived.size(); i++) {
                    PropertyGen propGen = nonDerived.get(i);
                    PropertyData prop = propGen.getData();
                    if (prop.isCollectionType()) {
                        if (prop.isNotNull()) {
                            addLine(2, "this." + prop.getPropertyName() + ".addAll(builder." + propGen.generateBuilderFieldName() + ");");
                        } else {
                            addLine(2, "this." + prop.getPropertyName() + " = builder." + propGen.generateBuilderFieldName() + ";");
                        }
                    } else if (prop.isMapType()) {
                        if (prop.isNotNull()) {
                            addLine(2, "this." + prop.getPropertyName() + ".putAll(builder." + propGen.generateBuilderFieldName() + ");");
                        } else {
                            addLine(2, "this." + prop.getPropertyName() + " = builder." + propGen.generateBuilderFieldName() + ";");
                        }
                    } else {
                        addLine(2, "this." + prop.getPropertyName() + " = builder." + propGen.generateBuilderFieldName() + ";");
                    }
                }
            }
            if (data.getImmutableValidator() != null) {
                addLine(2, data.getImmutableValidator() + "();");
            }
            addLine(1, "}");
            addBlankLine();
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
                    addLine(1, "/**");
                    addLine(1, " * Creates an instance.");
                    addLine(1, " */");
                }
                if (generateAnnotation) {
                    data.ensureImport(CLASS_CONSTRUCTOR_PROPERTIES);
                    addLine(1, "@ConstructorProperties({})");
                }
                addLine(1, scope + data.getTypeRaw() + "() {");
            } else {
                // signature
                if (generateJavadoc) {
                    addLine(1, "/**");
                    addLine(1, " * Creates an instance.");
                    for (int i = 0; i < nonDerived.size(); i++) {
                        PropertyData prop = nonDerived.get(i).getData();
                        addLine(1, " * @param " + prop.getPropertyName() + "  the value of the property" + prop.getNotNullJavadoc());
                    }
                    addLine(1, " */");
                }
                if (generateAnnotation) {
                    data.ensureImport(CLASS_CONSTRUCTOR_PROPERTIES);
                    StringBuilder buf = new StringBuilder();
                    for (int i = 0; i < nonDerived.size(); i++) {
                        buf.append('"').append(nonDerived.get(i).getData().getPropertyName()).append('"');
                        buf.append(join(i, nonDerived, ", ", ""));
                    }
                    addLine(1, "@ConstructorProperties({" + buf.toString() + "})");
                }
                addLine(1, scope + data.getTypeRaw() + "(");
                for (int i = 0; i < nonDerived.size(); i++) {
                    PropertyGen prop = nonDerived.get(i);
                    addLine(3, prop.getBuilderType() + " " + prop.getData().getPropertyName() + joinComma(i, nonDerived, ") {"));
                }
                // validate (mutable light beans call setters which validate)
                if (!(data.isMutable() && data.isBeanStyleLight())) {
                    for (PropertyGen prop : properties) {
                        if (prop.getData().isValidated()) {
                            addLine(2, prop.getData().getValidationMethodName() +
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
                        addLine(2, generateSetInvoke + ";");
                    } else {
                        addLines(prop.generateConstructorAssign(""));
                    }
                }
            }
            if (data.getImmutableValidator() != null) {
                addLine(2, data.getImmutableValidator() + "();");
            }
            addLine(1, "}");
            addBlankLine();
        }
    }

    //-----------------------------------------------------------------------
    private void generateMeta() {
        if (data.isBeanStyleLightOrMinimal()) {
            addLine(1, "/**");
            addLine(1, " * The meta-bean for {@code " + data.getTypeRaw() + "}.");
            addLine(1, " */");
            boolean genericProps = data.getProperties().stream()
                    .filter(p -> p.isGeneric())
                    .findAny()
                    .isPresent();
            boolean unchecked = data.isBeanStyleMinimal() && data.isMutable() && genericProps;
            unchecked |= data.isBeanStyleMinimal() && data.isTypeGeneric() && !data.isSkipBuilderGeneration();
            boolean rawtypes = data.isBeanStyleMinimal() && data.isTypeGeneric();
            if (unchecked && rawtypes) {
                addLine(1, "@SuppressWarnings({\"unchecked\", \"rawtypes\" })");
            } else if (rawtypes) {
                addLine(1, "@SuppressWarnings(\"rawtypes\")");
            } else if (unchecked) {
                addLine(1, "@SuppressWarnings(\"unchecked\")");
            }
            if (data.isTypeGeneric()) {
                data.ensureImport(MetaBean.class);
                addLine(1, "private static final MetaBean META_BEAN =");
            } else {
                data.ensureImport(TypedMetaBean.class);
                addLine(1, "private static final TypedMetaBean<" + data.getTypeNoExtends() + "> META_BEAN =");
            }
            List<PropertyGen> nonDerived = nonDerivedProperties();
            List<PropertyGen> aliases = nonDerived.stream().filter(p -> p.getData().getAlias() != null).collect(toList());
            boolean hasAliases = aliases.isEmpty();
            if (data.isBeanStyleLight()) {
                // light
                data.ensureImport(LightMetaBean.class);
                data.ensureImport(MethodHandles.class);
                boolean specialInit = nonDerived.stream().filter(p -> p.isSpecialInit()).findAny().isPresent();
                if (nonDerived.isEmpty()) {
                    addLine(3, "LightMetaBean.of(" + data.getTypeRaw() + ".class, MethodHandles.lookup());");
                } else {
                    addLine(3, "LightMetaBean.of(");
                    addLine(5, data.getTypeRaw() + ".class,");
                    addLine(5, "MethodHandles.lookup(),");
                    generateFieldNames(nonDerived);
                    if (specialInit) {
                        for (int i = 0; i < nonDerived.size(); i++) {
                            addLine(5, nonDerived.get(i).generateInit() +
                                    joinComma(i, nonDerived, ")" + (hasAliases ? ";" : "")));
                        }
                    } else {
                        addLine(5, "new Object[0])" + (hasAliases ? ";" : ""));
                    }
                    for (int i = 0; i < aliases.size(); i++) {
                        PropertyGen prop = aliases.get(i);
                        addLine(5, ".withAlias(\"" + prop.getData().getAlias() + "\", \"" +
                                prop.getData().getPropertyName() + "\")" + join(i, aliases, "", ";"));
                    }
                }
            } else {
                data.ensureImport(MinimalMetaBean.class);
                addLine(3, "MinimalMetaBean.of(");
                addLine(5, data.getTypeRaw() + ".class,");
                generateFieldNames(nonDerived);
                String builderLambda = "() -> new " + data.getTypeRaw() + ".Builder()";
                if (data.isSkipBuilderGeneration()) {
                    data.ensureImport(BasicBeanBuilder.class);
                    builderLambda = "() -> new BasicBeanBuilder<>(new " + data.getTypeWithDiamond() + "())";
                }
                if (nonDerived.isEmpty()) {
                    if (data.isImmutable()) {
                        addLine(5, builderLambda + ");");
                    } else {
                        data.ensureImport(Collections.class);
                        data.ensureImport(Function.class);
                        data.ensureImport(BiConsumer.class);
                        addLine(5, builderLambda + ",");
                        addLine(5, "Collections.<Function<" + data.getTypeRaw() + ", Object>>emptyList(),");
                        addLine(5, "Collections.<BiConsumer<" + data.getTypeRaw() + ", Object>>emptyList());");
                    }
                } else {
                    addLine(5, builderLambda + ",");
                    if (data.isImmutable()) {
                        for (int i = 0; i < nonDerived.size(); i++) {
                            addLine(5, nonDerived.get(i).generateLambdaGetter() + joinComma(i, nonDerived, ")" +
                                    (hasAliases ? ";" : "")));
                        }
                    } else {
                        data.ensureImport(Arrays.class);
                        data.ensureImport(Function.class);
                        data.ensureImport(BiConsumer.class);
                        addLine(5, "Arrays.<Function<" + data.getTypeRaw() + ", Object>>asList(");
                        for (int i = 0; i < nonDerived.size(); i++) {
                            addLine(7, nonDerived.get(i).generateLambdaGetter() + joinComma(i, nonDerived, "),"));
                        }
                        addLine(5, "Arrays.<BiConsumer<" + data.getTypeRaw() + ", Object>>asList(");
                        for (int i = 0; i < nonDerived.size(); i++) {
                            addLine(7, nonDerived.get(i).generateLambdaSetter() +
                                    joinComma(i, nonDerived, "))" + (hasAliases ? ";" : "")));
                        }
                    }
                    for (int i = 0; i < aliases.size(); i++) {
                        PropertyGen prop = aliases.get(i);
                        addLine(5, ".withAlias(\"" + prop.getData().getAlias() + "\", \"" +
                                prop.getData().getPropertyName() + "\")" + join(i, aliases, "", ";"));
                    }
                }
            }
            addBlankLine();
            addLine(1, "/**");
            addLine(1, " * The meta-bean for {@code " + data.getTypeRaw() + "}.");
            addLine(1, " * @return the meta-bean, not null");
            addLine(1, " */");
            if (data.isTypeGeneric()) {
                addLine(1, "public static MetaBean meta() {");
            } else {
                addLine(1, "public static TypedMetaBean<" + data.getTypeNoExtends() + "> meta() {");
            }
            addLine(2, "return META_BEAN;");
            addLine(1, "}");
            addBlankLine();
            addLine(1, "static {");
            data.ensureImport(MetaBean.class);
            addLine(2, "MetaBean.register(META_BEAN);");
            addLine(1, "}");
            addBlankLine();
            
        } else {
            // this cannot be generified without either Eclipse or javac complaining
            // raw types forever
            addLine(1, "/**");
            addLine(1, " * The meta-bean for {@code " + data.getTypeRaw() + "}.");
            addLine(1, " * @return the meta-bean, not null");
            if (data.isMetaScopePrivate()) {
                data.ensureImport(MetaBean.class);
                addLine(1, " */");
                addLine(1, "public static MetaBean meta() {");
            } else if (data.isTypeGeneric()) {
                addLine(1, " */");
                addLine(1, "@SuppressWarnings(\"rawtypes\")");
                addLine(1, "public static " + data.getTypeRaw() + ".Meta meta() {");
            } else {
                addLine(1, " */");
                addLine(1, "public static " + data.getTypeRaw() + ".Meta meta() {");
            }
            addLine(2, "return " + data.getTypeRaw() + ".Meta.INSTANCE;");
            addLine(1, "}");
            
            if (data.isTypeGeneric()) {
                generateMetaForGenericType();
            }
            
            addBlankLine();
            addLine(1, "static {");
            data.ensureImport(MetaBean.class);
            addLine(2, "MetaBean.register(" + data.getTypeRaw() + ".Meta.INSTANCE);");
            addLine(1, "}");
            addBlankLine();
        }
    }

    private void generateFieldNames(List<PropertyGen> nonDerived) {
        if (nonDerived.isEmpty()) {
            addLine(5, "new String[0],");
        } else {
            addLine(5, "new String[] {");
            for (int i = 0; i < nonDerived.size(); i++) {
                addLine(7, "\"" + nonDerived.get(i).getData().getFieldName() + join(i, nonDerived, "\",", "\"},"));
            }
        }
    }

    private void generateMetaForGenericType() {
        // this works around an Eclipse bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=397462
        // long name needed for uniqueness as static overriding is borked
        addBlankLine();
        addLine(1, "/**");
        addLine(1, " * The meta-bean for {@code " + data.getTypeRaw() + "}.");
        if (data.getTypeGenericCount() == 1) {
            addLine(1, " * @param <R>  the bean's generic type");
            addLine(1, " * @param cls  the bean's generic type");
        } else if (data.getTypeGenericCount() == 2) {
            addLine(1, " * @param <R>  the first generic type");
            addLine(1, " * @param <S>  the second generic type");
            addLine(1, " * @param cls1  the first generic type");
            addLine(1, " * @param cls2  the second generic type");
        } else if (data.getTypeGenericCount() == 3) {
            addLine(1, " * @param <R>  the first generic type");
            addLine(1, " * @param <S>  the second generic type");
            addLine(1, " * @param <T>  the second generic type");
            addLine(1, " * @param cls1  the first generic type");
            addLine(1, " * @param cls2  the second generic type");
            addLine(1, " * @param cls3  the third generic type");
        }
        addLine(1, " * @return the meta-bean, not null");
        addLine(1, " */");
        addLine(1, "@SuppressWarnings(\"unchecked\")");
        String[] typeNames = new String[] {"R", "S", "T"};
        if (data.getTypeGenericCount() == 1) {
            addLine(1, "public static <R" + data.getTypeGenericExtends(0, typeNames) +
                    "> " + data.getTypeRaw() + ".Meta<R> meta" + data.getTypeRaw() + "(Class<R> cls) {");
        } else if (data.getTypeGenericCount() == 2) {
            addLine(1, "public static <R" + data.getTypeGenericExtends(0, typeNames) +
                    ", S" + data.getTypeGenericExtends(1, typeNames) + "> " + data.getTypeRaw() +
                    ".Meta<R, S> meta" + data.getTypeRaw() + "(Class<R> cls1, Class<S> cls2) {");
        } else if (data.getTypeGenericCount() == 3) {
            addLine(1, "public static <R" + data.getTypeGenericExtends(0, typeNames) +
                    ", S" + data.getTypeGenericExtends(1, typeNames) +
                    ", T" + data.getTypeGenericExtends(2, typeNames) +
                    "> " + data.getTypeRaw() +
                    ".Meta<R, S, T> meta" + data.getTypeRaw() + "(Class<R> cls1, Class<S> cls2, Class<T> cls3) {");
        }
        addLine(2, "return " + data.getTypeRaw() + ".Meta.INSTANCE;");
        addLine(1, "}");
    }

    private void generateSerializationVersionId() {
        if (data.isSerializable() && !data.isManualSerializationId()) {
            addLine(1, "/**");
            addLine(1, " * The serialization version id.");
            addLine(1, " */");
            addLine(1, "private static final long serialVersionUID = 1L;");
            addBlankLine();
        }
    }

    private void generatePropertyChangeSupportField() {
        if (data.isPropertyChangeSupport()) {
            data.ensureImport(CLASS_PROPERTY_CHANGE_SUPPORT);
            addLine(1, "/**");
            addLine(1, " * The property change support field.");
            addLine(1, " */");
            addLine(1, "private final transient PropertyChangeSupport " + config.getPrefix() + "propertyChangeSupport = new PropertyChangeSupport(this);");
            addBlankLine();
        }
    }

    private void generateHashCodeField() {
        if (data.isCacheHashCode()) {
            addLine(1, "/**");
            addLine(1, " * The cached hash code, using the racy single-check idiom.");
            addLine(1, " */");
            addLine(1, "private transient int " + config.getPrefix() + "cacheHashCode;");
            addBlankLine();
        }
    }

    private void generateMetaBean() {
        if (data.isMetaScopePrivate() || data.isBeanStyleMinimal()) {
            addLine(1, "@Override");
            if (data.isBeanStyleLightOrMinimal()) {
                data.ensureImport(TypedMetaBean.class);
                if (data.isTypeGeneric()) {
                    addLine(1, "@SuppressWarnings(\"unchecked\")");
                }
                addLine(1, "public TypedMetaBean<" + data.getTypeNoExtends() + "> metaBean() {");
                if (data.isTypeGeneric()) {
                    addLine(2, "return (TypedMetaBean<" + data.getTypeNoExtends() + ">) META_BEAN;");
                } else {
                    addLine(2, "return META_BEAN;");
                }
            } else {
                data.ensureImport(MetaBean.class);
                addLine(1, "public MetaBean metaBean() {");
                addLine(2, "return " + data.getTypeRaw() + ".Meta.INSTANCE;");
            }
            addLine(1, "}");
            addBlankLine();
        } else {
            if (data.isTypeGeneric()) {
                addLine(1, "@SuppressWarnings(\"unchecked\")");
            }
            addLine(1, "@Override");
            addLine(1, "public " + data.getTypeRaw() +
                    ".Meta" + data.getTypeGenericName(true) + " metaBean() {");
            addLine(2, "return " + data.getTypeRaw() + ".Meta.INSTANCE;");
            addLine(1, "}");
            addBlankLine();
        }
    }

    private void generateGettersSetters() {
        for (PropertyGen prop : properties) {
            generateSeparator();
            addLines(prop.generateGetter());
            if (data.isMutable()) {
                addLines(prop.generateSetter());
            }
            if (data.isBeanStyleGenerateProperties()) {
                addLines(prop.generateProperty());
            }
        }
    }

    //-----------------------------------------------------------------------
    private void generateImmutableToBuilder() {
        if (data.isImmutable() && data.isEffectiveBuilderScopeVisible()) {
            if (data.isConstructable()) {
                List<PropertyGen> nonDerived = nonDerivedProperties();
                if (nonDerived.size() > 0) {
                    addLine(1, "/**");
                    addLine(1, " * Returns a builder that allows this bean to be mutated.");
                    addLine(1, " * @return the mutable builder, not null");
                    addLine(1, " */");
                    if (data.isRootClass() == false) {
                        addLine(1, "@Override");
                    }
                    addLine(1, data.getEffectiveBuilderScope() + "Builder" + data.getTypeGenericName(true) + " toBuilder() {");
                    addLine(2, "return new Builder" + data.getTypeGenericDiamond() + "(this);");
                    addLine(1, "}");
                    addBlankLine();
                }
            } else {
                addLine(1, "/**");
                addLine(1, " * Returns a builder that allows this bean to be mutated.");
                addLine(1, " * @return the mutable builder, not null");
                addLine(1, " */");
                if (data.isRootClass() == false) {
                    addLine(1, "@Override");
                }
                addLine(1, "public abstract Builder" + data.getTypeGenericName(true) + " toBuilder();");
                addBlankLine();
            }
        }
    }

    private void generateClone() {
        if (data.isSkipCloneGeneration() ||
                data.isManualClone() ||
                (data.isRootClass() == false && data.isConstructable() == false)) {
            return;
        }
        addLine(1, "@Override");
        if (data.isImmutable()) {
            addLine(1, "public " + data.getTypeNoExtends() + " clone() {");
            addLine(2, "return this;");
        } else {
            data.ensureImport(JodaBeanUtils.class);
            addLine(1, "public " + data.getTypeNoExtends() + " clone() {");
            addLine(2, "return JodaBeanUtils.cloneAlways(this);");
        }
        addLine(1, "}");
        addBlankLine();
    }

    private void generateEquals() {
        if (data.isManualEqualsHashCode()) {
            return;
        }
        addLine(1, "@Override");
        addLine(1, "public boolean equals(Object obj) {");
        addLine(2, "if (obj == this) {");
        addLine(3, "return true;");
        addLine(2, "}");
        addLine(2, "if (obj != null && obj.getClass() == this.getClass()) {");
        List<PropertyGen> nonDerived = nonDerivedEqualsHashCodeProperties();
        if (nonDerived.size() == 0) {
            if (data.isSubClass()) {
                addLine(3, "return super.equals(obj);");
            } else {
                addLine(3, "return true;");
            }
        } else {
            addLine(3, data.getTypeWildcard() + " other = (" + data.getTypeWildcard() + ") obj;");
            for (int i = 0; i < nonDerived.size(); i++) {
                PropertyGen prop = nonDerived.get(i);
                String getter = equalsHashCodeFieldAccessor(prop);
                data.ensureImport(JodaBeanUtils.class);
                String equals = "JodaBeanUtils.equal(" + getter + ", other." + getter + ")";
                if (PRIMITIVE_EQUALS.contains(prop.getData().getType())) {
                    equals = "(" + getter + " == other." + getter + ")";
                }
                addLine(
                        0, (i == 0 ? "\t\t\treturn " : "\t\t\t\t\t") + equals +
                        (data.isSubClass() || i < nonDerived.size() - 1 ? " &&" : ";"));
            }
            if (data.isSubClass()) {
                addLine(5, "super.equals(obj);");
            }
        }
        addLine(2, "}");
        addLine(2, "return false;");
        addLine(1, "}");
        addBlankLine();
    }

    private void generateHashCode() {
        if (data.isManualEqualsHashCode()) {
            return;
        }
        addLine(1, "@Override");
        addLine(1, "public int hashCode() {");
        if (data.isCacheHashCode()) {
            addLine(2, "int hash = " + config.getPrefix() + "cacheHashCode;");
            addLine(2, "if (hash == 0) {");
            if (data.isSubClass()) {
                addLine(3, "hash = 7;");
            } else {
                addLine(3, "hash = getClass().hashCode();");
            }
            generateHashCodeContent("\t\t\t");
            if (data.isSubClass()) {
                addLine(3, "hash = hash ^ super.hashCode();");
            }
            addLine(3, config.getPrefix() + "cacheHashCode = hash;");
            addLine(2, "}");
            addLine(2, "return hash;");
        } else {
            if (data.isSubClass()) {
                addLine(2, "int hash = 7;");
            } else {
                addLine(2, "int hash = getClass().hashCode();");
            }
            generateHashCodeContent("\t\t");
            if (data.isSubClass()) {
                addLine(2, "return hash ^ super.hashCode();");
            } else {
                addLine(2, "return hash;");
            }
        }
        addLine(1, "}");
        addBlankLine();
    }

    private void generateHashCodeContent(String indent) {
        List<PropertyGen> nonDerived = nonDerivedEqualsHashCodeProperties();
        for (int i = 0; i < nonDerived.size(); i++) {
            PropertyGen prop = nonDerived.get(i);
            String getter = equalsHashCodeFieldAccessor(prop);
            data.ensureImport(JodaBeanUtils.class);
            addLine(0, indent + "hash = hash * 31 + JodaBeanUtils.hashCode(" + getter + ");");
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
            addLine(1, "@Override");
            addLine(1, "public String toString() {");
            addLine(2, "StringBuilder buf = new StringBuilder(" + (props.size() * 32 + 32) + ");");
            addLine(2, "buf.append(\"" + data.getTypeRaw() + "{\");");
            if (props.size() > 0) {
                data.ensureImport(JodaBeanUtils.class);
                for (int i = 0; i < props.size(); i++) {
                    PropertyGen prop = props.get(i);
                    String getter = toStringFieldAccessor(prop);
                    addLine(2, "buf.append(\"" + prop.getData().getPropertyName() + "\").append('=')" + 
                            join(i, props,
                                    ".append(" + getter + ").append(',').append(' ');",
                                    ".append(JodaBeanUtils.toString(" + getter + "));"));
                }
            }
            addLine(2, "buf.append('}');");
            addLine(2, "return buf.toString();");
            addLine(1, "}");
            addBlankLine();
            return;
        }
        
        addLine(1, "@Override");
        addLine(1, "public String toString() {");
        addLine(2, "StringBuilder buf = new StringBuilder(" + (props.size() * 32 + 32) + ");");
        addLine(2, "buf.append(\"" + data.getTypeRaw() + "{\");");
        addLine(2, "int len = buf.length();");
        addLine(2, "toString(buf);");
        addLine(2, "if (buf.length() > len) {");
        addLine(3, "buf.setLength(buf.length() - 2);");
        addLine(2, "}");
        addLine(2, "buf.append('}');");
        addLine(2, "return buf.toString();");
        addLine(1, "}");
        addBlankLine();
        
        if (data.isSubClass()) {
            addLine(1, "@Override");
        }
        addLine(1, "protected void toString(StringBuilder buf) {");
        if (data.isSubClass()) {
            addLine(2, "super.toString(buf);");
        }
        for (int i = 0; i < props.size(); i++) {
            PropertyGen prop = props.get(i);
            String getter = toStringFieldAccessor(prop);
            data.ensureImport(JodaBeanUtils.class);
            addLine(2, "buf.append(\"" + prop.getData().getPropertyName() +
                    "\").append('=').append(JodaBeanUtils.toString(" + getter + ")).append(',').append(' ');");
        }
        addLine(1, "}");
        addBlankLine();
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
        addLine(1, "/**");
        addLine(1, " * The meta-bean for {@code " + data.getTypeRaw() + "}.");
        if (data.isTypeGeneric()) {
            for (int j = 0; j < data.getTypeGenericCount(); j++) {
                addLine(1, " * @param " + data.getTypeGenericName(j, true) + "  the type");
            }
        }
        addLine(1, " */");
        String superMeta;
        if (data.isSubClass()) {
            superMeta = data.getSuperTypeRaw() + ".Meta" + data.getSuperTypeGeneric(true);
        } else {
            data.ensureImport(DirectMetaBean.class);
            superMeta = "DirectMetaBean";
        }
        String finalType = data.isTypeFinal() ? "final " : "";
        if (data.isTypeGeneric()) {
            addLine(1, data.getEffectiveMetaScope() + "static " + finalType + 
                    "class Meta" + data.getTypeGeneric(true) + " extends " + superMeta + " {");
        } else {
            addLine(1, data.getEffectiveMetaScope() + "static " + finalType + 
                    "class Meta extends " + superMeta + " {");
        }
        addLine(2, "/**");
        addLine(2, " * The singleton instance of the meta-bean.");
        addLine(2, " */");
        if (data.isTypeGeneric()) {
            addLine(2, "@SuppressWarnings(\"rawtypes\")");
        }
        addLine(2, "static final Meta INSTANCE = new Meta();");
        addBlankLine();
        generateMetaPropertyConstants();
        generateMetaPropertyMapSetup();
        addLine(2, "/**");
        addLine(2, " * Restricted constructor.");
        addLine(2, " */");
        addLine(2, data.getNestedClassConstructorScope() + " Meta() {");
        addLine(2, "}");
        addBlankLine();
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
        addLine(1, "}");
        addBlankLine();
    }

    private void generateMetaPropertyConstants() {
        for (PropertyGen prop : properties) {
            addLines(prop.generateMetaPropertyConstant());
        }
    }

    private void generateMetaPropertyMapSetup() {
        data.ensureImport(MetaProperty.class);
        data.ensureImport(DirectMetaPropertyMap.class);
        addLine(2, "/**");
        addLine(2, " * The meta-properties.");
        addLine(2, " */");
        addLine(2, "private final Map<String, MetaProperty<?>> " + config.getPrefix() + "metaPropertyMap$ = new DirectMetaPropertyMap(");
        if (data.isSubClass()) {
            addLine(4, "this, (DirectMetaPropertyMap) super.metaPropertyMap()" + (properties.size() == 0 ? ");" : ","));
        } else {
            addLine(4, "this, null" + (properties.size() == 0 ? ");" : ","));
        }
        for (int i = 0; i < properties.size(); i++) {
            addLine(4, "\"" + properties.get(i).getData().getPropertyName() + "\"" + joinComma(i, properties, ");"));
        }
        addBlankLine();
    }

    private void generateMetaBuilder() {
        if (!data.isConstructable()) {
            addLine(2, "@Override");
            addLine(2, "public boolean isBuildable() {");
            addLine(3, "return false;");
            addLine(2, "}");
            addBlankLine();
        }
        addLine(2, "@Override");
        if (data.isImmutable() && data.isEffectiveBuilderScopeVisible() == false) {
            data.ensureImport(BeanBuilder.class);
            addLine(2, "public BeanBuilder<? extends " + data.getTypeNoExtends() + "> builder() {");
            if (data.isConstructable()) {
                addLine(3, "return new " + data.getTypeRaw() + ".Builder" + data.getTypeGenericDiamond() + "();");
            } else {
                addLine(3, "throw new UnsupportedOperationException(\"" + data.getTypeRaw() + " is an abstract class\");");
            }
        } else if (data.isImmutable() || (data.isMutable() && data.isBuilderScopeVisible())) {
            addLine(2, "public " + data.getTypeRaw() + ".Builder" + data.getTypeGenericName(true) + " builder() {");
            if (data.isConstructable()) {
                addLine(3, "return new " + data.getTypeRaw() + ".Builder" + data.getTypeGenericDiamond() + "();");
            } else {
                addLine(3, "throw new UnsupportedOperationException(\"" + data.getTypeRaw() + " is an abstract class\");");
            }
        } else {
            data.ensureImport(BeanBuilder.class);
            addLine(2, "public BeanBuilder<? extends " + data.getTypeNoExtends() + "> builder() {");
            if (data.isConstructable()) {
                data.ensureImport(DirectBeanBuilder.class);
                addLine(3, "return new DirectBeanBuilder<>(new " + data.getTypeNoExtends() + "());");
            } else {
                addLine(3, "throw new UnsupportedOperationException(\"" + data.getTypeRaw() + " is an abstract class\");");
            }
        }
        addLine(2, "}");
        addBlankLine();
    }

    private void generateMetaBeanType() {
        if (data.isTypeGeneric()) {
            addLine(2, "@SuppressWarnings({\"unchecked\", \"rawtypes\" })");
        }
        addLine(2, "@Override");
        addLine(2, "public Class<? extends " + data.getTypeNoExtends() + "> beanType() {");
        if (data.isTypeGeneric()) {
            addLine(3, "return (Class) " + data.getTypeRaw() + ".class;");
        } else {
            addLine(3, "return " + data.getTypeNoExtends() + ".class;");
        }
        addLine(2, "}");
        addBlankLine();
    }

    private void generateMetaPropertyGet() {
        if (properties.size() > 0) {
            data.ensureImport(MetaProperty.class);
            addLine(2, "@Override");
            addLine(2, "protected MetaProperty<?> metaPropertyGet(String propertyName) {");
            addLine(3, "switch (propertyName.hashCode()) {");
            for (PropertyGen prop : properties) {
                addLines(prop.generateMetaPropertyGetCase());
            }
            addLine(3, "}");
            addLine(3, "return super.metaPropertyGet(propertyName);");
            addLine(2, "}");
            addBlankLine();
        }
    }

    private void generateMetaPropertyMap() {
        data.ensureImport(Map.class);
        addLine(2, "@Override");
        addLine(2, "public Map<String, MetaProperty<?>> metaPropertyMap() {");
        addLine(3, "return " + config.getPrefix() + "metaPropertyMap$;");
        addLine(2, "}");
        addBlankLine();
    }

    private void generateMetaPropertyMethods() {
        if (data.isBeanStyleGenerateMetaProperties()) {
            for (PropertyGen prop : properties) {
                addLines(prop.generateMetaProperty());
            }
        }
    }

    //-----------------------------------------------------------------------
    private void generateMetaGetPropertyValue() {
        if (properties.size() == 0) {
            return;
        }
        data.ensureImport(Bean.class);
        addLine(2, "@Override");
        addLine(2, "protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {");
        addLine(3, "switch (propertyName.hashCode()) {");
        for (PropertyGen prop : properties) {
            addLines(prop.generatePropertyGetCase());
        }
        addLine(3, "}");
        addLine(3, "return super.propertyGet(bean, propertyName, quiet);");
        addLine(2, "}");
        addBlankLine();
    }

    private void generateMetaSetPropertyValue() {
        if (properties.size() == 0) {
            return;
        }
        data.ensureImport(Bean.class);
        if (data.isImmutable()) {
            addLine(2, "@Override");
            addLine(2, "protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {");
            addLine(3, "metaProperty(propertyName);");
            addLine(3, "if (quiet) {");
            addLine(4, "return;");
            addLine(3, "}");
            addLine(3, "throw new UnsupportedOperationException(\"Property cannot be written: \" + propertyName);");
            addLine(2, "}");
            addBlankLine();
            return;
        }
        
        boolean generics = false;
        for (PropertyData prop : data.getProperties()) {
            generics |= (prop.getStyle().isWritable() &&
                    ((prop.isGeneric() && prop.isGenericWildcardParamType() == false) || data.isTypeGeneric()));
        }
        if (generics) {
            addLine(2, "@SuppressWarnings(\"unchecked\")");
        }
        addLine(2, "@Override");
        addLine(2, "protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {");
        addLine(3, "switch (propertyName.hashCode()) {");
        for (PropertyGen prop : properties) {
            addLines(prop.generatePropertySetCase());
        }
        addLine(3, "}");
        addLine(3, "super.propertySet(bean, propertyName, newValue, quiet);");
        addLine(2, "}");
        addBlankLine();
    }

    private void generateMetaValidate() {
        if (data.isValidated() == false || data.isImmutable()) {
            return;
        }
        data.ensureImport(Bean.class);
        addLine(2, "@Override");
        addLine(2, "protected void validate(Bean bean) {");
        if (data.isValidated()) {
            for (PropertyGen prop : properties) {
                if (prop.getData().isValidated()) {
                    addLine(3, prop.getData().getValidationMethodName() +
                            "(((" + data.getTypeWildcard() + ") bean)." + prop.getData().getFieldName() +
                            ", \"" + prop.getData().getPropertyName() + "\");");
                }
            }
        }
        if (data.isSubClass()) {
            addLine(3, "super.validate(bean);");
        }
        addLine(2, "}");
        addBlankLine();
    }

    //-----------------------------------------------------------------------
    private void generateBuilderClass() {
        if (data.isSkipBuilderGeneration()) {
            return;
        }
        List<PropertyGen> nonDerived = nonDerivedProperties();
        generateSeparator();
        String finalType = data.isTypeFinal() ? "final " : "";
        addLine(1, "/**");
        addLine(1, " * The bean-builder for {@code " + data.getTypeRaw() + "}.");
        if (data.isTypeGeneric()) {
            for (int j = 0; j < data.getTypeGenericCount(); j++) {
                addLine(1, " * @param " + data.getTypeGenericName(j, true) + "  the type");
            }
        }
        addLine(1, " */");
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
            addLine(1, data.getEffectiveBuilderScope() + "static " + finalType +
                    "class Builder" + data.getTypeGeneric(true) + " extends " + superBuilder + " {");
        } else {
            addLine(1, data.getEffectiveBuilderScope() + "abstract static " + finalType +
                    "class Builder" + data.getTypeGeneric(true) + " extends " + superBuilder + " {");
        }
        if (nonDerived.size() > 0) {
            addBlankLine();
            generateBuilderProperties();
        }
        addBlankLine();
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
        addLine(1, "}");
        addBlankLine();
    }

    private void generateBuilderConstructorNoArgs() {
        addLine(2, "/**");
        addLine(2, " * Restricted constructor.");
        addLine(2, " */");
        addLine(2, data.getNestedClassConstructorScope() + " Builder() {");
        if (data.getImmutableDefaults() != null) {
            addLine(3, data.getImmutableDefaults() + "(this);");
        }
        addLine(2, "}");
        addBlankLine();
    }

    private void generateBuilderConstructorCopy() {
        if (data.isEffectiveBuilderScopeVisible()) {
            List<PropertyGen> nonDerived = nonDerivedProperties();
            if (nonDerived.size() > 0) {
                addLine(2, "/**");
                addLine(2, " * Restricted copy constructor.");
                addLine(2, " * @param beanToCopy  the bean to copy from, not null");
                addLine(2, " */");
                addLine(2, data.getNestedClassConstructorScope() + " Builder(" + data.getTypeNoExtends() + " beanToCopy) {");
                if (data.isSubClass()) {
                    addLine(3, "super(beanToCopy);");
                }
                for (int i = 0; i < nonDerived.size(); i++) {
                    addLines(nonDerived.get(i).generateBuilderConstructorAssign("beanToCopy"));
                }
                addLine(2, "}");
                addBlankLine();
            }
        }
    }

    private void generateBuilderProperties() {
        for (PropertyGen prop : nonDerivedProperties()) {
            addLines(prop.generateBuilderField());
        }
    }

    private void generateBuilderGet() {
        List<PropertyGen> nonDerived = nonDerivedProperties();
        addLine(2, "@Override");
        addLine(2, "public Object get(String propertyName) {");
        if (nonDerived.size() > 0) {
            addLine(3, "switch (propertyName.hashCode()) {");
            for (PropertyGen prop : nonDerived) {
                addLines(prop.generateBuilderFieldGet());
            }
            addLine(4, "default:");
            if (data.isRootClass()) {
                data.ensureImport(NoSuchElementException.class);
                addLine(5, "throw new NoSuchElementException(\"Unknown property: \" + propertyName);");
            } else {
                addLine(5, "return super.get(propertyName);");
            }
            addLine(3, "}");
        } else {
            data.ensureImport(NoSuchElementException.class);
            addLine(3, "throw new NoSuchElementException(\"Unknown property: \" + propertyName);");
        }
        addLine(2, "}");
        addBlankLine();
    }

    private void generateBuilderSet() {
        List<PropertyGen> nonDerived = nonDerivedProperties();
        boolean generics = data.getProperties().stream()
                .filter(p -> p.isGeneric() && p.isGenericWildcardParamType() == false)
                .findAny()
                .isPresent();
        if (generics) {
            addLine(2, "@SuppressWarnings(\"unchecked\")");
        }
        addLine(2, "@Override");
        addLine(2, "public Builder" + data.getTypeGenericName(true) + " set(String propertyName, Object newValue) {");
        if (nonDerived.size() > 0) {
            addLine(3, "switch (propertyName.hashCode()) {");
            for (PropertyGen prop : nonDerived) {
                addLines(prop.generateBuilderFieldSet());
            }
            addLine(4, "default:");
            if (data.isRootClass()) {
                data.ensureImport(NoSuchElementException.class);
                addLine(5, "throw new NoSuchElementException(\"Unknown property: \" + propertyName);");
            } else {
                addLine(5, "super.set(propertyName, newValue);");
                addLine(5, "break;");
            }
            addLine(3, "}");
            addLine(3, "return this;");
        } else {
            data.ensureImport(NoSuchElementException.class);
            addLine(3, "throw new NoSuchElementException(\"Unknown property: \" + propertyName);");
        }
        addLine(2, "}");
        addBlankLine();
    }

    private void generateBuilderOtherSets() {
        if (data.isEffectiveBuilderScopeVisible()) {
            addLine(2, "@Override");
            addLine(2, "public Builder" + data.getTypeGenericName(true) + " set(MetaProperty<?> property, Object value) {");
            addLine(3, "super.set(property, value);");
            addLine(3, "return this;");
            addLine(2, "}");
            addBlankLine();
        }
    }

    private void generateBuilderBuild() {
        List<PropertyGen> nonDerived = nonDerivedProperties();
        addLine(2, "@Override");
        addLine(2, "public " + data.getTypeRaw() + data.getTypeGenericName(true) + " build() {");
        if (data.getImmutablePreBuild() != null) {
            addLine(3, data.getImmutablePreBuild() + "(this);");
        }
        if (data.getConstructorStyle() == CONSTRUCTOR_BY_ARGS) {
            if (nonDerived.size() == 0) {
                addLine(3, "return new " + data.getTypeWithDiamond() + "();");
            } else {
                addLine(3, "return new " + data.getTypeWithDiamond() + "(");
                for (int i = 0; i < nonDerived.size(); i++) {
                    addLine(5, nonDerived.get(i).generateBuilderFieldName() + joinComma(i, nonDerived, ");"));
                }
            }
        } else if (data.getConstructorStyle() == CONSTRUCTOR_BY_BUILDER) {
            addLine(3, "return new " + data.getTypeWithDiamond() + "(this);");
        }
        addLine(2, "}");
        addBlankLine();
    }

    private void generateBuilderPropertySetMethods() {
        if (data.isEffectiveBuilderScopeVisible()) {
            for (PropertyGen prop : nonDerivedProperties()) {
                addLines(prop.generateBuilderSetMethod());
            }
        }
    }

    private void generateBuilderToString() {
        List<PropertyGen> nonDerived = toStringProperties();
        if (data.isImmutable() && data.isTypeFinal()) {
            addLine(2, "@Override");
            addLine(2, "public String toString() {");
            if (nonDerived.size() == 0) {
                addLine(3, "return \"" + data.getTypeRaw() + ".Builder{}\";");
            } else {
                addLine(3, "StringBuilder buf = new StringBuilder(" + (nonDerived.size() * 32 + 32) + ");");
                addLine(3, "buf.append(\"" + data.getTypeRaw() + ".Builder{\");");
                for (int i = 0; i < nonDerived.size(); i++) {
                    PropertyGen prop = nonDerived.get(i);
                    String getter = nonDerived.get(i).generateBuilderFieldName();
                    data.ensureImport(JodaBeanUtils.class);
                    String base = "\t\t\tbuf.append(\"" + prop.getData().getPropertyName() +
                            "\").append('=').append(JodaBeanUtils.toString(" + getter + "))";
                    addLine(0, base + join(i, nonDerived, ".append(',').append(' ');", ";"));
                }
                addLine(3, "buf.append('}');");
                addLine(3, "return buf.toString();");
            }
            addLine(2, "}");
            addBlankLine();
            return;
        }
        
        addLine(2, "@Override");
        addLine(2, "public String toString() {");
        addLine(3, "StringBuilder buf = new StringBuilder(" + (nonDerived.size() * 32 + 32) + ");");
        addLine(3, "buf.append(\"" + data.getTypeRaw() + ".Builder{\");");
        addLine(3, "int len = buf.length();");
        addLine(3, "toString(buf);");
        addLine(3, "if (buf.length() > len) {");
        addLine(4, "buf.setLength(buf.length() - 2);");
        addLine(3, "}");
        addLine(3, "buf.append('}');");
        addLine(3, "return buf.toString();");
        addLine(2, "}");
        addBlankLine();
        
        if (data.isSubClass()) {
            addLine(2, "@Override");
        }
        addLine(2, "protected void toString(StringBuilder buf) {");
        if (data.isSubClass()) {
            addLine(3, "super.toString(buf);");
        }
        for (int i = 0; i < nonDerived.size(); i++) {
            PropertyGen prop = nonDerived.get(i);
            String getter = nonDerived.get(i).generateBuilderFieldName();
            data.ensureImport(JodaBeanUtils.class);
            addLine(3, "buf.append(\"" + prop.getData().getPropertyName() +
                    "\").append('=').append(JodaBeanUtils.toString(" + getter + ")).append(',').append(' ');");
        }
        addLine(2, "}");
        addBlankLine();
    }

    //-----------------------------------------------------------------------
    private void addLines(List<String> lines) {
        insertRegion.addAll(lines);
    }

    private void addLine(int tabCount, String line) {
        StringBuilder buf = new StringBuilder(line.length() + tabCount);
        for (int i = 0; i < tabCount; i++) {
            buf.append('\t');
        }
        buf.append(line);
        insertRegion.add(buf.toString());
    }

    private void addBlankLine() {
        insertRegion.add("");
    }

    private static String join(int i, List<?> list, String join, String end) {
        return (i < list.size() - 1 ? join : end);
    }

    private static String joinComma(int i, List<?> list, String end) {
        return join(i, list, ",", end);
    }

    boolean isBean() {
        return properties != null;
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
