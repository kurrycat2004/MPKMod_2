package io.github.kurrycat.mpkmod.api.lwjgl;

import java.nio.FloatBuffer;

public interface IGL20 {
    void glUniformMatrix4fv(int location, boolean transpose, FloatBuffer value);
}
