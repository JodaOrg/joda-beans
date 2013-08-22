/*
 *  Copyright 2001-2013 Stephen Colebourne
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * Code generator for a bean.
 * 
 * @author Stephen Colebourne
 */
class BeanGen {

    /** Start marker. */
    private static final String AUTOGENERATED_START = "\t//------------------------- AUTOGENERATED START -------------------------";
    /** End marker. */
    private static final String AUTOGENERATED_END = "\t//-------------------------- AUTOGENERATED END --------------------------";
    /** Pattern to find bean type. */
    private static final Pattern BEAN_TYPE = Pattern.compile(".*class (([A-Z][A-Za-z0-9_]+)(?:<([A-Z])( extends [A-Za-z0-9_]+)?>)?) .*");
    /** Pattern to find super type. */
    private static final Pattern SUPER_TYPE = Pattern.compile(".* extends (([A-Z][A-Za-z0-9_]+)(?:<([A-Z][A-Za-z0-9_<> ]*)>)?).*");
    /** Pattern to find super type. */
    private static final Set<String> PRIMITIVE_EQUALS = new HashSet<String>();
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
    private final List<String> content;
    /** The indent. */
    private final String indent;
    /** The prefix. */
    private final String prefix;
    /** The start position of auto-generation. */
    private final int autoStartIndex;
    /** The end position of auto-generation. */
    private final int autoEndIndex;
    /** The region to insert into. */
    private final List<String> insertRegion;
    /** The list of property generators. */
    private final List<PropertyGen> properties;
    /** The data model of the bean. */
    private final GeneratableBean data;

    /**
     * Constructor.
     * @param content  the content to process, not null
     * @param indent  the indent to use, not null
     * @param prefix  the prefix to use, not null
     */
    BeanGen(List<String> content, String indent, String prefix) {
        this.content = content;
        this.indent = indent;
        this.prefix = prefix;
        int beanDefIndex = parseBeanDefinition();
        if (beanDefIndex >= 0) {
            this.data = new GeneratableBean();
            this.data.getCurrentImports().addAll(parseImports(beanDefIndex));
            this.data.setImportInsertLocation(parseImportLocation(beanDefIndex));
            this.data.setConstructable(parseConstructable(beanDefIndex));
            this.data.setTypeParts(parseBeanType(beanDefIndex));
            this.data.setSuperTypeParts(parseBeanSuperType(beanDefIndex));
            this.properties = parseProperties(data);
            this.autoStartIndex = parseStartAutogen();
            this.autoEndIndex = parseEndAutogen();
            this.insertRegion = content.subList(autoStartIndex + 1, autoEndIndex);
            this.data.setManualEqualsHashCode(parseManualEqualsHashCode(beanDefIndex));
            this.data.setManualToStringCode(parseManualToStringCode(beanDefIndex));
        } else {
            this.autoStartIndex = -1;
            this.autoEndIndex = -1;
            this.insertRegion = null;
            this.data = null;
            this.properties = null;
        }
    }

    //-----------------------------------------------------------------------
    void process() {
        if (insertRegion != null) {
            removeOld();
            if (data.isSubclass() == false) {
                data.ensureImport(DirectBean.class);
            }
            insertRegion.add("\t///CLOVER:OFF");
            generateMeta();
            generateMetaBean();
            generatePropertyGet();
            generatePropertySet();
            if (data.isManualEqualsHashCode() == false) {
                generateEquals();
                generateHashCode();
            }
            if (data.isManualToStringCode() == false) {
                generateToString();
            }
            generateGettersSetters();
            generateMetaClass();
            insertRegion.add("\t///CLOVER:ON");
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
            it.set(it.next().replace("\t", indent));
        }
    }

    //-----------------------------------------------------------------------
    private int parseBeanDefinition() {
        for (int index = 0; index < content.size(); index++) {
            String line = content.get(index).trim();
            if (line.startsWith("@BeanDefinition")) {
                return index;
            }
        }
        return -1;
    }

    private Set<String> parseImports(int defLine) {
        Set<String> imports = new HashSet<String>();
        for (int index = 0; index < defLine; index++) {
            if (content.get(index).startsWith("import ")) {
                String imp = content.get(index).substring(7).trim();
                imp = imp.substring(0, imp.indexOf(';'));
                if (imp.endsWith(".*") == false) {
                    imports.add(imp);
                }
            }
        }
        return imports;
    }

    private int parseImportLocation(int defLine) {
        int location = 0;
        for (int index = 0; index < defLine; index++) {
            if (content.get(index).startsWith("import ") || content.get(index).startsWith("package ")) {
                location = index;
            }
        }
        return location;
    }

    private boolean parseConstructable(int defLine) {
        for (int index = defLine; index < content.size(); index++) {
            if (content.get(index).contains(" abstract class ")) {
                return false;
            }
        }
        return true;
    }

    private String[] parseBeanType(int defLine) {
        Matcher matcher = BEAN_TYPE.matcher("");
        for (int index = defLine; index < content.size(); index++) {
            matcher.reset(content.get(index));
            if (matcher.matches()) {
                int startName = matcher.start(1);
                String fnl = content.get(index).substring(0, startName).contains(" final ") ? " final " : null;
                return new String[] {fnl, matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4)};
            }
        }
        throw new RuntimeException("Unable to locate bean class name");
    }

    private String[] parseBeanSuperType(int defLine) {
        Matcher matcher = SUPER_TYPE.matcher("");
        for (int index = defLine; index < content.size(); index++) {
            matcher.reset(content.get(index));
            if (matcher.matches()) {
                return new String[] {matcher.group(1), matcher.group(2), matcher.group(3)};
            }
        }
        throw new RuntimeException("Unable to locate bean superclass");
    }

    private List<PropertyGen> parseProperties(GeneratableBean data) {
        List<PropertyGen> props = new ArrayList<PropertyGen>();
        for (int index = 0; index < content.size(); index++) {
            String line = content.get(index).trim();
            if (line.startsWith("@PropertyDefinition")) {
                PropertyGen prop = new PropertyGen(this, content, index, false);
                props.add(prop);
                data.getProperties().add(prop.getData());
            } else if (line.startsWith("@DerivedProperty")) {
                PropertyGen prop = new PropertyGen(this, content, index, true);
                props.add(prop);
                data.getProperties().add(prop.getData());
            }
        }
        return props;
    }

    private int parseStartAutogen() {
        for (int index = 0; index < content.size(); index++) {
            String line = content.get(index).trim();
            if (line.contains(" AUTOGENERATED START ")) {
                content.set(index, AUTOGENERATED_START);
                return index;
            }
        }
        for (int index = content.size() - 1; index >= 0; index--) {
            String line = content.get(index).trim();
            if (line.equals("}")) {
                content.add(index, AUTOGENERATED_START);
                return index;
            }
            if (line.length() > 0) {
                break;
            }
        }
        throw new RuntimeException("Unable to locate start autogeneration point");
    }

    private int parseEndAutogen() {
        for (int index = autoStartIndex; index < content.size(); index++) {
            String line = content.get(index).trim();
            if (line.contains(" AUTOGENERATED END ")) {
                content.set(index, AUTOGENERATED_END);
                return index;
            }
        }
        content.add(autoStartIndex + 1, AUTOGENERATED_END);
        return autoStartIndex + 1;
    }

    private void removeOld() {
        insertRegion.clear();
    }

    private boolean parseManualEqualsHashCode(int defLine) {
        for (int index = defLine; index < autoStartIndex; index++) {
            String line = content.get(index).trim();
            if (line.equals("public int hashCode() {") || (line.startsWith("public boolean equals(") && line.endsWith(") {"))) {
                return true;
            }
        }
        for (int index = autoEndIndex; index < content.size(); index++) {
            String line = content.get(index).trim();
            if (line.equals("public int hashCode() {") || (line.startsWith("public boolean equals(") && line.endsWith(") {"))) {
                return true;
            }
        }
        return false;
    }

    private boolean parseManualToStringCode(int defLine) {
        for (int index = defLine; index < autoStartIndex; index++) {
            String line = content.get(index).trim();
            if (line.equals("public String toString() {")) {
                return true;
            }
        }
        for (int index = autoEndIndex; index < content.size(); index++) {
            String line = content.get(index).trim();
            if (line.equals("public String toString() {")) {
                return true;
            }
        }
        return false;
    }

    //-----------------------------------------------------------------------
    private void generateSeparator() {
        insertRegion.add("\t//-----------------------------------------------------------------------");
    }

    private void generateMeta() {
        data.ensureImport(JodaBeanUtils.class);
        // this cannot be generified without either Eclipse or javac complaining
        // raw types forever
        insertRegion.add("\t/**");
        insertRegion.add("\t * The meta-bean for {@code " + data.getTypeRaw() + "}.");
        if (data.isTypeGeneric()) {
            insertRegion.add("\t * @return the meta-bean, not null");
            insertRegion.add("\t */");
            insertRegion.add("\t@SuppressWarnings(\"rawtypes\")");
            insertRegion.add("\tpublic static " + data.getTypeRaw() + ".Meta meta() {");
        } else {
            insertRegion.add("\t * @return the meta-bean, not null");
            insertRegion.add("\t */");
            insertRegion.add("\tpublic static " + data.getTypeRaw() + ".Meta meta() {");
        }
        insertRegion.add("\t\treturn " + data.getTypeRaw() + ".Meta.INSTANCE;");
        insertRegion.add("\t}");
        
        if (data.isTypeGeneric()) {
            // this works around an Eclipse bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=397462
            // long name needed for uniqueness as static overriding is borked
            insertRegion.add("");
            insertRegion.add("\t/**");
            insertRegion.add("\t * The meta-bean for {@code " + data.getTypeRaw() + "}.");
            insertRegion.add("\t * @param <R>  the bean's generic type");
            insertRegion.add("\t * @param cls  the bean's generic type");
            insertRegion.add("\t * @return the meta-bean, not null");
            insertRegion.add("\t */");
            insertRegion.add("\t@SuppressWarnings(\"unchecked\")");
            insertRegion.add("\tpublic static <R" + data.getTypeGenericExtends() + "> " + data.getTypeRaw() +
                    ".Meta<R> meta" + data.getTypeRaw() + "(Class<R> cls) {");
            insertRegion.add("\t\treturn " + data.getTypeRaw() + ".Meta.INSTANCE;");
            insertRegion.add("\t}");
        }
        
        insertRegion.add("");
        insertRegion.add("\tstatic {");
        insertRegion.add("\t\tJodaBeanUtils.registerMetaBean(" + data.getTypeRaw() + ".Meta.INSTANCE);");
        insertRegion.add("\t}");
        insertRegion.add("");
    }

    private void generateMetaBean() {
        if (data.isTypeGeneric()) {
            insertRegion.add("\t@SuppressWarnings(\"unchecked\")");
        }
        insertRegion.add("\t@Override");
        insertRegion.add("\tpublic " + data.getTypeRaw() + ".Meta" + data.getTypeGenericName(true) + " metaBean() {");
        insertRegion.add("\t\treturn " + data.getTypeRaw() + ".Meta.INSTANCE;");
        insertRegion.add("\t}");
        insertRegion.add("");
    }

    private void generateGettersSetters() {
        for (PropertyGen prop : properties) {
            generateSeparator();
            insertRegion.addAll(prop.generateGetter());
            insertRegion.addAll(prop.generateSetter());
            insertRegion.addAll(prop.generateProperty());
        }
    }

    private void generatePropertyGet() {
        insertRegion.add("\t@Override");
        insertRegion.add("\tprotected Object propertyGet(String propertyName, boolean quiet) {");
        if (properties.size() > 0) {
            insertRegion.add("\t\tswitch (propertyName.hashCode()) {");
            for (PropertyGen prop : properties) {
                insertRegion.addAll(prop.generatePropertyGetCase());
            }
            insertRegion.add("\t\t}");
        }
        insertRegion.add("\t\treturn super.propertyGet(propertyName, quiet);");
        insertRegion.add("\t}");
        insertRegion.add("");
    }

    private void generatePropertySet() {
        boolean generics = false;
        for (GeneratableProperty prop : data.getProperties()) {
            generics |= (prop.getReadWrite().isWritable() && prop.isGeneric() && prop.isGenericWildcardParamType() == false);
        }
        if (generics) {
            insertRegion.add("\t@SuppressWarnings(\"unchecked\")");
        }
        insertRegion.add("\t@Override");
        insertRegion.add("\tprotected void propertySet(String propertyName, Object newValue, boolean quiet) {");
        if (properties.size() > 0) {
            insertRegion.add("\t\tswitch (propertyName.hashCode()) {");
            for (PropertyGen prop : properties) {
                insertRegion.addAll(prop.generatePropertySetCase());
            }
            insertRegion.add("\t\t}");
        }
        insertRegion.add("\t\tsuper.propertySet(propertyName, newValue, quiet);");
        insertRegion.add("\t}");
        insertRegion.add("");
    }

    //-----------------------------------------------------------------------
    private void generateEquals() {
        data.ensureImport(JodaBeanUtils.class);
        insertRegion.add("\t@Override");
        insertRegion.add("\tpublic boolean equals(Object obj) {");
        insertRegion.add("\t\tif (obj == this) {");
        insertRegion.add("\t\t\treturn true;");
        insertRegion.add("\t\t}");
        insertRegion.add("\t\tif (obj != null && obj.getClass() == this.getClass()) {");
        if (properties.size() == 0) {
            if (data.isSubclass()) {
                insertRegion.add("\t\t\treturn super.equals(obj);");
            } else {
                insertRegion.add("\t\t\treturn true;");
            }
        } else {
            insertRegion.add("\t\t\t" + data.getTypeWildcard() + " other = (" + data.getTypeWildcard() + ") obj;");
            for (int i = 0; i < properties.size(); i++) {
                PropertyGen prop = properties.get(i);
                String getter = GetterGen.of(prop.getData()).generateGetInvoke(prop.getData());
                String equals = "JodaBeanUtils.equal(" + getter + ", other." + getter + ")";
                if (PRIMITIVE_EQUALS.contains(prop.getData().getType())) {
                    equals = "(" + getter + " == other." + getter + ")";
                }
                insertRegion.add(
                        (i == 0 ? "\t\t\treturn " : "\t\t\t\t\t") + equals +
                        (data.isSubclass() || i < properties.size() - 1 ? " &&" : ";"));
            }
            if (data.isSubclass()) {
                insertRegion.add("\t\t\t\t\tsuper.equals(obj);");
            }
        }
        insertRegion.add("\t\t}");
        insertRegion.add("\t\treturn false;");
        insertRegion.add("\t}");
        insertRegion.add("");
    }

    private void generateHashCode() {
        data.ensureImport(JodaBeanUtils.class);
        insertRegion.add("\t@Override");
        insertRegion.add("\tpublic int hashCode() {");
        if (data.isSubclass()) {
            insertRegion.add("\t\tint hash = 7;");
        } else {
            insertRegion.add("\t\tint hash = getClass().hashCode();");
        }
        for (int i = 0; i < properties.size(); i++) {
            PropertyGen prop = properties.get(i);
            String getter = GetterGen.of(prop.getData()).generateGetInvoke(prop.getData());
            insertRegion.add("\t\thash += hash * 31 + JodaBeanUtils.hashCode(" + getter + ");");
        }
        if (data.isSubclass()) {
            insertRegion.add("\t\treturn hash ^ super.hashCode();");
        } else {
            insertRegion.add("\t\treturn hash;");
        }
        insertRegion.add("\t}");
        insertRegion.add("");
    }

    private void generateToString() {
        if (data.isSubclass() == false && data.isTypeFinal()) {
            insertRegion.add("\t@Override");
            insertRegion.add("\tpublic String toString() {");
            insertRegion.add("\t\tStringBuilder buf = new StringBuilder(" + (properties.size() * 32 + 32) + ");");
            insertRegion.add("\t\tbuf.append(getClass().getSimpleName());");
            insertRegion.add("\t\tbuf.append('{');");
            for (int i = 0; i < properties.size(); i++) {
                PropertyGen prop = properties.get(i);
                String getter = GetterGen.of(prop.getData()).generateGetInvoke(prop.getData());
                if (i < properties.size() - 1) {
                    insertRegion.add("\t\tbuf.append(\"" + prop.getData().getPropertyName() +
                            "\").append('=').append(" + getter + ").append(',').append(' ');");
                } else {
                    insertRegion.add("\t\tbuf.append(\"" + prop.getData().getPropertyName() +
                            "\").append('=').append(" + getter + ");");
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
        insertRegion.add("\t\tStringBuilder buf = new StringBuilder(" + (properties.size() * 32 + 32) + ");");
        insertRegion.add("\t\tbuf.append(getClass().getSimpleName());");
        insertRegion.add("\t\tbuf.append('{');");
        insertRegion.add("\t\tint len = buf.length();");
        insertRegion.add("\t\ttoString(buf);");
        insertRegion.add("\t\tif (buf.length() > len) {");
        insertRegion.add("\t\t\tbuf.setLength(buf.length() - 2);");
        insertRegion.add("\t\t}");
        insertRegion.add("\t\tbuf.append('}');");
        insertRegion.add("\t\treturn buf.toString();");
        insertRegion.add("\t}");
        insertRegion.add("");
        
        if (data.isSubclass()) {
            insertRegion.add("\t@Override");
        }
        insertRegion.add("\tprotected void toString(StringBuilder buf) {");
        if (data.isSubclass()) {
            insertRegion.add("\t\tsuper.toString(buf);");
        }
        for (int i = 0; i < properties.size(); i++) {
            PropertyGen prop = properties.get(i);
            String getter = GetterGen.of(prop.getData()).generateGetInvoke(prop.getData());
            insertRegion.add("\t\tbuf.append(\"" + prop.getData().getPropertyName() +
                    "\").append('=').append(" + getter + ").append(',').append(' ');");
        }
        insertRegion.add("\t}");
        insertRegion.add("");
    }

    //-----------------------------------------------------------------------
    private void generateMetaClass() {
        generateSeparator();
        insertRegion.add("\t/**");
        insertRegion.add("\t * The meta-bean for {@code " + data.getTypeRaw() + "}.");
        insertRegion.add("\t */");
        String superMeta;
        if (data.isSubclass()) {
            superMeta = data.getSuperTypeRaw() + ".Meta" + data.getSuperTypeGeneric(true);
        } else {
            data.ensureImport(DirectMetaBean.class);
            superMeta = "DirectMetaBean";
        }
        if (data.isTypeGeneric()) {
            insertRegion.add("\tpublic static class Meta" + data.getTypeGeneric(true) + " extends " + superMeta + " {");
        } else {
            insertRegion.add("\tpublic static class Meta extends " + superMeta + " {");
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
        insertRegion.add("\t\tprotected Meta() {");
        insertRegion.add("\t\t}");
        insertRegion.add("");
        generateMetaPropertyGet();
        generateMetaBuilder();
        generateMetaBeanType();
        generateMetaPropertyMap();
        if (data.isValidated()) {
            generateMetaValidate();
        }
        insertRegion.add("\t\t//-----------------------------------------------------------------------");
        generateMetaPropertyMethods();
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
        insertRegion.add("\t\tprivate final Map<String, MetaProperty<?>> " + prefix + "metaPropertyMap$ = new DirectMetaPropertyMap(");
        if (data.isSubclass()) {
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
        data.ensureImport(BeanBuilder.class);
        insertRegion.add("\t\t@Override");
        insertRegion.add("\t\tpublic BeanBuilder<? extends " + data.getTypeNoExtends() + "> builder() {");
        if (data.isConstructable()) {
            data.ensureImport(DirectBeanBuilder.class);
            insertRegion.add("\t\t\treturn new DirectBeanBuilder<" + data.getTypeNoExtends() + ">(new " + data.getTypeNoExtends() + "());");
        } else {
            insertRegion.add("\t\t\tthrow new UnsupportedOperationException(\"" + data.getTypeRaw() + " is an abstract class\");");
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
        insertRegion.add("\t\t\treturn " + prefix + "metaPropertyMap$;");
        insertRegion.add("\t\t}");
        insertRegion.add("");
    }

    private void generateMetaValidate() {
        data.ensureImport(DirectBean.class);
        insertRegion.add("\t\t@Override");
        insertRegion.add("\t\tprotected void validate(DirectBean bean) {");
        if (data.isValidated()) {
            for (PropertyGen prop : properties) {
                if (prop.getData().isValidated()) {
                    insertRegion.add("\t\t\t" + prop.getData().getValidationMethodName() +
                            "(((" + data.getTypeWildcard() + ") bean)." + prop.getData().getFieldName() +
                            ", \"" + prop.getData().getPropertyName() + "\");");
                }
            }
        }
        if (data.isSubclass()) {
            insertRegion.add("\t\t\tsuper.validate(bean);");
        }
        insertRegion.add("\t\t}");
        insertRegion.add("");
    }

    private void generateMetaPropertyMethods() {
        for (PropertyGen prop : properties) {
            insertRegion.addAll(prop.generateMetaProperty());
        }
    }

    //-----------------------------------------------------------------------
    boolean isBean() {
        return data != null;
    }

    GeneratableBean getData() {
        return data;
    }

    String getFieldPrefix() {
        return prefix;
    }

}
