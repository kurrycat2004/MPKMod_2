package io.github.kurrycat.mpkmod.stonecutter.vintage_forge;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.api.resource.ResourceManager;
import io.github.kurrycat.mpkmod.api.service.DefaultServiceProvider;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import net.minecraft.util.ResourceLocation;

public final class ResourceManagerImpl implements ResourceManager {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends DefaultServiceProvider<ResourceManager> {
        public Provider() {
            super(ResourceManagerImpl::new, ResourceManager.class);
        }
    }

    @Override
    public IResource resource(String domain, String path) {
        ResourceLocation loc = new ResourceLocation(domain, path);
        return new Resource(domain, path, loc);
    }

    private record Resource(
            String domain,
            String path,
            ResourceLocation backendResource
    ) implements IResource {}
}
