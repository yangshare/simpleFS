//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.zhouzifei.cache;

import com.zhouzifei.cache.common.SerializationUtils;
import com.zhouzifei.cache.util.FileManager;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class FileCacheEngine implements CacheEngine {
    private static final FileManager FILE_MANAGER = new FileManager();

    public FileCacheEngine() {
    }

    public void add(String cacheName, Serializable key, Object value) {
        if (cacheName != null && key != null && value != null) {
            try {
                byte[] data = SerializationUtils.serialize(value);
                FILE_MANAGER.add(cacheName, key, data);
            } catch (IOException var5) {
                IOException e = var5;
                e.printStackTrace();
            }
        }

    }

    public void add(Serializable key, Object value) {
        this.add("default", key, value);
    }

    public Object get(String folderName, Serializable key) {
        if (folderName != null && key != null) {
            try {
                byte[] data = FILE_MANAGER.get(folderName, key);
                return SerializationUtils.deserialize(data);
            } catch (IOException var4) {
                IOException e = var4;
                e.printStackTrace();
            }
        }

        return null;
    }

    public Object get(Serializable key) {
        return this.get("default", key);
    }

    public <T> T get(String folderName, Serializable key, Class<?> s) {
        Object o = this.get(folderName, key);
        if (null == o) {
            return null;
        } else {
            return s.isInstance(o) ? (T) s.cast(o) : null;
        }
    }

    public <T> T get(Serializable key, Class<?> s) {
        Object o = this.get(key);
        if (null == o) {
            return null;
        } else {
            return s.isInstance(o) ? (T) s.cast(o) : null;
        }
    }

    public void remove(String folderName, Serializable key) {
        if (folderName != null && key != null) {
            FILE_MANAGER.remove(folderName, key);
        }

    }

    public void remove(Serializable key) {
        this.remove("default", key);
    }

    public void clear(String folderName) {
        if (folderName != null) {
            try {
                FILE_MANAGER.clear(folderName);
            } catch (IOException var3) {
                IOException e = var3;
                e.printStackTrace();
            }
        }

    }

    public void clear() {
        this.clear("default");
    }

    public void stop() {
        try {
            FILE_MANAGER.close();
        } catch (IOException var2) {
            IOException e = var2;
            e.printStackTrace();
        }

    }

    public File[] getList() {
        return this.getList("default");
    }

    public File[] getAll() {
        return this.getList();
    }

    public File[] getList(String cacheName) {
        return FILE_MANAGER.getList(cacheName);
    }

    static {
        FILE_MANAGER.init();
    }
}
