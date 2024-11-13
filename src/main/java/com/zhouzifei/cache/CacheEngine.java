//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.zhouzifei.cache;

import java.io.Serializable;

public interface CacheEngine {
    String FOLDER_NAME = "default";

    void add(String var1, Serializable var2, Object var3);

    void add(Serializable var1, Object var2);

    Object get(String var1, Serializable var2);

    Object get(Serializable var1);

    <T> T get(String var1, Serializable var2, Class<?> var3);

    <T> T get(Serializable var1, Class<?> var2);

    void remove(String var1, Serializable var2);

    void remove(Serializable var1);

    void clear(String var1);

    void clear();

    void stop();
}
