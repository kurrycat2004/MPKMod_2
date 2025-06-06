package io.github.kurrycat.mpkmod.lwjgl.lwjgl2;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.lwjgl.IGL20;
import io.github.kurrycat.mpkmod.api.lwjgl.IGLCaps;
import io.github.kurrycat.mpkmod.api.lwjgl.LwjglBackend;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;

import java.nio.FloatBuffer;
import java.util.Optional;

public final class Lwjgl2Backend implements LwjglBackend {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<LwjglBackend> {
        public Provider() {
            super(Lwjgl2Backend::new, LwjglBackend.class);
        }

        @Override
        public Optional<String> invalidReason() {
            if (!isClassLoaded("org.lwjgl.opengl.ContextCapabilities")) {
                return Optional.of("No LWJGL2 found");
            }
            return Optional.empty();
        }
    }

    private final IGLCaps caps;
    private final IGL20 gl20;

    public Lwjgl2Backend() {
        this.caps = new GLCaps(GLContext.getCapabilities());
        this.gl20 = new GL20Impl();
    }

    private record GLCaps(ContextCapabilities caps) implements IGLCaps {
        @Override
        public boolean OpenGL11() {return caps.OpenGL11;}

        @Override
        public boolean OpenGL15() {return caps.OpenGL15;}

        @Override
        public boolean OpenGL33() {return caps.OpenGL33;}

        @Override
        public boolean GL_ARB_vertex_buffer_object() {return caps.GL_ARB_vertex_buffer_object;}
    }

    private static final class GL20Impl implements IGL20 {
        @Override
        public void glUniformMatrix4fv(int location, boolean transpose, FloatBuffer value) {
            GL20.glUniformMatrix4(location, transpose, value);
        }
    }

    @Override
    public IGLCaps capabilities() {
        return caps;
    }

    @Override
    public IGL20 gl20() {
        return gl20;
    }
}
