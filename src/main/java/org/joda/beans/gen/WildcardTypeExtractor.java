package org.joda.beans.gen;

import java.lang.reflect.Type;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

public class WildcardTypeExtractor implements TypeExtractor {
    @Override
    public Type extract(Type type, Class<?> contextClass) {
        var wtype = (WildcardType) type;
        if (wtype.getLowerBounds().length == 0 && wtype.getUpperBounds().length > 0) {
            return wtype.getUpperBounds()[0];
        }
        return type;
    }
}
