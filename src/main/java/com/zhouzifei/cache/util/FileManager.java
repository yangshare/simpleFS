//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.zhouzifei.cache.util;

import com.zhouzifei.cache.FileCacheEngine;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

public class FileManager {
    private static final String CacheName = "default";
    private static final String Cache = System.getProperty("user.home") + "/fileCache";
    private static final File DIRECTORY;

    public FileManager() {
    }

    public void init() {
        if (!DIRECTORY.isDirectory()) {
            DIRECTORY.mkdir();
        }

    }

    public void close() throws IOException {
        if (DIRECTORY.isDirectory()) {
            FileUtil.deleteFiles(Cache, 1);
        }

    }

    public void add(String cacheName, Serializable key, byte[] data) throws IOException {
        File file = new File(DIRECTORY, cacheName + File.separator + key.toString());
        FileUtil.writeByteArrayToFile(file, data);
    }

    public void add(Serializable key, byte[] data) throws IOException {
        this.add("default", key, data);
    }

    public byte[] get(String cacheName, Serializable key) throws IOException {
        File file = new File(DIRECTORY, cacheName + File.separator + key.toString());
        return !file.isFile() ? null : FileUtil.toByteArray(new FileInputStream(file));
    }

    public byte[] get(Serializable key) throws IOException {
        return this.get("default", key);
    }

    public void remove(String cacheName, Serializable key) {
        File file = new File(DIRECTORY, cacheName + File.separator + key.toString());
        if (file.isFile()) {
            boolean var4 = file.delete();
        }

    }

    public void remove(Serializable key) {
        this.remove("default", key);
    }

    public void clear(String cacheName) throws IOException {
        File file = new File(DIRECTORY, cacheName);
        if (file.isDirectory()) {
            FileUtil.deleteFiles(Cache, 1);
        }

    }

    public void clear() throws IOException {
        this.clear("default");
    }

    public File[] getList(String cacheName) {
        File file = new File(DIRECTORY, cacheName);
        return file.listFiles();
    }

    public File[] getList() {
        return DIRECTORY.listFiles();
    }

    public static void main(String[] args) {
        FileCacheEngine fileCacheEngine = new FileCacheEngine();
        fileCacheEngine.add("12", "213");
        System.out.println(fileCacheEngine.get("12"));
        fileCacheEngine.remove("12");
        System.out.println(fileCacheEngine.get("12"));
        System.out.println(Arrays.toString(fileCacheEngine.getList()));
    }

    static {
        DIRECTORY = new File(Cache);
    }
}
