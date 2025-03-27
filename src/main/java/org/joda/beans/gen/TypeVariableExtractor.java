package org.joda.beans.gen;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import org.joda.beans.JodaBeanUtils;

public class TypeVariableExtractor implements TypeExtractor {
    @Override
    public Type extract(Type type, Class<?> contextClass) {
        var tvar = (TypeVariable<?>) type;
        return JodaBeanUtils.resolveGenerics(tvar, contextClass);
    }
}
