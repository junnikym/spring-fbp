package org.junnikym.springfbp;

import java.util.HashMap;
import java.util.Map;

public class TypeClassFactory {

    // key: type name, value: Class<?>
    static private Map<String, Class<?>> primitiveClasses = new HashMap<>() {{
        put( "int", int.class );
        put( "boolean", boolean.class );
        put( "char", char.class );
        put( "double", double.class );
        put( "byte", byte.class );
        put( "short", short.class );
        put( "long", long.class );
        put( "float", float.class );
        put( "void", void.class );
    }};

    static public Class<?> of(String type) throws ClassNotFoundException {
        final Class<?> clazz = primitiveClasses.get(type);
        if(clazz == null)
            return Class.forName(type);

        return clazz;
    }

    static public boolean isPrimitiveType(String type) {
        return primitiveClasses.keySet().contains(type);
    }

}
