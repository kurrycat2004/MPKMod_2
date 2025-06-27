package io.github.kurrycat.mpkmod.service;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.Tags;
import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.log.LogManager;
import io.github.kurrycat.mpkmod.api.service.ServiceManager;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.log.StdoutLogger;
import io.github.kurrycat.mpkmod.util.LogUtil;
import org.jetbrains.annotations.NotNull;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

@AutoService(ServiceManager.class)
public final class ServiceManagerImpl implements ServiceManager {
    private static final boolean LOG_PROVIDERS = Boolean.getBoolean("mpkmod.service.logProviders");
    private static final ILogger.Level LOG_LEVEL = LOG_PROVIDERS ? ILogger.Level.INFO : ILogger.Level.DEBUG;

    private ILogger.FixedLevel LOGGER;
    private final Map<Class<?>, List<ServiceProvider>> cache;
    private boolean initialized = false;

    private final ClassValue<RawServiceHolder<?>> HOLDERS = new ClassValue<>() {
        @Override
        protected RawServiceHolder<?> computeValue(@NotNull Class<?> service) {
            return new RawServiceHolder<>(service, rawLoadOrThrow(service));
        }
    };

    public ServiceManagerImpl() {
        LOGGER = StdoutLogger.FALLBACK.createSubLogger("service").createFixed(LOG_LEVEL);
        LOGGER.log("Initialized service manager using class loader: {}",
                ServiceManagerImpl.class.getClassLoader());

        Map<Class<?>, List<ServiceProvider>> cache = new HashMap<>();
        for (ServiceProvider provider : ServiceLoader.load(ServiceProvider.class, ServiceManager.class.getClassLoader())) {
            cache.computeIfAbsent(provider.type(), k -> new ArrayList<>())
                    .add(provider);
        }
        for (List<ServiceProvider> providers : cache.values()) {
            providers.sort(Comparator.comparingInt(ServiceProvider::priority).reversed());
        }
        this.cache = new HashMap<>();
        for (Map.Entry<Class<?>, List<ServiceProvider>> entry : cache.entrySet()) {
            Class<?> providerClass = entry.getKey();
            List<ServiceProvider> providers = entry.getValue();

            List<ServiceProviderWrapper> wrappedProviders = new ArrayList<>(providers.size());
            for (int i = 0; i < providers.size(); i++) {
                wrappedProviders.add(new ServiceProviderWrapper(providers.get(i), i));
            }
            this.cache.put(providerClass, Collections.unmodifiableList(wrappedProviders));
        }
    }

    @Override
    public void init() {
        if (initialized) return;
        LOGGER.log("Initializing logger service...");
        LOGGER = LogManager.instance().createLogger(Tags.MOD_ID).createSubLogger("service").createFixed(LOG_LEVEL);
        if (LOGGER.parentLogger() instanceof StdoutLogger) {
            LOGGER.log("Failed to initialize logger service, continuing with fallback logger");
        } else {
            LOGGER.log("Initialized logger service");
        }
        initialized = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> S get(Class<S> serviceClass) {
        return ((RawServiceHolder<S>) HOLDERS.get(serviceClass)).current();
    }

    @Override
    public <S> List<ServiceProvider> getProviders(Class<S> serviceClass) {
        return cache.getOrDefault(serviceClass, Collections.emptyList());
    }

    @Override
    public void switchToService(ServiceProvider provider) {
        List<ServiceProvider> providers;
        if (
                !(provider instanceof ServiceProviderWrapper wrapper) ||
                (providers = cache.get(provider.type())) == null ||
                wrapper.id() < 0 || wrapper.id() >= providers.size() ||
                providers.get(wrapper.id()) != provider
        ) {
            throw new IllegalArgumentException("Tried to switch to unregistered service provider: " + provider.name());
        }

        Optional<String> reason = provider.invalidReason();
        if (reason.isPresent()) {
            throw new IllegalStateException("Failed to switch to service provider " +
                                            provider.name() + ": " + reason.get());
        }

        LOGGER.log("Switching service {} from {} to provider {}",
                provider.type().getName(),
                HOLDERS.get(provider.type()).current().getClass().getName(),
                provider.name()
        );
        HOLDERS.get(provider.type()).switchTo(provider);
    }

    @Override
    public void readyForSwitch(Class<?> serviceClass) {
        HOLDERS.get(serviceClass).readyForSwitch();
    }

    private Object rawLoadOrThrow(Class<?> serviceClass) {
        Map<ServiceProvider, String> reasons = new IdentityHashMap<>();
        List<ServiceProvider> providers = cache.get(serviceClass);

        LOGGER.log("Loading service provider for {}", serviceClass.getName());
        if (providers == null || providers.isEmpty()) {
            String message = "No service provider found for " + serviceClass.getName();
            LOGGER.log(message);
            throw new IllegalStateException(message);
        }
        LOGGER.log("Found {} potential provider(s):", providers.size());
        for (ServiceProvider provider : providers) {
            LOGGER.log("\t{} with priority {}", provider.name(), provider.priority());
        }

        for (ServiceProvider provider : providers) {
            Optional<String> reason = provider.invalidReason();
            if (reason.isPresent()) {
                LOGGER.log("Service provider {} with priority {} is invalid: {}",
                        provider.name(), provider.priority(), reason.get());
                reasons.put(provider, reason.get());
                continue;
            }
            Object service = provider.provide();
            LOGGER.log("Selecting service provider {} with priority {}",
                    provider.name(), provider.priority());
            return service;
        }
        StringBuilder sb = new StringBuilder("No valid provider found for ")
                .append(serviceClass.getName())
                .append(": ")
                .append("All providers found are invalid: ");
        for (ServiceProvider provider : providers) {
            sb.append("\n\t").append(provider.name());
            String reason = reasons.get(provider);
            if (reason == null) {
                sb.append(" (no reason given)");
            } else {
                sb.append(" (").append(reason).append(")");
            }
        }

        throw new IllegalStateException(sb.toString());
    }

    private Object tryLoad(ServiceProvider provider, Map<ServiceProvider, String> errors) {
        Optional<String> reason;
        try {
            reason = provider.invalidReason();
        } catch (Exception e) {
            StringWriter builder = new StringWriter();
            builder.append("Exception while checking invalid reason: \n");
            LogUtil.appendPrefixedException(builder, "\t\t", e);
            reason = Optional.of(builder.toString());
        }
        if (reason.isEmpty()) {
            return provider.provide();
        }
        errors.put(provider, reason.get());
        return null;
    }
}
