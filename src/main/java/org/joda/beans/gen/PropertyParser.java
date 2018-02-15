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
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A property parsed from the source file.
 * 
 * @author Stephen Colebourne
 */
class PropertyParser {

    /** The getter pattern. */
    private static final Pattern ALIAS_PATTERN = Pattern.compile(".*[ ,(]alias[ ]*[=][ ]*[\"]([a-zA-z_][a-zA-z0-9_]*)[\"].*");
    /** The getter pattern. */
    private static final Pattern GETTER_PATTERN = Pattern.compile(".*[ ,(]get[ ]*[=][ ]*[\"]([a-zA-Z-]*)[\"].*");
    /** The setter pattern. */
    private static final Pattern SETTER_PATTERN = Pattern.compile(".*[ ,(]set[ ]*[=][ ]*[\"]([ !#-~]*)[\"].*");
    /** The override pattern. */
    private static final Pattern OVERRIDE_GET_PATTERN = Pattern.compile(".*[ ,(]overrideGet[ ]*[=][ ]*(true|false).*");
    /** The override pattern. */
    private static final Pattern OVERRIDE_SET_PATTERN = Pattern.compile(".*[ ,(]overrideSet[ ]*[=][ ]*(true|false).*");
    /** The type pattern. */
    private static final Pattern TYPE_PATTERN = Pattern.compile(".*[ ,(]type[ ]*[=][ ]*[\"]([a-zA-Z0-9 ,_<>?.]*)[\"].*");
    /** The type builder pattern. */
    private static final Pattern BUILDER_TYPE_PATTERN = Pattern.compile(".*[ ,(]builderType[ ]*[=][ ]*[\"]([a-zA-Z0-9 ,_<>?.]*)[\"].*");
    /** The equalsHashCode pattern. */
    private static final Pattern EQ_HASH_PATTERN = Pattern.compile(".*[ ,(]equalsHashCodeStyle[ ]*[=][ ]*[\"]([a-zA-Z]*)[\"].*");
    /** The toString pattern. */
    private static final Pattern TO_STR_PATTERN = Pattern.compile(".*[ ,(]toStringStyle[ ]*[=][ ]*[\"]([a-zA-Z]*)[\"].*");
    /** The validation pattern. */
    private static final Pattern VALIDATION_PATTERN = Pattern.compile(".*[ ,(]validate[ ]*[=][ ]*[\"]([a-zA-Z_.]*)[\"].*");

    /** The bean generator. */
    private final BeanParser beanParser;
    /** Annotation line index for {@code PropertyDefinition} in input file. */
    private int propertyIndex;
    /** Annotation line index in input file. */
    private int annotationIndex;
    /** Field line index in input file. */
    private int fieldIndex;

    /**
     * Constructor.
     * @param beanParser  the bean parser
     */
    PropertyParser(BeanParser beanParser) {
        this.beanParser = beanParser;
    }

    //-----------------------------------------------------------------------
    PropertyGen parse(BeanData beanData, List<String> content, int lineIndex) {
        propertyIndex = lineIndex;
        annotationIndex = parseAnnotationStart(content, lineIndex);
        fieldIndex = parseCodeIndex(content);
        PropertyData data = new PropertyData(beanData, beanParser.getConfig(), lineIndex);
        data.setAlias(parseAlias(content));
        data.setGetStyle(parseGetStyle(content));
        data.setSetStyle(parseSetStyle(content));
        data.setOverrideGet(parseOverrideGet(content));
        data.setOverrideSet(parseOverrideSet(content));
        data.setTypeStyle(parseTypeStyle(content));
        data.setBuilderTypeStyle(parseBuilderTypeStyle(content));
        data.setEqualsHashCodeStyle(parseEqualsHashCodeStyle(content));
        data.setToStringStyle(parseToStringStyle(content));
        data.setValidation(parseValidation(content));
        data.setDeprecated(parseDeprecated(content));
        data.setFieldName(parseFieldName(content));
        data.setPropertyName(makePropertyName(data.getFieldName()));
        data.setUpperName(makeUpperName(data.getPropertyName()));
        data.setFinal(parseFinal(content));
        data.setFieldType(parseFieldType(content));
        data.setInitializer(parseFieldInitializer(content));
        data.resolveType();
        data.resolveBuilderType();
        data.resolveValidation();
        data.resolveGetterGen(beanParser.getFile(), lineIndex);
        data.resolveSetterGen(beanParser.getFile(), lineIndex);
        data.resolveCopyGen(beanParser.getFile(), lineIndex);
        data.resolveBuilderGen();
        data.resolveEqualsHashCodeStyle(beanParser.getFile(), lineIndex);
        data.resolveToStringStyle(beanParser.getFile(), lineIndex);
        data.setMetaFieldName(beanParser.getFieldPrefix() + data.getPropertyName());
        List<String> comments = parseComment(content, data.getPropertyName());
        data.setFirstComment(comments.get(0));
        data.getComments().addAll(comments.subList(1, comments.size()));
        if (beanData.isBeanStyleLightOrMinimal() && beanData.isMutable() && data.getSetterGen() instanceof SetterGen.NoSetterGen) {
            throw new IllegalArgumentException("Light and Minimal style beans do not support final fields when mutable");
        }
        return new PropertyGen(data);
    }

    PropertyGen parseDerived(BeanData beanData, List<String> content, int lineIndex) {
        propertyIndex = lineIndex;
        annotationIndex = parseAnnotationStart(content, lineIndex);
        fieldIndex = parseCodeIndex(content);
        PropertyData data = new PropertyData(beanData, beanParser.getConfig(), lineIndex);
        data.setGetStyle("manual");
        data.setSetStyle("");
        data.setTypeStyle("");
        data.setBuilderTypeStyle("");
        data.setDeprecated(parseDeprecated(content));
        data.setPropertyName(parseMethodNameAsPropertyName(content));
        data.setUpperName(makeUpperName(data.getPropertyName()));
        data.setFieldType(parseMethodType(content));
        data.setInitializer(parseFieldInitializer(content));
        data.resolveType();
        data.resolveBuilderType();
        data.resolveValidation();
        data.resolveGetterGen(beanParser.getFile(), lineIndex);
        data.resolveSetterGen(beanParser.getFile(), lineIndex);
        data.resolveCopyGen(beanParser.getFile(), lineIndex);
        data.resolveBuilderGen();
        data.setMetaFieldName(beanParser.getFieldPrefix() + data.getPropertyName());
        List<String> comments = parseComment(content, data.getPropertyName());
        data.setFirstComment(comments.get(0));
        data.getComments().addAll(comments.subList(1, comments.size()));
        return new PropertyGen(data);
    }

    //-----------------------------------------------------------------------
    private String makePropertyName(String name) {
        if (name.startsWith(beanParser.getFieldPrefix())) {
            return name.substring(beanParser.getFieldPrefix().length());
        }
        return name;
    }

    private String makeUpperName(String name) {
        return name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
    }

    //-----------------------------------------------------------------------
    private int parseAnnotationStart(List<String> content, int lineIndex) {
        while (lineIndex > 0 && content.get(lineIndex - 1).trim().startsWith("@")) {
            return lineIndex - 1;
        }
        return lineIndex;
    }

    private int parseCodeIndex(List<String> content) {
        for (int index = propertyIndex; index < content.size(); index++) {
            String line = content.get(index).trim();
            if (line.startsWith("@") == false) {
                if (line.length() == 0 ||
                        line.startsWith("//") ||
                        (index > propertyIndex && content.get(index - 1).endsWith(","))) {
                    continue;
                }
                return index;
            }
        }
        throw new BeanCodeGenException(
            "Unable to locate field for property at line " + (propertyIndex + 1),
            beanParser.getFile(), propertyIndex + 1);
    }

    private String parseAlias(List<String> content) {
        String line = content.get(propertyIndex).trim();
        Matcher matcher = ALIAS_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "";
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

    private boolean parseOverrideGet(List<String> content) {
        String line = content.get(propertyIndex).trim();
        Matcher matcher = OVERRIDE_GET_PATTERN.matcher(line);
        if (matcher.matches()) {
            return "true".equals(matcher.group(1));
        }
        return false;
    }

    private boolean parseOverrideSet(List<String> content) {
        String line = content.get(propertyIndex).trim();
        Matcher matcher = OVERRIDE_SET_PATTERN.matcher(line);
        if (matcher.matches()) {
            return "true".equals(matcher.group(1));
        }
        return false;
    }

    private String parseTypeStyle(List<String> content) {
        String line = content.get(propertyIndex).trim();
        Matcher matcher = TYPE_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "smart";
    }

    private String parseBuilderTypeStyle(List<String> content) {
        String line = content.get(propertyIndex).trim();
        Matcher matcher = BUILDER_TYPE_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "smart";
    }

    private String parseEqualsHashCodeStyle(List<String> content) {
        String line = content.get(propertyIndex).trim();
        Matcher matcher = EQ_HASH_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "smart";
    }
    
    private String parseToStringStyle(List<String> content) {
        String line = content.get(propertyIndex).trim();
        Matcher matcher = TO_STR_PATTERN.matcher(line);
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
        throw new BeanCodeGenException(
            "Unable to locate field name at line " + (propertyIndex + 1), beanParser.getFile(), propertyIndex + 1);
    }

    private boolean parseFinal(List<String> content) {
        String line = parseFieldDefinition(content);
        String[] parts = line.split(" ");
        if (parts.length < 2) {
            throw new BeanCodeGenException(
                "Unable to locate field type at line " + (propertyIndex + 1), beanParser.getFile(), propertyIndex + 1);
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
            throw new BeanCodeGenException(
                "Unable to locate field type at line " + (propertyIndex + 1), beanParser.getFile(), propertyIndex + 1);
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
                throw new BeanCodeGenException(
                    "Unable to locate field type at line " + (propertyIndex + 1) + ", mismatched generics",
                    beanParser.getFile(), propertyIndex + 1);
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
        if (line.contains("=")) {
            line = line.substring(0, line.indexOf("=")).trim() + ";";
        }
        return line.replace("  ", " ");
    }

    private String parseFieldInitializer(List<String> content) {
        String line = content.get(fieldIndex).trim();
        if (line.contains("//")) {
            line = line.substring(0, line.indexOf("//")).trim();
        }
        if (line.contains(" = ")) {
            line = line.substring(line.indexOf(" = ") + 3).trim();
            if (line.endsWith(";") == false) {
                throw new BeanCodeGenException("Field line does not end with semi-colon", beanParser.getFile(), fieldIndex);
            }
            return line.substring(0, line.length() - 1).trim();
        }
        return "";
    }

    //-----------------------------------------------------------------------
    private String parseMethodNameAsPropertyName(List<String> content) {
        String name = parseMethodDefinition(content)[1];
        if (name.length() == 0 || Character.isUpperCase(name.charAt(0)) == false) {
            throw new BeanCodeGenException("@DerivedProperty method name invalid: '" + name + "'", beanParser.getFile(), fieldIndex);
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
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
            throw new BeanCodeGenException("@DerivedProperty method cannot be static", beanParser.getFile(), fieldIndex);
        }
        int getIndex = line.indexOf(" get");
        if (getIndex < 0) {
            throw new BeanCodeGenException("@DerivedProperty method must start with 'get'", beanParser.getFile(), fieldIndex);
        }
        if (line.endsWith(lineEnd) == false) {
            throw new BeanCodeGenException("@DerivedProperty method must end with '" + lineEnd + "'", beanParser.getFile(), fieldIndex);
        }
        line = line.substring(0, line.length() - lineEnd.length());
        String[] split = new String[2];
        split[0] = line.substring(0, getIndex).trim();
        split[1] = line.substring(getIndex + 4).trim();
        return split;
    }

    //-----------------------------------------------------------------------
    private List<String> parseComment(List<String> content, String propertyName) {
        List<String> comments = new ArrayList<>();
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
                throw new BeanCodeGenException("Unable to locate comment start at line " + annotationIndex, beanParser.getFile(), annotationIndex);
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
                    comments.set(0, firstLine.substring(0, 1).toLowerCase(Locale.ENGLISH) + firstLine.substring(1));
                } else {
                    comments.remove(0);
                }
            }
        } else if (commentEnd.startsWith("/**") && commentEnd.endsWith("*/")) {
            int startPos = commentEnd.indexOf("/**") + 3;
            int endPos = commentEnd.lastIndexOf("*/");
            String comment = commentEnd.substring(startPos, endPos).trim();
            if (comment.length() > 0) {
                comments.add(comment.substring(0, 1).toLowerCase(Locale.ENGLISH) + comment.substring(1));
            }
        }
        if (comments.size() == 0) {
            comments.add("the " + propertyName + ".");
        }
        return comments;
    }

}
