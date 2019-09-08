package com.fenquen.rdelay.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

public class BeanUtils {
    public static void assertNotNull(Object object) {
        if (null == object) {
            throw new NullPointerException();
        }
    }

    public static void assertNeitherNullNorEmpty(Object object) {
        if (null == object) {
            throw new NullPointerException();
        }

        Class clazz = object.getClass();

        int length = 0;
        if (clazz.isArray()) {
            length = Array.getLength(object);
        } else if (clazz.isAssignableFrom(Collection.class)) {
            length = ((Collection) object).size();
        } else if (clazz.isAssignableFrom(Map.class)) {
            length = ((Map) object).size();
        }

        if (length == 0) {
            throw new RuntimeException("length == 0");
        }
    }

    public void parseAbstractClass(String jsonStr) {

    }
}
