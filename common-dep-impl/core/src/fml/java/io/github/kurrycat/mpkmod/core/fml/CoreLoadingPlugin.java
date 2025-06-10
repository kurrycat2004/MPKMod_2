package io.github.kurrycat.mpkmod.core.fml;

import io.github.kurrycat.mpkmod.Tags;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

public final class CoreLoadingPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{Tags.MOD_GROUP + ".core.fml.CoreTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
