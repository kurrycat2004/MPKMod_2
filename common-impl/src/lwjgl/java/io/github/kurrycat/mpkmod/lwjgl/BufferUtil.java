package io.github.kurrycat.mpkmod.lwjgl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public final class BufferUtil {
    private BufferUtil() {}

    public static ByteBuffer allocDirectByte(int size) {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }

    public static FloatBuffer allocDirectFloat(int floatCount) {
        return allocDirectByte(floatCount * Float.BYTES).asFloatBuffer();
    }

    public static FloatBuffer reallocFloat(FloatBuffer oldBuf, int floatCount) {
        FloatBuffer newBuf = allocDirectByte(floatCount * Float.BYTES).asFloatBuffer();
        if (oldBuf != null) {
            oldBuf.flip();
            newBuf.put(oldBuf);
        }
        return newBuf;
    }

    public static IntBuffer reallocInt(IntBuffer oldBuf, int intCount) {
        IntBuffer newBuf = allocDirectByte(intCount * Integer.BYTES).asIntBuffer();
        if (oldBuf != null) {
            oldBuf.flip();
            newBuf.put(oldBuf);
        }
        return newBuf;
    }

    public static ByteBuffer reallocByte(ByteBuffer oldBuf, int byteCount) {
        ByteBuffer newBuf = allocDirectByte(byteCount);
        if (oldBuf != null) {
            oldBuf.flip();
            newBuf.put(oldBuf);
        }
        return newBuf;
    }
}
