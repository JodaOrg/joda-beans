package org.joda.beans.gen;

import java.util.ArrayList;
import java.util.List;

public class GenericMetaLinesBuilder {

    private final BeanData data;

    public GenericMetaLinesBuilder(BeanData data) {
        this.data = data;
    }

    public List<String> buildLines() {
        List<String> lines = new ArrayList<>();
        lines.add("/**");
        lines.add(" * The meta-bean for {@code " + data.getTypeRaw() + "}.");

        int count = data.getTypeGenericCount();

        if (count == 1) {
            lines.add(" * @param <R>  the bean's generic type");
            lines.add(" * @param cls  the bean's generic type");
        } else if (count == 2) {
            lines.add(" * @param <R>  the first generic type");
            lines.add(" * @param <S>  the second generic type");
            lines.add(" * @param cls1  the first generic type");
            lines.add(" * @param cls2  the second generic type");
        } else if (count == 3) {
            lines.add(" * @param <R>  the first generic type");
            lines.add(" * @param <S>  the second generic type");
            lines.add(" * @param <T>  the third generic type");
            lines.add(" * @param cls1  the first generic type");
            lines.add(" * @param cls2  the second generic type");
            lines.add(" * @param cls3  the third generic type");
        }

        lines.add(" * @return the meta-bean, not null");
        lines.add(" */");
        lines.add("@SuppressWarnings(\"unchecked\")");

        String[] typeNames = {"R", "S", "T"};
        if (count == 1) {
            lines.add("public static <R" + data.getTypeGenericExtends(0, typeNames) + "> " +
                    data.getTypeRaw() + ".Meta<R> meta" + data.getTypeRaw() + "(Class<R> cls) {");
        } else if (count == 2) {
            lines.add("public static <R" + data.getTypeGenericExtends(0, typeNames) + ", S" +
                    data.getTypeGenericExtends(1, typeNames) + "> " + data.getTypeRaw() +
                    ".Meta<R, S> meta" + data.getTypeRaw() + "(Class<R> cls1, Class<S> cls2) {");
        } else if (count == 3) {
            lines.add("public static <R" + data.getTypeGenericExtends(0, typeNames) + ", S" +
                    data.getTypeGenericExtends(1, typeNames) + ", T" +
                    data.getTypeGenericExtends(2, typeNames) + "> " + data.getTypeRaw() +
                    ".Meta<R, S, T> meta" + data.getTypeRaw() +
                    "(Class<R> cls1, Class<S> cls2, Class<T> cls3) {");
        }

        lines.add("    return " + data.getTypeRaw() + ".Meta.INSTANCE;");
        lines.add("}");

        return lines;
    }
}
