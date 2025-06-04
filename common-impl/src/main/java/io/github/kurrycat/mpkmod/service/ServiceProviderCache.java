package io.github.kurrycat.mpkmod.service;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.Tags;
import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.log.LogManager;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.log.StdoutLogger;
import io.github.kurrycat.mpkmod.util.PrefixedPrintWriter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

@AutoService(ServiceProvider.Cache.class)
public final class ServiceProviderCache implements ServiceProvider.Cache {
    private static final boolean LOG_PROVIDERS = Boolean.getBoolean("mpkmod.service.logProviders");
    private static final ILogger.Level LOG_LEVEL = LOG_PROVIDERS ? ILogger.Level.INFO : ILogger.Level.DEBUG;

    private ILogger.FixedLevel LOGGER;
    private final Map<Class<?>, List<ServiceProvider>> cache;
    private boolean initialized = false;

    public ServiceProviderCache() {
        LOGGER = StdoutLogger.FALLBACK
                .createSubLogger("service")
                .createFixed(LOG_LEVEL);

        cache = new HashMap<>();
        for (ServiceProvider provider : ServiceLoader.load(ServiceProvider.class)) {
            cache.computeIfAbsent(provider.type(), k -> new ArrayList<>())
                    .add(provider);
        }
        for (List<ServiceProvider> providers : cache.values()) {
            providers.sort(Comparator.comparingInt(ServiceProvider::priority).reversed());
        }
    }

    @Override
    public void init() {
        if (initialized) return;
        LOGGER.log("Initializing logger service...");
        LOGGER = LogManager.INSTANCE.createLogger(Tags.MOD_ID)
                .createSubLogger("service")
                .createFixed(LOG_LEVEL);
        if (LOGGER.parentLogger() instanceof StdoutLogger) {
            LOGGER.log("Failed to initialize logger service, continuing with fallback logger");
        } else {
            LOGGER.log("Initialized logger service");
        }
        initialized = true;
    }

    private List<ServiceProvider> get(Class<?> providerClass) {
        List<ServiceProvider> list = cache.get(providerClass);
        return list == null ? List.of() : list;
    }

    public Object rawLoadOrThrow(Class<?> providerClass) {
        Map<ServiceProvider, String> reasons = new IdentityHashMap<>();
        List<ServiceProvider> providers = get(providerClass);

        LOGGER.log("Loading service provider for {}", providerClass.getName());
        LOGGER.log("Found {} potential provider(s):", providers.size());
        for (ServiceProvider provider : providers) {
            LOGGER.log("\t{} with priority {}", provider.getClass().getName(), provider.priority());
        }

        for (ServiceProvider provider : providers) {
            Optional<String> reason;
            try {
                reason = provider.invalidReason();
            } catch (Exception e) {
                reason = Optional.of("Exception while checking invalid reason: " + e);
            }
            if (reason.isEmpty()) {
                LOGGER.log("Service provider {} with priority {} is valid",
                        provider.getClass().getName(), provider.priority());
                try {
                    return provider.provide();
                } catch (Exception e) {
                    StringWriter builder = new StringWriter();
                    builder.append("Exception while providing service: \n");
                    try (PrintWriter writer = new PrefixedPrintWriter("\t\t", builder)) {
                        e.printStackTrace(writer);
                    } catch (Exception printException) {
                        builder.append("\n\tfailed to print stack trace: ");
                        builder.append(printException.toString());
                        builder.append("\n");
                    }
                    reason = Optional.of(builder.toString());
                }
            }
            reasons.put(provider, reason.get());

            LOGGER.log("Service provider {} with priority {} is invalid: {}",
                    provider.getClass().getName(), provider.priority(), reason.get());
        }
        StringBuilder sb = new StringBuilder("No valid provider found for ");
        sb.append(providerClass.getName()).append(": ");
        if (providers.isEmpty()) {
            sb.append("No providers found.");
        } else {
            sb.append("All providers found are invalid: ");
            for (ServiceProvider provider : providers) {
                sb.append("\n\t").append(provider.getClass().getName());
                String reason = reasons.get(provider);
                if (reason == null) {
                    sb.append(" (no reason given)");
                } else {
                    sb.append(" (").append(reason).append(")");
                }
            }
        }
        throw new IllegalStateException(sb.toString());
    }
}
