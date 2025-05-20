package io.github.kurrycat.mpkmod.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

public interface ServiceProvider {
    Object provide();

    Class<?> type();

    default Optional<String> invalidReason() {
        return Optional.empty();
    }

    default int priority() {
        return 0;
    }

    ServiceProviderCache CACHE = new ServiceProviderCache();
    boolean LOG_PROVIDERS = Boolean.getBoolean("mpkmod.service.logProviders");

    class ServiceProviderCache {
        private boolean initialized = false;
        private Map<Class<?>, List<ServiceProvider>> cache;

        private synchronized void ensureInit() {
            if (initialized) return;
            cache = new HashMap<>();
            for (ServiceProvider provider : ServiceLoader.load(ServiceProvider.class)) {
                cache.computeIfAbsent(provider.type(), k -> new ArrayList<>())
                        .add(provider);
            }
            for (List<ServiceProvider> providers : cache.values()) {
                providers.sort(Comparator.comparingInt(ServiceProvider::priority).reversed());
            }
            initialized = true;
        }

        private List<ServiceProvider> get(Class<?> providerClass) {
            ensureInit();
            List<ServiceProvider> list = cache.get(providerClass);
            return list == null ? List.of() : list;
        }

        Object rawLoadOrThrow(Class<?> providerClass) {
            Map<ServiceProvider, String> reasons = new IdentityHashMap<>();
            List<ServiceProvider> providers = get(providerClass);
            if (LOG_PROVIDERS) {
                System.out.print("Loading service provider for " + providerClass.getName() + "\n");
                System.out.print("Found " + providers.size() + " potential provider(s):\n");
                for (ServiceProvider provider : providers) {
                    System.out.print("\t" + provider.getClass().getName() +
                                     " with priority " +
                                     provider.priority() + "\n");
                }
            }
            for (ServiceProvider provider : providers) {
                Optional<String> reason;
                try {
                    reason = provider.invalidReason();
                } catch (Exception e) {
                    reason = Optional.of("Exception while checking invalid reason: " + e);
                }
                if (reason.isEmpty()) {
                    if (LOG_PROVIDERS) {
                        System.out.print("Service provider " +
                                         provider.getClass().getName() +
                                         " with priority " +
                                         provider.priority() +
                                         " is valid\n");
                    }
                    return provider.provide();
                }
                reasons.put(provider, reason.get());
                if (LOG_PROVIDERS) {
                    System.out.print("Service provider " +
                                     provider.getClass().getName() +
                                     " with priority " +
                                     provider.priority() +
                                     " is invalid: " +
                                     reason.get() + "\n");
                }
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
}
