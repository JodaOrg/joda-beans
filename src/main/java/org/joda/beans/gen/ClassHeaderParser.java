package org.joda.beans.gen;

import java.util.List;

public class ClassHeaderParser {

    private final List<String> content;

    public ClassHeaderParser(List<String> content) {
        this.content = content;
    }

    public String extractAfterType(int defLine, String fullType) {
        var buf = new StringBuilder(128);
        var matchedType = false;
        for (var index = defLine; index < content.size(); index++) {
            var line = content.get(index);
            if (!matchedType) {
                if (!line.contains(fullType)) {
                    continue;
                }
                matchedType = true;
                line = line.substring(line.indexOf(fullType) + fullType.length());
            }
            buf.append(line).append(' ');
            if (line.trim().endsWith("{") && !line.trim().startsWith("@")) {
                break;
            }
        }
        return buf.toString().trim();
    }
}
