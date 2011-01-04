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
package org.joda.beans.gen;

import java.util.ArrayList;
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

    /** The getter pattern. */
    private static final Pattern GETTER_PATTERN = Pattern.compile(".*[ ,(]get[ ]*[=][ ]*[\"]([a-zA-Z-]*)[\"].*");
    /** The getter pattern. */
    private static final Pattern SETTER_PATTERN = Pattern.compile(".*[ ,(]set[ ]*[=][ ]*[\"]([a-zA-Z-]*)[\"].*");

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
     */
    public PropertyGen(BeanGen bean, List<String> content, int lineIndex) {
        this.bean = bean;
        this.annotationIndex = lineIndex;
        this.fieldIndex = parseFieldIndex(content);
        GeneratableProperty prop = new GeneratableProperty(bean.getData());
        prop.setGetStyle(parseGetStyle(content));
        prop.setSetStyle(parseSetStyle(content));
        prop.setReadWrite(makeReadWrite(prop.getGetStyle(), prop.getSetStyle(), prop.getPropertyName()));
        prop.setDeprecated(parseDeprecated(content));
        prop.setFieldName(parseName(content));
        prop.setPropertyName(makePropertyName(bean, prop.getFieldName()));
        prop.setUpperName(makeUpperName(prop.getPropertyName()));
        prop.setFinal(parseFinal(content));
        prop.setType(parseType(content));
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

    private PropertyReadWrite makeReadWrite(String getter, String setter, String propertyName) {
        if (getter.length() > 0 && setter.length() > 0) {
            return PropertyReadWrite.READ_WRITE;
        }
        if (getter.length() > 0) {
            return PropertyReadWrite.READ_ONLY;
        }
        if (setter.length() > 0) {
            return PropertyReadWrite.WRITE_ONLY;
        }
        throw new RuntimeException("Property must have a getter or setter: " + propertyName);
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

    private String parseGetStyle(List<String> content) {
        String line = content.get(annotationIndex).trim();
        Matcher matcher = GETTER_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "smart";
    }

    private String parseSetStyle(List<String> content) {
        String line = content.get(annotationIndex).trim();
        Matcher matcher = SETTER_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "smart";
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

    private boolean parseFinal(List<String> content) {
        String line = parseFieldDefinition(content);
        String[] parts = line.split(" ");
        if (parts.length < 2) {
            throw new RuntimeException("Unable to locate field type at line " + annotationIndex);
        }
        String first = parts[0];
        String second = parts[1];
        if (first.equals("final") || second.equals("final")) {
            return true;
        }
        return false;
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
        list.add("\t\t * The meta-property for the {@code " + data.getPropertyName() + "} property.");
        list.add("\t\t */");
        if (data.isBeanGenericType()) {
            list.add("\t\t@SuppressWarnings({\"unchecked\", \"rawtypes\" })");
            list.add("\t\tprivate final MetaProperty<" + data.getBean().getTypeGenericName(false) + "> " + metaFieldName() +
                " = (DirectMetaProperty) DirectMetaProperty.of" + readWrite() + "(this, \"" + data.getPropertyName() + "\", " + actualType() + ");");
        } else {
            String propertyType = propertyType();
            if (propertyType.length() == 1) {
                propertyType = "Object";
            }
            if (data.isGenericParamType()) {
                list.add("\t\t@SuppressWarnings({\"unchecked\", \"rawtypes\" })");
            }
            list.add("\t\tprivate final MetaProperty<" + propertyType + "> " + metaFieldName() +
                " = DirectMetaProperty.of" + readWrite() + "(this, \"" + data.getPropertyName() + "\", " + actualType() + ");");
        }
        return list;
    }

    List<String> generateMetaPropertyMapBuild() {
        List<String> list = new ArrayList<String>();
        list.add("\t\t\ttemp.put(\"" + data.getPropertyName() + "\", " + metaFieldName() + ");");
        return list;
    }

    List<String> generateGetter() {
        return GetterGen.of(data).generateGetter(data);
    }

    List<String> generateSetter() {
        return SetterGen.of(data).generateSetter(data);
    }

    List<String> generateProperty() {
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
        list.add("\tpublic final Property<" + propertyType() + "> " + data.getPropertyName() + "() {");
        list.add("\t\treturn metaBean()." + data.getPropertyName() + "().createProperty(this);");
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
        if (data.isBeanGenericType()) {
            propertyType = data.getBean().getTypeGenericName(false);
        }
        list.add("\t\t/**");
        list.add("\t\t * The meta-property for the {@code " + data.getPropertyName() + "} property.");
        list.add("\t\t * @return the meta-property, not null");
        list.add("\t\t */");
        if (data.isDeprecated()) {
            list.add("\t\t@Deprecated");
        }
        list.add("\t\tpublic final MetaProperty<" + propertyType + "> " + data.getPropertyName() + "() {");
        list.add("\t\t\treturn " + metaFieldName() + ";");
        list.add("\t\t}");
        list.add("");
        return list;
    }

    List<String> generatePropertyGetCase() {
        List<String> list = new ArrayList<String>();
        list.add("\t\t\tcase " + data.getPropertyName().hashCode() + ":  // " + data.getPropertyName());
        if (data.getReadWrite().isReadable()) {
            list.add("\t\t\t\treturn " + GetterGen.of(data).generateGetInvoke(data) + "();");
        } else {
            list.add("\t\t\t\tthrow new UnsupportedOperationException(\"Property cannot be read: " + data.getPropertyName() + "\");");
        }
        return list;
    }

    List<String> generatePropertySetCase() {
        List<String> list = new ArrayList<String>();
        list.add("\t\t\tcase " + data.getPropertyName().hashCode() + ":  // " + data.getPropertyName());
        if (data.getReadWrite().isWritable()) {
            list.add("\t\t\t\t" + SetterGen.of(data).generateSetInvoke(data) + "(" + castObject() + "newValue);");
            list.add("\t\t\t\treturn;");
        } else {
            list.add("\t\t\t\tthrow new UnsupportedOperationException(\"Property cannot be written: " + data.getPropertyName() + "\");");
        }
        return list;
    }

    //-----------------------------------------------------------------------
    private String readWrite() {
        switch (data.getReadWrite()) {
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
        if (pt.equals(data.getType())) {
            int genericStart = pt.indexOf('<');
            if (genericStart >= 0) {
                return "(Class) " + pt.substring(0, genericStart) + ".class";
            }
            if (data.getType().length() == 1) {
                return "Object.class";
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
//        return "(" + data.getType() + ") (" + pt + ") ";
    }

    private String propertyType() {
        if (data.getType().equals("boolean")) {
            return "Boolean";
        }
        if (data.getType().equals("byte")) {
            return "Byte";
        }
        if (data.getType().equals("short")) {
            return "Short";
        }
        if (data.getType().equals("char")) {
            return "Character";
        }
        if (data.getType().equals("int")) {
            return "Integer";
        }
        if (data.getType().equals("long")) {
            return "Long";
        }
        if (data.getType().equals("float")) {
            return "Float";
        }
        if (data.getType().equals("double")) {
            return "Double";
        }
        return data.getType();
    }

    private String metaFieldName() {
        return bean.getFieldPrefix() + data.getPropertyName();
    }

    GeneratableProperty getData() {
        return data;
    }

}
