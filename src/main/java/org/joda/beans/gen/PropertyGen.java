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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.impl.direct.DirectMetaProperty;

/**
 * A property parsed from the source file.
 * 
 * @author Stephen Colebourne
 */
class PropertyGen {

    /** The getter pattern. */
    private static final Pattern GETTER_PATTERN = Pattern.compile(".*[ ,(]get[ ]*[=][ ]*[\"]([a-zA-Z-]*)[\"].*");
    /** The setter pattern. */
    private static final Pattern SETTER_PATTERN = Pattern.compile(".*[ ,(]set[ ]*[=][ ]*[\"]([ !#-~]*)[\"].*");
    /** The type pattern. */
    private static final Pattern TYPE_PATTERN = Pattern.compile(".*[ ,(]type[ ]*[=][ ]*[\"]([a-zA-Z0-9_<>.]*)[\"].*");
    /** The validation pattern. */
    private static final Pattern VALIDATION_PATTERN = Pattern.compile(".*[ ,(]validate[ ]*[=][ ]*[\"]([a-zA-Z_.]*)[\"].*");

    /** Annotation line index for {@code PropertyDefinition} in input file. */
    private final int propertyIndex;
    /** Annotation line index in input file. */
    private final int annotationIndex;
    /** Field line index in input file. */
    private final int fieldIndex;
    /** The bean generator. */
    private final BeanGen bean;
    /** The data model of the property. */
    private final GeneratableProperty data;

    /**
     * Constructor.
     * @param bean  the bean generator
     * @param content  the lines, not null
     * @param lineIndex  the index of a PropertyDefinition
     * @param derived  true if derived
     */
    public PropertyGen(BeanGen bean, List<String> content, int lineIndex, boolean derived) {
        this.bean = bean;
        this.propertyIndex = lineIndex;
        this.annotationIndex = parseAnnotationStart(content, lineIndex);
        this.fieldIndex = parseCodeIndex(content);
        GeneratableProperty prop = new GeneratableProperty(bean.getData(), bean.getConfig());
        if (derived) {
            prop.setGetStyle("manual");
            prop.setSetStyle("");
            prop.setTypeStyle("");
            prop.setDeprecated(parseDeprecated(content));
            prop.setPropertyName(parseMethodNameAsPropertyName(content));
            prop.setUpperName(makeUpperName(prop.getPropertyName()));
            prop.setFieldType(parseMethodType(content));
            prop.setInitializer(parseFieldInitializer(content));
        } else {
            prop.setGetStyle(parseGetStyle(content));
            prop.setSetStyle(parseSetStyle(content));
            prop.setTypeStyle(parseTypeStyle(content));
            prop.setValidation(parseValidation(content));
            prop.setDeprecated(parseDeprecated(content));
            prop.setFieldName(parseFieldName(content));
            prop.setPropertyName(makePropertyName(bean, prop.getFieldName()));
            prop.setUpperName(makeUpperName(prop.getPropertyName()));
            prop.setFinal(parseFinal(content));
            prop.setFieldType(parseFieldType(content));
            prop.setInitializer(parseFieldInitializer(content));
        }
        prop.resolveType();
        prop.resolveGetterGen();
        prop.resolveSetterGen();
        prop.resolveCopyGen();
        prop.resolveBuilderGen();
        prop.resolveValidation();
        List<String> comments = parseComment(content, prop.getPropertyName());
        prop.setFirstComment(comments.get(0));
        prop.getComments().addAll(comments.subList(1, comments.size()));
        this.data = prop;
    }

    private String makePropertyName(BeanGen bean, String name) {
        if (name.startsWith(bean.getFieldPrefix())) {
            name = name.substring(bean.getFieldPrefix().length());
        }
        return name;
    }

    private String makeUpperName(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    //-----------------------------------------------------------------------
    private int parseAnnotationStart(List<String> content, int lineIndex) {
        while (lineIndex > 0 && content.get(lineIndex - 1).trim().startsWith("@")) {
            lineIndex--;
        }
        return lineIndex;
    }

    private int parseCodeIndex(List<String> content) {
        for (int index = propertyIndex; index < content.size(); index++) {
            if (content.get(index).trim().startsWith("@") == false) {
                if (content.get(index).trim().length() == 0) {
                    throw new RuntimeException("Unable to locate field for property at line " + (propertyIndex + 1) + ", found blank line");
                }
                return index;
            }
        }
        throw new RuntimeException("Unable to locate field for property at line " + (propertyIndex + 1));
    }

    private String parseGetStyle(List<String> content) {
        String line = content.get(propertyIndex).trim();
        Matcher matcher = GETTER_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "smart";
    }

    private String parseSetStyle(List<String> content) {
        String line = content.get(propertyIndex).trim();
        Matcher matcher = SETTER_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "smart";
    }

    private String parseTypeStyle(List<String> content) {
        String line = content.get(propertyIndex).trim();
        Matcher matcher = TYPE_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "smart";
    }

    private String parseValidation(List<String> content) {
        String line = content.get(propertyIndex).trim();
        Matcher matcher = VALIDATION_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "";
    }

    private boolean parseDeprecated(List<String> content) {
        for (int index = annotationIndex; index < fieldIndex; index++) {
            String line = content.get(index).trim();
            if (line.equals("@Deprecated") || line.startsWith("@Deprecated ")) {
                return true;
            }
        }
        return false;
    }

    //-----------------------------------------------------------------------
    private String parseFieldName(List<String> content) {
        String line = parseFieldDefinition(content);
        String[] parts = line.split(" ");
        String last = parts[parts.length - 1];
        if (last.endsWith(";") && last.length() > 1) {
            return last.substring(0, last.length() - 1);
        }
        throw new RuntimeException("Unable to locate field name at line " + (propertyIndex + 1));
    }

    private boolean parseFinal(List<String> content) {
        String line = parseFieldDefinition(content);
        String[] parts = line.split(" ");
        if (parts.length < 2) {
            throw new RuntimeException("Unable to locate field type at line " + (propertyIndex + 1));
        }
        if (parts[0].equals("final") || parts[1].equals("final") ||
                (parts.length >= 3 && parts[2].equals("final"))) {
            return true;
        }
        return false;
    }

    private String parseFieldType(List<String> content) {
        String line = parseFieldDefinition(content);
        String[] parts = line.split(" ");
        if (parts.length < 2) {
            throw new RuntimeException("Unable to locate field type at line " + (propertyIndex + 1));
        }
        int partsPos = parts.length - 2;
        String type = parts[partsPos];
        while (true) {
            int open = 0, openPos = 0, close = 0, closePos = 0;
            while ((openPos = type.indexOf('<', openPos)) >= 0) {
                open++;
                openPos++;
            }
            while ((closePos = type.indexOf('>', closePos)) >= 0) {
                close++;
                closePos++;
            }
            if (open == close) {
                break;
            }
            if (partsPos == 0) {
                throw new RuntimeException("Unable to locate field type at line " + (propertyIndex + 1) + ", mismatched generics");
            }
            partsPos--;
            type = parts[partsPos] + " " + type;
        }
        return type;
    }

    private String parseFieldDefinition(List<String> content) {
        String line = content.get(fieldIndex).trim();
        if (line.contains("//")) {
            line = line.substring(0, line.indexOf("//")).trim();
        }
        if (line.contains(" = ")) {
            line = line.substring(0, line.indexOf(" = ")).trim() + ";";
        }
        return line;
    }

    private String parseFieldInitializer(List<String> content) {
        String line = content.get(fieldIndex).trim();
        if (line.contains("//")) {
            line = line.substring(0, line.indexOf("//")).trim();
        }
        if (line.contains(" = ")) {
            line = line.substring(line.indexOf(" = ") + 3).trim();
            if (line.endsWith(";") == false) {
                throw new RuntimeException("Field line does not end with semi-colon");
            }
            return line.substring(0, line.length() - 1).trim();
        }
        return "";
    }

    //-----------------------------------------------------------------------
    private String parseMethodNameAsPropertyName(List<String> content) {
        String[] parts = parseMethodDefinition(content);
        if (parts[1].length() == 0 || Character.isUpperCase(parts[1].charAt(0)) == false) {
            throw new RuntimeException("@DerivedProperty method name invalid'");
        }
        return Character.toLowerCase(parts[1].charAt(0)) + parts[1].substring(1);
    }

    private String parseMethodType(List<String> content) {
        String[] parts = parseMethodDefinition(content);
        return parts[0];
    }

    private String[] parseMethodDefinition(List<String> content) {
        String line = content.get(fieldIndex).trim();
        if (line.startsWith("public ")) {
            line = line.substring(7).trim();
        } else if (line.startsWith("private ")) {
            line = line.substring(8).trim();
        } else if (line.startsWith("protected ")) {
            line = line.substring(10).trim();
        }
        String lineEnd = "() {";
        if (line.startsWith("abstract ")) {
            line = line.substring(9).trim();
            lineEnd = "();";
        } else if (line.startsWith("final ")) {
            line = line.substring(6).trim();
        } else if (line.startsWith("static ")) {
            throw new RuntimeException("@DerivedProperty method cannot be static");
        }
        int getIndex = line.indexOf(" get");
        if (getIndex < 0) {
            throw new RuntimeException("@DerivedProperty method must start with 'get'");
        }
        if (line.endsWith(lineEnd) == false) {
            throw new RuntimeException("@DerivedProperty method must end with '" + lineEnd + "'");
        }
        line = line.substring(0, line.length() - lineEnd.length());
        String[] split = new String[2];
        split[0] = line.substring(0, getIndex).trim();
        split[1] = line.substring(getIndex + 4).trim();
        return split;
    }

    //-----------------------------------------------------------------------
    private List<String> parseComment(List<String> content, String propertyName) {
        List<String> comments = new ArrayList<String>();
        String commentEnd = content.get(annotationIndex - 1).trim();
        if (commentEnd.equals("*/")) {
            int startCommentIndex = -1;
            for (int index = annotationIndex - 1; index >= 0; index--) {
                String line = content.get(index).trim();
                if (line.equals("/**")) {
                    startCommentIndex = index + 1;
                    break;
                }
            }
            if (startCommentIndex == -1) {
                throw new RuntimeException("Unable to locate comment start at line " + annotationIndex);
            }
            if (startCommentIndex < annotationIndex - 1) {
                for (int i = startCommentIndex; i < annotationIndex - 1; i++) {
                    String commentLine = content.get(i).trim();
                    if (commentLine.startsWith("*")) {
                        commentLine = commentLine.substring(1).trim();
                    }
                    if (commentLine.startsWith("@return") == false && commentLine.startsWith("@param") == false &&
                            commentLine.startsWith("@throws") == false && commentLine.startsWith("@exception") == false) {
                        comments.add(commentLine);
                    }
                }
                String firstLine = comments.get(0);
                if (firstLine.length() > 0) {
                    comments.set(0, firstLine.substring(0, 1).toLowerCase() + firstLine.substring(1));
                } else {
                    comments.remove(0);
                }
            }
        } else if (commentEnd.startsWith("/**") && commentEnd.endsWith("*/")) {
            int startPos = commentEnd.indexOf("/**") + 3;
            int endPos = commentEnd.lastIndexOf("*/");
            String comment = commentEnd.substring(startPos, endPos).trim();
            if (comment.length() > 0) {
                comments.add(comment.substring(0, 1).toLowerCase() + comment.substring(1));
            }
        }
        if (comments.size() == 0) {
            comments.add("the " + propertyName + ".");
        }
        return comments;
    }

    //-----------------------------------------------------------------------
    List<String> generateConstructorAssign(String fromBean) {
        return data.getCopyGen().generateCopyToImmutable("\t\t", fromBean, data);
    }

    //-----------------------------------------------------------------------
    List<String> generateMetaPropertyConstant() {
        data.getBean().ensureImport(MetaProperty.class);
        data.getBean().ensureImport(DirectMetaProperty.class);
        List<String> list = new ArrayList<String>();
        list.add("\t\t/**");
        list.add("\t\t * The meta-property for the {@code " + data.getPropertyName() + "} property.");
        list.add("\t\t */");
        if (data.isBeanGenericType()) {
            list.add("\t\t@SuppressWarnings({\"unchecked\", \"rawtypes\" })");
            list.add("\t\tprivate final MetaProperty<" + propertyType() + "> " + metaFieldName() +
                " = (DirectMetaProperty) DirectMetaProperty.of" + readWrite() + "(");
            list.add("\t\t\t\tthis, \"" + data.getPropertyName() + "\", " +
                data.getBean().getTypeRaw() + ".class, " + actualType() + ");");
        } else {
            String propertyType = propertyType();
            if (propertyType.length() == 1) {
                propertyType = "Object";
            }
            if (data.isGenericParamType()) {
                list.add("\t\t@SuppressWarnings({\"unchecked\", \"rawtypes\" })");
            }
            list.add("\t\tprivate final MetaProperty<" + propertyType + "> " + metaFieldName() +
                " = DirectMetaProperty.of" + readWrite() + "(");
            list.add("\t\t\t\tthis, \"" + data.getPropertyName() + "\", " +
                data.getBean().getTypeRaw() + ".class, " + actualType() + ");");
        }
        return list;
    }

    List<String> generateMetaPropertyGetCase() {
        List<String> list = new ArrayList<String>();
        list.add("\t\t\t\tcase " + data.getPropertyName().hashCode() + ":  // " + data.getPropertyName());
        list.add("\t\t\t\t\treturn " + metaFieldName() + ";");
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
        List<String> list = new ArrayList<String>();
        list.add("\t/**");
        list.add("\t * Gets the the {@code " + data.getPropertyName() + "} property.");
        for (String comment : data.getComments()) {
            list.add("\t * " + comment);
        }
        list.add("\t * @return the property, not null");
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
        List<String> list = new ArrayList<String>();
        String propertyType = propertyType();
        list.add("\t\t/**");
        list.add("\t\t * The meta-property for the {@code " + data.getPropertyName() + "} property.");
        if (data.isDeprecated()) {
            for (String comment : data.getComments()) {
                if (comment.contains("@deprecated")) {
                    list.add("\t\t * " + comment);
                }
            }
        }
        list.add("\t\t * @return the meta-property, not null");
        list.add("\t\t */");
        if (data.isDeprecated()) {
            list.add("\t\t@Deprecated");
        }
        list.add("\t\tpublic " + (data.getBean().isTypeFinal() ? "" : "final ") + "MetaProperty<" + propertyType + "> " + data.getPropertyName() + "() {");
        list.add("\t\t\treturn " + metaFieldName() + ";");
        list.add("\t\t}");
        list.add("");
        return list;
    }

    List<String> generatePropertyGetCase() {
        List<String> list = new ArrayList<String>();
        list.add("\t\t\t\tcase " + data.getPropertyName().hashCode() + ":  // " + data.getPropertyName());
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
        List<String> list = new ArrayList<String>();
        list.add("\t\t\t\tcase " + data.getPropertyName().hashCode() + ":  // " + data.getPropertyName());
        String setter = data.getSetterGen().generateSetInvoke(data);
        if (data.getStyle().isWritable() && setter != null) {
            list.add("\t\t\t\t\t((" + data.getBean().getTypeNoExtends() + ") bean)." + setter + "(" + castObject() + "newValue);");
            list.add("\t\t\t\t\treturn;");
        } else {
            list.add("\t\t\t\t\tif (quiet) {");
            list.add("\t\t\t\t\t\treturn;");
            list.add("\t\t\t\t\t}");
            list.add("\t\t\t\t\tthrow new UnsupportedOperationException(\"Property cannot be written: " + data.getPropertyName() + "\");");
        }
        return list;
    }

    //-----------------------------------------------------------------------
    List<String> generateBuilderField() {
        return data.getBuilderGen().generateField("\t\t", data);
    }

    List<String> generateBuilderConstructorAssign(String beanToCopyFrom) {
        return data.getCopyGen().generateCopyToMutable("\t\t\t", data, beanToCopyFrom);
    }

    List<String> generateBuilderFieldSet() {
        List<String> list = new ArrayList<String>();
        list.add("\t\t\t\tcase " + data.getPropertyName().hashCode() + ":  // " + data.getPropertyName());
        list.add("\t\t\t\t\tthis." + generateBuilderFieldName() + " = (" + propertyType(getBuilderType()) + ") newValue;");
        list.add("\t\t\t\t\tbreak;");
        return list;
    }

    String generateBuilderFieldName() {
        return data.getFieldName();
    }

    List<String> generateBuilderSetMethod() {
        List<String> list = new ArrayList<String>();
        list.add("\t\t/**");
        list.add("\t\t * Sets the {@code " + data.getPropertyName() + "} property in the builder.");
        list.add("\t\t * @param " + data.getPropertyName() + "  the new value" + data.getNotNullJavadoc());
        list.add("\t\t * @return this, for chaining, not null");
        if (data.isDeprecated()) {
            for (String comment : data.getComments()) {
                if (comment.contains("@deprecated")) {
                    list.add("\t\t * " + comment);
                }
            }
        }
        list.add("\t\t */");
        if (data.isDeprecated()) {
            list.add("\t\t@Deprecated");
        }
        list.add("\t\tpublic Builder" + data.getBean().getTypeGenericName(true) + " " + data.getPropertyName() +
                "(" + getBuilderType() + " " + data.getPropertyName() + ") {");
        if (data.isValidated()) {
            list.add("\t\t\t" + data.getValidationMethodName() + "(" + data.getPropertyName() + ", \"" + data.getPropertyName() + "\");");
        }
        list.add("\t\t\tthis." + generateBuilderFieldName() + " = " + data.getPropertyName() + ";");
        list.add("\t\t\treturn this;");
        list.add("\t\t}");
        list.add("");
        return list;
    }

    String getBuilderType() {
        return data.getBuilderGen().generateType(data);
    }

    //-----------------------------------------------------------------------
    private String readWrite() {
        switch (data.getStyle()) {
            case READ_WRITE:
                return "ReadWrite";
            case READ_ONLY:
                return "ReadOnly";
            case WRITE_ONLY:
                return "WriteOnly";
            case DERIVED:
                return "Derived";
            case IMMUTABLE:
                return "Immutable";
            default:
                break;
        }
        throw new RuntimeException("Invalid style");
    }

    private String actualType() {
        String pt = propertyType();
        if (pt.equals(data.getType())) {
            int genericStart = pt.indexOf('<');
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
        String pt = propertyType();
        if (pt.equals(data.getType())) {
            return "(" + pt + ") ";
        }
        return "(" + pt + ") ";
    }

    private String propertyType() {
        return propertyType(data.getType());
    }

    private String propertyType(String type) {
        if (type.equals("boolean")) {
            return "Boolean";
        }
        if (type.equals("byte")) {
            return "Byte";
        }
        if (type.equals("short")) {
            return "Short";
        }
        if (type.equals("char")) {
            return "Character";
        }
        if (type.equals("int")) {
            return "Integer";
        }
        if (type.equals("long")) {
            return "Long";
        }
        if (type.equals("float")) {
            return "Float";
        }
        if (type.equals("double")) {
            return "Double";
        }
        return type;
    }

    private String metaFieldName() {
        return bean.getFieldPrefix() + data.getPropertyName();
    }

    GeneratableProperty getData() {
        return data;
    }

}
