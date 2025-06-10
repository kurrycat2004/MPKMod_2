package io.github.kurrycat.mpkmod.lwjgl.glsc;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.BitSet;

public final class GLStateCache {
    private static int blendSrc, blendDst;
    private static int shadeModel;
    private static int arrayBufferBinding, elementArrayBufferBinding;
    private static int currentProgram;
    private static int vertexArrayBinding;
    private static final BitSet enabledCaps = new BitSet(4096);

    private GLStateCache() {}

    public static void glBlendFunc(int src, int dst) {
        blendSrc = src;
        blendDst = dst;
        GL11.glBlendFunc(src, dst);
    }

    public static void glShadeModel(int mode) {
        shadeModel = mode;
        GL11.glShadeModel(mode);
    }

    public static void glEnable(int cap) {
        enabledCaps.set(cap);
        GL11.glEnable(cap);
    }

    public static void glDisable(int cap) {
        enabledCaps.clear(cap);
        GL11.glDisable(cap);
    }

    public static boolean glIsEnabled(int cap) {
        return enabledCaps.get(cap);
    }

    public static void glBindBuffer(int target, int buffer) {
        switch (target) {
            case GL15.GL_ARRAY_BUFFER -> arrayBufferBinding = buffer;
            case GL15.GL_ELEMENT_ARRAY_BUFFER -> elementArrayBufferBinding = buffer;
        }
        GL15.glBindBuffer(target, buffer);
    }

    public static void glUseProgram(int program) {
        currentProgram = program;
        GL20.glUseProgram(program);
    }

    public static void glBindVertexArray(int array) {
        vertexArrayBinding = array;
        GL30.glBindVertexArray(array);
    }

    public static int glGetInteger(int pname) {
        return switch (pname) {
            case GL11.GL_BLEND_SRC -> blendSrc;
            case GL11.GL_BLEND_DST -> blendDst;
            case GL11.GL_SHADE_MODEL -> shadeModel;
            case GL15.GL_ARRAY_BUFFER_BINDING -> arrayBufferBinding;
            case GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING -> elementArrayBufferBinding;
            case GL20.GL_CURRENT_PROGRAM -> currentProgram;
            case GL30.GL_VERTEX_ARRAY_BINDING -> vertexArrayBinding;
            default -> GL11.glGetInteger(pname);
        };
    }
}
