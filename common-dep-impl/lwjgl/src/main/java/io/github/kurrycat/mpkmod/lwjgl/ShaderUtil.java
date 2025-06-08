package io.github.kurrycat.mpkmod.lwjgl;

import io.github.kurrycat.mpkmod.api.resource.IResource;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.IOException;

public final class ShaderUtil {
    private ShaderUtil() {}

    public static int createProgram(IResource vertexShader, IResource fragmentShader) throws IOException {
        String vertexSource = vertexShader.readUtf8();
        String fragmentSource = fragmentShader.readUtf8();

        int vs = compile(GL20.GL_VERTEX_SHADER, vertexSource);
        int fs = compile(GL20.GL_FRAGMENT_SHADER, fragmentSource);

        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vs);
        GL20.glAttachShader(program, fs);
        GL20.glLinkProgram(program);

        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            int logLength = GL20.glGetProgrami(program, GL20.GL_INFO_LOG_LENGTH);
            String log = GL20.glGetProgramInfoLog(program, logLength);
            throw new RuntimeException("Program link error:\n" + log);
        }

        GL20.glDetachShader(program, vs);
        GL20.glDetachShader(program, fs);
        GL20.glDeleteShader(vs);
        GL20.glDeleteShader(fs);

        return program;
    }

    private static int compile(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            int logLength = GL20.glGetShaderi(shader, GL20.GL_INFO_LOG_LENGTH);
            String log = GL20.glGetShaderInfoLog(shader, logLength);
            throw new RuntimeException("Shader compile error (" + shaderTypeName(type) + "):\n" + log);
        }
        return shader;
    }

    private static String shaderTypeName(int type) {
        return switch (type) {
            case GL20.GL_VERTEX_SHADER -> "vertex";
            case GL20.GL_FRAGMENT_SHADER -> "fragment";
            default -> "unknown(" + type + ")";
        };
    }
}
