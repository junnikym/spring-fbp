package org.junnikym.springfbp;

public class TypeClassFactory {

    static private Class<?> intClass = int.class;
    static private Class<?> booleanClass = boolean.class;
    static private Class<?> charClass = char.class;
    static private Class<?> doubleClass = double.class;
    static private Class<?> byteClass = byte.class;
    static private Class<?> shortClass = short.class;
    static private Class<?> longClass = long.class;
    static private Class<?> floatClass = float.class;

    static Class<?> of(String type) throws ClassNotFoundException {
        switch(type) {
            case "int": return intClass;
            case "boolean": return booleanClass;
            case "char": return charClass;
            case "double": return doubleClass;
            case "byte": return byteClass;
            case "short": return shortClass;
            case "long": return longClass;
            case "float": return floatClass;
            default: return Class.forName(type);
        }
    }

}
