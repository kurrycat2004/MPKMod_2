package io.github.kurrycat.mpkmod.api.service;

import java.util.List;
import java.util.ServiceLoader;

public interface ServiceManager {
    static ServiceManager instance() {
        return Holder.INSTANCE;
    }

    class Holder {
        private static final ServiceManager INSTANCE;

        static {
            INSTANCE = ServiceLoader.load(ServiceManager.class, ServiceManager.class.getClassLoader()).findFirst().orElseThrow();
            INSTANCE.init();
        }
    }

    void init();

    <S> S get(Class<S> serviceClass);

    <S> List<ServiceProvider> getProviders(Class<S> serviceClass);

    void switchToService(ServiceProvider provider);

    void readyForSwitch(Class<?> serviceClass);
}
