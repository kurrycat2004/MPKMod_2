package io.github.kurrycat.mpkmod.lwjgl.lwjgl3;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.lwjgl.IGLCaps;
import io.github.kurrycat.mpkmod.api.lwjgl.LwjglBackend;
import io.github.kurrycat.mpkmod.service.DefaultServiceProvider;
import io.github.kurrycat.mpkmod.service.ServiceProvider;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.util.Optional;

public class Lwjgl3Backend implements LwjglBackend {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends DefaultServiceProvider<LwjglBackend> {
        public Provider() {
            super(Lwjgl3Backend::new, LwjglBackend.class);
        }

        @Override
        public Optional<String> invalidReason() {
            if (!isClassLoaded("org.lwjgl.opengl.GLCapabilities")) {
                return Optional.of("No LWJGL3 found");
            }
            return Optional.empty();
        }
    }

    private final IGLCaps caps;

    private record GLCaps(GLCapabilities caps) implements IGLCaps {
        @Override
        public boolean OpenGL11() {return caps.OpenGL11;}

        @Override
        public boolean OpenGL15() {return caps.OpenGL15;}

        @Override
        public boolean OpenGL33() {return caps.OpenGL33;}

        @Override
        public boolean GL_ARB_vertex_buffer_object() {return caps.GL_ARB_vertex_buffer_object;}
    }

    public Lwjgl3Backend() {
        this.caps = new GLCaps(GL.getCapabilities());
    }

    @Override
    public IGLCaps capabilities() {
        return caps;
    }
}
