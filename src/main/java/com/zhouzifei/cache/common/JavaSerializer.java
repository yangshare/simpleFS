package com.zhouzifei.cache.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JavaSerializer implements Serializer {
    public JavaSerializer() {
    }

    public String name() {
        return "java";
    }

    public byte[] serialize(Object obj) throws IOException {
        ObjectOutputStream oos = null;

        byte[] var4;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            var4 = baos.toByteArray();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException var11) {
                }
            }

        }

        return var4;
    }

    public Object deserialize(byte[] bits) throws IOException {
        if (bits != null && bits.length != 0) {
            ObjectInputStream ois = null;

            Object var4;
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(bits);
                ois = new ObjectInputStream(bais);
                var4 = ois.readObject();
                return var4;
            } catch (ClassNotFoundException var14) {
                var4 = null;
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException var13) {
                    }
                }

            }

            return var4;
        } else {
            return null;
        }
    }
}
