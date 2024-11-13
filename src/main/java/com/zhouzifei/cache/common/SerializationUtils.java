//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.zhouzifei.cache.common;

import java.io.IOException;

public class SerializationUtils {
    private static Serializer g_ser = new JavaSerializer();

    public SerializationUtils() {
    }

    public static byte[] serialize(Object obj) throws IOException {
        return g_ser.serialize(obj);
    }

    public static Object deserialize(byte[] bytes) throws IOException {
        return g_ser.deserialize(bytes);
    }
}
