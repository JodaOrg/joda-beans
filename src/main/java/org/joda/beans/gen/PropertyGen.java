/*
 *  Copyright 2001-2012 Stephen Colebourne
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
    private static final Pattern SETTER_PATTERN = Pattern.compile(".*[ ,(]set[ ]*[=][ ]*[\"]([a-zA-Z-]*)[\"].*");
    /** The validation pattern. */
    private static final Pattern VALIDATION_PATTERN = Pattern.compile(".*[ ,(]validate[ ]*[=][ ]*[\"]([a-zA-Z_.]*)[\"].*");

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
        this.annotationIndex = lineIndex;
        this.fieldIndex = parseCodeIndex(content);
        GeneratableProperty prop = new GeneratableProperty(bean.getData());
        if (derived) {
            prop.setGetStyle("manual");
            prop.setSetStyle("");
            prop.setDeprecated(parseDeprecated(content));
            prop.setPropertyName(parseMethodNameAsPropertyName(content));
            prop.setUpperName(makeUpperName(prop.getPropertyName()));
            prop.setType(parseMethodType(content));
        } else {
            prop.setGetStyle(parseGetStyle(content));
            prop.setSetStyle(parseSetStyle(content));
            prop.setValidation(parseValidation(content));
            prop.setDeprecated(parseDeprecated(content));
            prop.setFieldName(parseFieldName(content));
            prop.setPropertyName(makePropertyName(bean, prop.getFieldName()));
            prop.setUpperName(makeUpperName(prop.getPropertyName()));
            prop.setFinal(parseFinal(content));
            prop.setType(parseFieldType(content));
        }
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
    private int parseCodeIndex(List<String> content) {
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

    private String parseValidation(List<String> content) {
        String line = content.get(annotationIndex).trim();
        Matcher matcher = VALIDATION_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "";
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
    private String parseFieldName(List<String> content) {
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

    private String parseFieldType(List<String> content) {
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
    List<String> generateMetaPropertyConstant() {
        data.getBean().ensureImport(MetaProperty.class);
        data.getBean().ensureImport(DirectMetaProperty.class);
        List<String> list = new ArrayList<String>();
        list.add("\t\t/**");
        list.add("\t\t * The meta-property for the {@code " + data.getPropertyName() + "} property.");
        list.add("\t\t */");
        if (data.isBeanGenericType()) {
            list.add("\t\t@SuppressWarnings({\"unchecked\", \"rawtypes\" })");
            list.add("\t\tprivate final MetaProperty<" + data.getBean().getTypeGenericName(false) + "> " + metaFieldName() +
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
        return GetterGen.of(data).generateGetter(data);
    }

    List<String> generateSetter() {
        return SetterGen.of(data).generateSetter(data);
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
            list.add("\t\t\t\treturn " + GetterGen.of(data).generateGetInvoke(data) + ";");
        } else {
            list.add("\t\t\t\tif (quiet) {");
            list.add("\t\t\t\t\treturn null;");
            list.add("\t\t\t\t}");
            list.add("\t\t\t\tthrow new UnsupportedOperationException(\"Property cannot be read: " + data.getPropertyName() + "\");");
        }
        return list;
    }

    List<String> generatePropertySetCase() {
        List<String> list = new ArrayList<String>();
        list.add("\t\t\tcase " + data.getPropertyName().hashCode() + ":  // " + data.getPropertyName());
        String setter = SetterGen.of(data).generateSetInvoke(data);
        if (data.getReadWrite().isWritable() && setter != null) {
            list.add("\t\t\t\t" + setter + "(" + castObject() + "newValue);");
            list.add("\t\t\t\treturn;");
        } else {
            list.add("\t\t\t\tif (quiet) {");
            list.add("\t\t\t\t\treturn;");
            list.add("\t\t\t\t}");
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
