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
package org.joda.beans.gen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.beans.PropertyReadWrite;

/**
 * A property parsed from the source file.
 * 
 * @author Stephen Colebourne
 */
class PropertyGen {

    /** Annotation line index in input file. */
    private final int annotationIndex;
    /** Field line index in input file. */
    private final int fieldIndex;
    /** Property name. */
    private final String propertyName;
    /** Field name. */
    private final String fieldName;
    /** Upper property name. */
    private final String upperName;
    /** Property type. */
    private final String type;
    /** Read-write type. */
    private final PropertyReadWrite readWrite;
    /** Deprecated flag. */
    private final boolean deprecated;
    /** First comment about the property. */
    private final String firstComment;
    /** Other comments about the property. */
    private final List<String> comments;
    /** The bean generator. */
    private final BeanGen bean;

    /**
     * Constructor.
     * @param bean  the bean generator
     * @param content  the lines, not null
     * @param lineIndex  the index of a PropertyDefinition
     */
    public PropertyGen(BeanGen bean, List<String> content, int lineIndex) {
        this.bean = bean;
        this.annotationIndex = lineIndex;
        this.fieldIndex = parseFieldIndex(content);
        this.readWrite = parseReadWrite(content);
        this.deprecated = parseDeprecated(content);
        this.fieldName = parseName(content);
        this.propertyName = makePropertyName(bean, fieldName);
        this.upperName = makeUpperName(propertyName);
        this.type = parseType(content);
        List<String> comments = parseComment(content);
        this.firstComment = comments.get(0);
        this.comments = comments.subList(1, comments.size());
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
    private int parseFieldIndex(List<String> content) {
        for (int index = annotationIndex; index < content.size(); index++) {
            if (content.get(index).trim().startsWith("@") == false) {
                if (content.get(index).trim().length() == 0) {
                    throw new RuntimeException("Unable to locate field for property at line " + annotationIndex + ", found blank line");
                }
                return index;
            }
        }
        throw new RuntimeException("Unable to locate field for property at line " + annotationIndex);
    }

    private PropertyReadWrite parseReadWrite(List<String> content) {
        String line = content.get(annotationIndex).trim();
        Matcher matcher = Pattern.compile(".*readWrite[ ]*[=][ ]*PropertyReadWrite\\.([A-Z_]+).*").matcher(line);
        if (matcher.matches()) {
            return PropertyReadWrite.valueOf(matcher.group(1));
        }
        return PropertyReadWrite.READ_WRITE;
    }

    private boolean parseDeprecated(List<String> content) {
        for (int index = annotationIndex + 1; index < fieldIndex; index++) {
            String line = content.get(index).trim();
            if (line.equals("@Deprecated") || line.startsWith("@Deprecated ")) {
                return true;
            }
        }
        return false;
    }

    //-----------------------------------------------------------------------
    private String parseName(List<String> content) {
        String line = parseFieldDefinition(content);
        String[] parts = line.split(" ");
        String last = parts[parts.length - 1];
        if (last.endsWith(";") && last.length() > 1) {
            return last.substring(0, last.length() - 1);
        }
        throw new RuntimeException("Unable to locate field name at line " + annotationIndex);
    }

    private String parseType(List<String> content) {
        String line = parseFieldDefinition(content);
        String[] parts = line.split(" ");
        if (parts.length < 2) {
            throw new RuntimeException("Unable to locate field type at line " + annotationIndex);
        }
        int partsPos = parts.length - 2;
        String type = parts[partsPos];
        while (true) {
            int open, openPos, close, closePos = 0;
            open = openPos = close = closePos = 0;
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
                throw new RuntimeException("Unable to locate field type at line " + annotationIndex + ", mismatched generics");
            }
            partsPos--;
            type = parts[partsPos] + " " + type;
        }
        return type;
    }

    private String parseFieldDefinition(List<String> content) {
        String line = content.get(fieldIndex).trim();
        if (line.contains(" = ")) {
            line = line.substring(0, line.indexOf(" = ")).trim() + ";";
        }
        return line;
    }

    private List<String> parseComment(List<String> content) {
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
                    comments.add(commentLine);
                }
                String firstLine = comments.get(0);
                comments.set(0, firstLine.substring(0, 1).toLowerCase() + firstLine.substring(1));
            }
        } else if (commentEnd.startsWith("/**") && commentEnd.endsWith("*/")) {
            int startPos = commentEnd.indexOf("/**") + 3;
            int endPos = commentEnd.lastIndexOf("*/");
            String comment = commentEnd.substring(startPos, endPos).trim();
            comments.add(comment.substring(0, 1).toLowerCase() + comment.substring(1));
        }
        if (comments.size() == 0) {
            comments.add("the " + propertyName + ".");
        }
        return comments;
    }

    //-----------------------------------------------------------------------
    List<String> generateMetaPropertyConstant() {
        List<String> list = new ArrayList<String>();
        list.add("\t\t/**");
        list.add("\t\t * The meta-property for the {@code " + propertyName + "} property.");
        list.add("\t\t */");
        String propertyType = propertyType();
        if (propertyType.length() == 1) {
            propertyType = "Object";
        }
        if (isGenericPropertyType()) {
            list.add("\t\t@SuppressWarnings(\"unchecked\")");
            list.add("\t\tfinal MetaProperty " + propertyName +
                " = DirectMetaProperty.of" + readWrite() + "(this, \"" + propertyName + "\", " + actualType() + ");");
        } else {
            if (isGenericTypedProperty()) {
                list.add("\t\t@SuppressWarnings(\"unchecked\")");
            }
            list.add("\t\tfinal MetaProperty<" + propertyType + "> " + propertyName +
                " = DirectMetaProperty.of" + readWrite() + "(this, \"" + propertyName + "\", " + actualType() + ");");
        }
        return list;
    }

    List<String> generateMetaPropertyMapBuild() {
        List<String> list = new ArrayList<String>();
        list.add("\t\t\ttemp.put(\"" + propertyName + "\", " + propertyName + ");");
        return list;
    }

    List<String> generateGetter() {
        if (readWrite.isReadable() == false) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<String>();
        list.add("\t/**");
        list.add("\t * Gets " + firstComment);
        for (String comment : comments) {
            list.add("\t * " + comment);
        }
        list.add("\t * @return the value of the property");
        list.add("\t */");
        if (deprecated) {
            list.add("\t@Deprecated");
        }
        list.add("\tpublic " + type + " " + getterPrefix() + upperName + "() {");
        list.add("\t\treturn " + fieldName + ";");
        list.add("\t}");
        list.add("");
        return list;
    }

    List<String> generateSetter() {
        if (readWrite.isWritable() == false) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<String>();
        list.add("\t/**");
        list.add("\t * Sets " + firstComment);
        for (String comment : comments) {
            list.add("\t * " + comment);
        }
        list.add("\t * @param " + propertyName + "  the new value of the property");
        list.add("\t */");
        if (deprecated) {
            list.add("\t@Deprecated");
        }
        list.add("\tpublic void set" + upperName + "(" + type +  " " + propertyName + ") {");
        list.add("\t\tthis." + fieldName + " = " + propertyName + ";");
        list.add("\t}");
        list.add("");
        return list;
    }

    List<String> generateProperty() {
        List<String> list = new ArrayList<String>();
        list.add("\t/**");
        list.add("\t * Gets the the {@code " + propertyName + "} property.");
        list.add("\t * @return the property, not null");
        list.add("\t */");
        if (deprecated) {
            list.add("\t@Deprecated");
        }
        list.add("\tpublic final Property<" + propertyType() + "> " + propertyName + "() {");
        if (isGenericPropertyType()) {
            String beanGeneric = bean.getBeanType().substring(bean.getBeanNoGenericsType().length());
            list.add("\t\treturn meta()." + beanGeneric + propertyName + "().createProperty(this);");
        } else {
            list.add("\t\treturn meta()." + propertyName + "().createProperty(this);");
        }
        list.add("\t}");
        list.add("");
        return list;
    }

    List<String> generateMetaProperty() {
        List<String> list = new ArrayList<String>();
        String propertyType = propertyType();
        if (propertyType.length() == 1) {
            propertyType = "Object";
        }
        list.add("\t\t/**");
        list.add("\t\t * The meta-property for the {@code " + propertyName + "} property.");
        if (isGenericPropertyType()) {
            list.add("\t\t * @param <R>  the property type (which cannot be inferred in a static context)");
        }
        list.add("\t\t * @return the meta-property, not null");
        list.add("\t\t */");
        if (deprecated) {
            list.add("\t\t@Deprecated");
        }
        if (isGenericPropertyType()) {
            list.add("\t\t@SuppressWarnings(\"unchecked\")");
            list.add("\t\tpublic final <R> MetaProperty<R> " + propertyName + "() {");
            list.add("\t\t\treturn (MetaProperty<R>) " + propertyName + ";");
        } else {
            list.add("\t\tpublic final MetaProperty<" + propertyType + "> " + propertyName + "() {");
            list.add("\t\t\treturn " + propertyName + ";");
        }
        list.add("\t\t}");
        list.add("");
        return list;
    }

    List<String> generatePropertyGetCase() {
        List<String> list = new ArrayList<String>();
        list.add("\t\t\tcase " + propertyName.hashCode() + ":  // " + propertyName);
        if (readWrite.isReadable()) {
            list.add("\t\t\t\treturn " + getterPrefix() + upperName + "();");
        } else {
            list.add("\t\t\t\tthrow new UnsupportedOperationException(\"Property cannot be read: " + propertyName + "\");");
        }
        return list;
    }

    List<String> generatePropertySetCase() {
        List<String> list = new ArrayList<String>();
        list.add("\t\t\tcase " + propertyName.hashCode() + ":  // " + propertyName);
        if (readWrite.isWritable()) {
            list.add("\t\t\t\tset" + upperName + "(" + castObject() + "newValue);");
            list.add("\t\t\t\treturn;");
        } else {
            list.add("\t\t\t\tthrow new UnsupportedOperationException(\"Property cannot be written: " + propertyName + "\");");
        }
        return list;
    }

    //-----------------------------------------------------------------------
    private String readWrite() {
        switch (readWrite) {
            case READ_WRITE:
                return "ReadWrite";
            case READ_ONLY:
                return "ReadOnly";
            case WRITE_ONLY:
                return "WriteOnly";
            default:
                break;
        }
        throw new RuntimeException("Invalid read-write type");
    }

    private String actualType() {
        String pt = propertyType();
        if (pt.equals(type)) {
            int genericStart = pt.indexOf('<');
            if (genericStart >= 0) {
                return "(Class) " + pt.substring(0, genericStart) + ".class";
            }
            if (type.length() == 1) {
                return "Object.class";
            }
            return pt + ".class";
        }
        return pt + ".TYPE";
    }

    private String castObject() {
        String pt = propertyType();
        if (pt.equals(type)) {
            return "(" + pt + ") ";
        }
        return "(" + type + ") (" + pt + ") ";
    }

    private String propertyType() {
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

    private String getterPrefix() {
        return type.equals("boolean") ? "is" : "get";
    }

    private boolean isGenericTypedProperty() {
        return type.contains("<");
    }

    private boolean isGenericPropertyType() {
        return type.length() == 1;
    }

    boolean isGenericWritableProperty() {
        return readWrite.isWritable() && (type.contains("<") || type.length() == 1);
    }

}
