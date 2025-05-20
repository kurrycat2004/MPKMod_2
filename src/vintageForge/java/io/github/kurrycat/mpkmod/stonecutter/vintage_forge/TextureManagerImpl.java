package io.github.kurrycat.mpkmod.stonecutter.vintage_forge;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.render.texture.TextureManager;
import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.service.DefaultServiceProvider;
import io.github.kurrycat.mpkmod.service.ServiceProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public final class TextureManagerImpl implements TextureManager {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends DefaultServiceProvider<TextureManager> {
        public Provider() {
            super(TextureManagerImpl::new, TextureManager.class);
        }
    }

    @Override
    public void bindTexture(IResource texture) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(
                (ResourceLocation) texture.backendResource()
        );
    }
}
