package io.github.kurrycat.mpkmod.core.lwjgl;

import io.github.kurrycat.mpkmod.Tags;
import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.core.Transformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class GLStateCacheRedirector implements Transformer {
    private static final boolean LOG_REDIRECTS = Boolean.getBoolean("mpkmod.core.logGLStateCacheRedirects");
    private static final ILogger LOGGER = Transformer.LOGGER.createSubLogger("GLStateCacheRedirector");

    private static final String MOD_GROUP = Tags.MOD_GROUP.replace('.', '/');
    private static final String EXCLUDE = MOD_GROUP + "/lwjgl/glsc";
    private static final String PREFIX = "org/lwjgl/opengl/GL";
    private static final String GL11 = "org/lwjgl/opengl/GL11";
    private static final String GL15 = "org/lwjgl/opengl/GL15";
    private static final String GL20 = "org/lwjgl/opengl/GL20";
    private static final String GL30 = "org/lwjgl/opengl/GL30";
    private static final String CACHE_OWNER = MOD_GROUP + "/lwjgl/glsc/GLStateCache";

    @Override
    public boolean shouldTransform(String className) {
        return !className.startsWith(EXCLUDE);
    }

    @Override
    public boolean transform(ClassNode cn) {
        boolean modified = false;

        for (Object mnObj : cn.methods) {
            MethodNode mn = (MethodNode) mnObj;
            if (mn.instructions == null) continue;

            for (AbstractInsnNode insn : mn.instructions.toArray()) {
                if (!(insn instanceof MethodInsnNode m)) continue;
                if (m.getOpcode() != Opcodes.INVOKESTATIC) continue;

                if (m.owner.length() < PREFIX.length() + 2 || !m.owner.startsWith(PREFIX)) continue;

                final String key = m.name + m.desc;
                switch (m.owner) {
                    case GL11 -> {
                        switch (key) {
                            case "glBlendFunc(II)V",
                                 "glShadeModel(I)V",
                                 "glEnable(I)V",
                                 "glDisable(I)V",
                                 "glIsEnabled(I)Z",
                                 "glGetInteger(I)I" -> {
                                m.owner = CACHE_OWNER;
                                modified = true;
                            }
                        }
                    }
                    case GL15 -> {
                        switch (key) {
                            case "glBindBuffer(II)V" -> {
                                m.owner = CACHE_OWNER;
                                modified = true;
                            }
                        }
                    }
                    case GL20 -> {
                        switch (key) {
                            case "glUseProgram(I)V" -> {
                                m.owner = CACHE_OWNER;
                                modified = true;
                            }
                        }
                    }
                    case GL30 -> {
                        switch (key) {
                            case "glBindVertexArray(I)V" -> {
                                m.owner = CACHE_OWNER;
                                modified = true;
                            }
                        }
                    }
                }
            }
        }

        if (LOG_REDIRECTS && modified) {
            LOGGER.info("Redirected GL call in: " + cn.name);
        }
        return modified;
    }
}
