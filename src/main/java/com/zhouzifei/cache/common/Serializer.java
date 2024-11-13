//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.zhouzifei.cache.common;

import java.io.IOException;

public interface Serializer {
    String name();

    byte[] serialize(Object var1) throws IOException;

    Object deserialize(byte[] var1) throws IOException;
}
