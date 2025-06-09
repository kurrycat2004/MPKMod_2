package io.github.kurrycat.mpkmod.stonecutter.vintage_forge;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.api.resource.ResourceManager;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;

public final class ResourceManagerImpl implements ResourceManager {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<ResourceManager> {
        public Provider() {
            super(ResourceManagerImpl::new, ResourceManager.class);
        }
    }

    @Override
    public IResource resource(String domain, String path) {
        ResourceLocation loc = new ResourceLocation(domain, path);
        return new Resource(domain, path, loc);
    }

    @Override
    public InputStream inputStream(IResource resource) throws IOException {
        return Minecraft.getMinecraft().getResourceManager()
                .getResource((ResourceLocation) resource.backendResource()).getInputStream();
    }

    private record Resource(
            String domain,
            String path,
            ResourceLocation backendResource
    ) implements IResource {}
}
