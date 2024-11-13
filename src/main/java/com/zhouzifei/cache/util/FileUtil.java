//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.zhouzifei.cache.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    public FileUtil() {
    }

    public static int deleteFiles(String rootPath, int fileNum) {
        File file = new File(rootPath);
        if (!file.exists()) {
            return -1;
        } else {
            if (file.isDirectory()) {
                File[] sonFiles = file.listFiles();
                if (sonFiles != null && sonFiles.length > 0) {
                    File[] var4 = sonFiles;
                    int var5 = sonFiles.length;

                    for(int var6 = 0; var6 < var5; ++var6) {
                        File sonFile = var4[var6];
                        if (sonFile.isDirectory()) {
                            fileNum = deleteFiles(sonFile.getAbsolutePath(), fileNum);
                        } else {
                            sonFile.delete();
                            ++fileNum;
                        }
                    }
                }
            } else {
                file.delete();
            }

            return fileNum;
        }
    }

    public static void writeByteArrayToFile(File file, byte[] data) throws IOException {
        OutputStream out = null;

        try {
            out = openOutputStream(file);
            out.write(data);
            out.close();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException var9) {
                    IOException var4 = var9;
                    log.info("Ignore failure in closing the Closeable", var4);
                }
            }

        }

    }

    private static OutputStream openOutputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }

            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && !parent.mkdirs() && !parent.isDirectory()) {
                throw new IOException("Directory '" + parent + "' could not be created");
            }
        }

        return new FileOutputStream(file);
    }

    public static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            byte[] b = new byte[4096];
            boolean var3 = false;

            int n;
            while((n = is.read(b)) != -1) {
                output.write(b, 0, n);
            }

            byte[] var5 = output.toByteArray();
            return var5;
        } finally {
            output.close();
        }
    }
}
