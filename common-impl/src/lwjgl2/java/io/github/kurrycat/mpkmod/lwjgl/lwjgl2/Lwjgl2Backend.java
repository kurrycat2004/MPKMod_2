package io.github.kurrycat.mpkmod.lwjgl.lwjgl2;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.lwjgl.IGLCaps;
import io.github.kurrycat.mpkmod.api.lwjgl.LwjglBackend;
import io.github.kurrycat.mpkmod.service.DefaultServiceProvider;
import io.github.kurrycat.mpkmod.service.ServiceProvider;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

import java.util.Optional;

public class Lwjgl2Backend implements LwjglBackend {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends DefaultServiceProvider<LwjglBackend> {
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

    public Lwjgl2Backend() {
        this.caps = new GLCaps(GLContext.getCapabilities());
    }

    @Override
    public IGLCaps capabilities() {
        return caps;
    }
}
