package org.joda.beans.gen;
import java.lang.reflect.Type;

public interface TypeExtractor {
    Type extract(Type type, Class<?> contextClass);
}
