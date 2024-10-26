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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.impl.direct.DirectMetaProperty;

/**
 * A property parsed from the source file.
 */
class PropertyGen {

    /** The data model of the property. */
    private final PropertyData data;

    /**
     * Constructor.
     * @param propData  the property data
     */
    PropertyGen(PropertyData propData) {
        this.data = propData;
    }

    //-----------------------------------------------------------------------
    List<String> generateConstructorAssign(String fromBean) {
        return data.getCopyGen().generateCopyToImmutable("\t\t", fromBean, data);
    }

    //-----------------------------------------------------------------------
    List<String> generateMetaPropertyConstant() {
        data.getBean().ensureImport(MetaProperty.class);
        data.getBean().ensureImport(DirectMetaProperty.class);
        var list = new ArrayList<String>();
        list.add("\t\t/**");
        list.add("\t\t * The meta-property for the {@code " + data.getPropertyName() + "} property.");
        list.add("\t\t */");
        if (data.isBeanGenericType()) {
            list.add("\t\t@SuppressWarnings({\"unchecked\", \"rawtypes\" })");
            list.add("\t\tprivate final MetaProperty<" + propertyType() + "> " + data.getMetaFieldName() +
                " = (DirectMetaProperty) DirectMetaProperty.of" + readWrite() + "(");
            list.add("\t\t\t\tthis, \"" + data.getPropertyName() + "\", " +
                data.getBean().getTypeRaw() + ".class, " + actualType() + ");");
        } else {
            var propertyType = propertyType();
            if (propertyType.length() == 1) {
                propertyType = "Object";
            }
            if (data.isGenericParamType()) {
                list.add("\t\t@SuppressWarnings({\"unchecked\", \"rawtypes\" })");
            }
            list.add("\t\tprivate final MetaProperty<" + propertyType + "> " + data.getMetaFieldName() +
                " = DirectMetaProperty.of" + readWrite() + "(");
            list.add("\t\t\t\tthis, \"" + data.getPropertyName() + "\", " +
                data.getBean().getTypeRaw() + ".class, " + actualType() + ");");
        }
        return list;
    }

    List<String> generateMetaPropertyGetCase() {
        var list = new ArrayList<String>();
        list.add("\t\t\t\tcase " + data.getPropertyName().hashCode() + ":  // " + data.getPropertyName());
        if (data.getAlias() != null) {
            list.add("\t\t\t\tcase " + data.getAlias().hashCode() + ":  // " + data.getAlias() + " (alias)");
        }
        list.add("\t\t\t\t\treturn " + data.getMetaFieldName() + ";");
        return list;
    }

    List<String> generateGetter() {
        return data.getGetterGen().generateGetter(data);
    }

    List<String> generateSetter() {
        return data.getSetterGen().generateSetter("\t", data);
    }

    List<String> generateProperty() {
        data.getBean().ensureImport(Property.class);
        var list = new ArrayList<String>();
        list.add("\t/**");
        list.add("\t * Gets the the {@code " + data.getPropertyName() + "} property.");
        for (var comment : data.getComments()) {
            list.add("\t * " + comment);
        }
        list.add("\t * @return the property, not null");
        if (data.getDeprecatedComment() != null) {
            list.add("\t * " + data.getDeprecatedComment());
        }
        list.add("\t */");
        if (data.isDeprecated()) {
            list.add("\t@Deprecated");
        }
        list.add("\tpublic " + (data.getBean().isTypeFinal() ? "" : "final ") + "Property<" + propertyType() + "> " + data.getPropertyName() + "() {");
        list.add("\t\treturn metaBean()." + data.getPropertyName() + "().createProperty(this);");
        list.add("\t}");
        list.add("");
        return list;
    }

    List<String> generateMetaProperty() {
        var list = new ArrayList<String>();
        var propertyType = propertyType();
        list.add("\t\t/**");
        list.add("\t\t * The meta-property for the {@code " + data.getPropertyName() + "} property.");
        list.add("\t\t * @return the meta-property, not null");
        if (data.getDeprecatedComment() != null) {
            list.add("\t\t * " + data.getDeprecatedComment());
        }
        list.add("\t\t */");
        if (data.isDeprecated()) {
            list.add("\t\t@Deprecated");
        }
        list.add("\t\tpublic " + (data.getBean().isTypeFinal() ? "" : "final ") + "MetaProperty<" + propertyType + "> " + data.getPropertyName() + "() {");
        list.add("\t\t\treturn " + data.getMetaFieldName() + ";");
        list.add("\t\t}");
        list.add("");
        return list;
    }

    List<String> generatePropertyGetCase() {
        var list = new ArrayList<String>();
        list.add("\t\t\t\tcase " + data.getPropertyName().hashCode() + ":  // " + data.getPropertyName());
        if (data.getAlias() != null) {
            list.add("\t\t\t\tcase " + data.getAlias().hashCode() + ":  // " + data.getAlias() + " (alias)");
        }
        if (data.getStyle().isReadable()) {
            list.add("\t\t\t\t\treturn ((" + data.getBean().getTypeWildcard() + ") bean)." + data.getGetterGen().generateGetInvoke(data) + ";");
        } else {
            list.add("\t\t\t\t\tif (quiet) {");
            list.add("\t\t\t\t\t\treturn null;");
            list.add("\t\t\t\t\t}");
            list.add("\t\t\t\t\tthrow new UnsupportedOperationException(\"Property cannot be read: " + data.getPropertyName() + "\");");
        }
        return list;
    }

    List<String> generatePropertySetCase() {
        var list = new ArrayList<String>();
        list.add("\t\t\t\tcase " + data.getPropertyName().hashCode() + ":  // " + data.getPropertyName());
        if (data.getAlias() != null) {
            list.add("\t\t\t\tcase " + data.getAlias().hashCode() + ":  // " + data.getAlias() + " (alias)");
        }
        var setter = data.getSetterGen().generateSetInvoke(data, castObject() + "newValue");
        if (data.getStyle().isWritable() && setter != null) {
            list.add("\t\t\t\t\t((" + data.getBean().getTypeNoExtends() + ") bean)." + setter + ";");
            list.add("\t\t\t\t\treturn;");
        } else {
            list.add("\t\t\t\t\tif (quiet) {");
            list.add("\t\t\t\t\t\treturn;");
            list.add("\t\t\t\t\t}");
            list.add("\t\t\t\t\tthrow new UnsupportedOperationException(\"Property cannot be written: " + data.getPropertyName() + "\");");
        }
        return list;
    }

    String generateLambdaGetter() {
        return "b -> b." + data.getGetterGen().generateGetInvoke(data);
    }

    String generateLambdaSetter() {
        var propType = propertyType(data.getTypeBeanErased());
        var cast = propType.equals("Object") ? "" : "(" + propType + ") ";
        return "(b, v) -> b." + data.getSetterGen().generateSetInvoke(data, cast + "v");
    }

    //-----------------------------------------------------------------------
    boolean isSpecialInit() {
        return data.getBuilderGen().isSpecialInit(data);
    }

    String generateInit() {
        return data.getBuilderGen().generateInit(data);
    }

    List<String> generateBuilderField() {
        return data.getBuilderGen().generateField("\t\t", data);
    }

    List<String> generateBuilderConstructorAssign(String beanToCopyFrom) {
        return data.getCopyGen().generateCopyToMutable("\t\t\t", data, beanToCopyFrom);
    }

    List<String> generateBuilderFieldGet() {
        var list = new ArrayList<String>();
        list.add("\t\t\t\tcase " + data.getPropertyName().hashCode() + ":  // " + data.getPropertyName());
        if (data.getAlias() != null) {
            list.add("\t\t\t\tcase " + data.getAlias().hashCode() + ":  // " + data.getAlias() + " (alias)");
        }
        list.add("\t\t\t\t\treturn " + generateBuilderFieldName() + ";");
        return list;
    }

    List<String> generateBuilderFieldSet() {
        var list = new ArrayList<String>();
        list.add("\t\t\t\tcase " + data.getPropertyName().hashCode() + ":  // " + data.getPropertyName());
        if (data.getAlias() != null) {
            list.add("\t\t\t\tcase " + data.getAlias().hashCode() + ":  // " + data.getAlias() + " (alias)");
        }
        list.add("\t\t\t\t\tthis." + generateBuilderFieldName() + " = (" + propertyType(getBuilderType()) + ") newValue;");
        list.add("\t\t\t\t\tbreak;");
        return list;
    }

    String generateBuilderFieldName() {
        return data.getFieldName();
    }

    List<String> generateBuilderSetMethod() {
        var list = new ArrayList<String>();
        list.add("\t\t/**");
        list.add("\t\t * Sets " + data.getFirstComment());
        for (var comment : data.getComments()) {
            list.add("\t\t * " + comment);
        }
        list.add("\t\t * @param " + data.getPropertyName() + "  the new value" + data.getNotNullJavadoc());
        list.add("\t\t * @return this, for chaining, not null");
        if (data.getDeprecatedComment() != null) {
            list.add("\t\t * " + data.getDeprecatedComment());
        }
        list.add("\t\t */");
        if (data.isDeprecated()) {
            list.add("\t\t@Deprecated");
        }
        var builderType = getBuilderType();
        if (builderType.endsWith("[]") && !builderType.endsWith("[][]") && !builderType.equals("byte[]")) {
            list.add("\t\tpublic Builder" + data.getBean().getTypeGenericName(true) + " " + data.getPropertyName() +
                    "(" + builderType.substring(0, builderType.length() - 2) + "... " + data.getPropertyName() + ") {");
        } else {
            list.add("\t\tpublic Builder" + data.getBean().getTypeGenericName(true) + " " + data.getPropertyName() +
                    "(" + builderType + " " + data.getPropertyName() + ") {");
        }
        if (data.isValidated()) {
            list.add("\t\t\t" + data.getValidationMethodName() + "(" + data.getPropertyName() + ", \"" + data.getPropertyName() + "\");");
        }
        list.add("\t\t\tthis." + generateBuilderFieldName() + " = " + data.getPropertyName() + ";");
        list.add("\t\t\treturn this;");
        list.add("\t\t}");
        list.add("");
        generateBuilderSetCollectionMethod(list);
        return list;
    }

    String getBuilderType() {
        return data.getBuilderGen().generateType(data);
    }

    private void generateBuilderSetCollectionMethod(List<String> list) {
        var code = data.getVarArgsCode();
        if (code == null) {
            return;
        }
        var argType = data.getTypeGenericsSimple();
        if (argType.equals("?")) {
            argType = "Object";
        }
        if (argType.startsWith("? extends ")) {
            argType = argType.substring(10);
        }
        var safeVarargs = argType.length() == 1 || argType.contains("<");
        // generate based on varargs
        list.add("\t\t/**");
        list.add("\t\t * Sets the {@code " + data.getPropertyName() + "} property in the builder");
        list.add("\t\t * from an array of objects.");
        list.add("\t\t * @param " + data.getPropertyName() + "  the new value" + data.getNotNullJavadoc());
        list.add("\t\t * @return this, for chaining, not null");
        if (data.getDeprecatedComment() != null) {
            list.add("\t\t * " + data.getDeprecatedComment());
        }
        list.add("\t\t */");
        if (data.isDeprecated()) {
            list.add("\t\t@Deprecated");
        }
        if (safeVarargs) {
            list.add("\t\t@SafeVarargs");
        }
        list.add("\t\tpublic " + (safeVarargs ? "final " : "") +
                "Builder" + data.getBean().getTypeGenericName(true) + " " + data.getPropertyName() +
                "(" + argType + "... " + data.getPropertyName() + ") {");
        if (code.contains("Arrays.asList")) {
            data.getBean().ensureImport(Arrays.class);
        }
        code = code.replace("$value", data.getPropertyName());
        code = code.replace("<>", data.getTypeGenerics());
        list.add("\t\t\treturn " + data.getPropertyName() + "(" + code + ");");
        list.add("\t\t}");
        list.add("");
    }

    //-----------------------------------------------------------------------
    private String readWrite() {
        return switch (data.getStyle()) {
            case READ_WRITE -> "ReadWrite";
            case READ_ONLY -> "ReadOnly";
            case WRITE_ONLY -> "WriteOnly";
            case DERIVED -> "Derived";
            case READ_ONLY_BUILDABLE -> "ReadOnlyBuildable";
            case IMMUTABLE -> "Immutable";
            default -> throw new RuntimeException("Invalid style");
        };
    }

    private String actualType() {
        var pt = propertyType();
        if (pt.equals(data.getType())) {
            var genericStart = pt.indexOf('<');
            if (genericStart >= 0) {
                return "(Class) " + pt.substring(0, genericStart) + ".class";
            }
            if (data.getType().length() == 1) {
                return "Object.class";
            }
            if (data.isGenericArrayType()) {
                return "Object[].class";
            }
            return pt + ".class";
        }
        return pt + ".TYPE";
    }

    private String castObject() {
        var pt = propertyType();
        return "(" + pt + ") ";
    }

    private String propertyType() {
        return propertyType(data.getType());
    }

    private String propertyType(String type) {
        return switch (type) {
            case "boolean" -> "Boolean";
            case "byte" -> "Byte";
            case "short" -> "Short";
            case "char" -> "Character";
            case "int" -> "Integer";
            case "long" -> "Long";
            case "float" -> "Float";
            case "double" -> "Double";
            default -> type;
        };
    }

    PropertyData getData() {
        return data;
    }

    // resolves awkward generics
    static String resolveWildcard(String input) {
        return input.equals("<?>") ? "<Object>" : input;
    }

}
